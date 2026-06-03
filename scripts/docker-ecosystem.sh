#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

usage() {
  cat <<'USAGE'
Usage: scripts/docker-ecosystem.sh [command]

Commands:
  start      Build jars, build Docker images, and start service(s)
  stop       Stop and remove service container(s)
  restart    Stop then start
  build      Build jars and Docker image(s) only
  clean      Remove containers, old local images, and orphan resources
  logs       Tail compose logs for service(s)
  status     Show compose service status

Examples:
  scripts/docker-ecosystem.sh start
  scripts/docker-ecosystem.sh start product-service
  scripts/docker-ecosystem.sh restart cloud-gateway
  scripts/docker-ecosystem.sh logs order-service

Services:
  service-registry config-server product-service payment-service
  order-service user-service cloud-gateway redis

Environment:
  DB_HOST=host                Default inside compose: localhost
  DB_PORT=port                Default: 3306
  DB_USER=user                Default: damu
  DB_PASSWORD=password        Default: password123
  PRODUCT_DB_URL=url          Optional full JDBC URL override
  PAYMENT_DB_URL=url          Optional full JDBC URL override
  ORDER_DB_URL=url            Optional full JDBC URL override
  USER_DB_URL=url             Optional full JDBC URL override
  SKIP_TESTS=true|false       Default for jar build: true
  NO_CACHE=true|false         Default for Docker build: true
  STOP_LOCAL_SERVICES=true|false
                             Stop services started by deploy-ecosystem.sh first. Default: true
USAGE
}

compose() {
  docker compose -f "${ROOT_DIR}/docker-compose.yml" "$@"
}

all_services() {
  printf '%s\n' \
    service-registry \
    config-server \
    product-service \
    payment-service \
    order-service \
    user-service \
    cloud-gateway \
    redis
}

image_for_service() {
  case "$1" in
    service-registry) printf '%s\n' "shopify/service-registry:local" ;;
    config-server) printf '%s\n' "shopify/config-server:local" ;;
    product-service) printf '%s\n' "shopify/product-service:local" ;;
    payment-service) printf '%s\n' "shopify/payment-service:local" ;;
    order-service) printf '%s\n' "shopify/order-service:local" ;;
    user-service) printf '%s\n' "shopify/user-service:local" ;;
    cloud-gateway) printf '%s\n' "shopify/cloud-gateway:local" ;;
    redis) printf '%s\n' "redis:7-alpine" ;;
    *)
      printf 'Unknown service: %s\n' "$1" >&2
      usage >&2
      exit 1
      ;;
  esac
}

port_for_service() {
  case "$1" in
    service-registry) printf '%s\n' "8761" ;;
    config-server) printf '%s\n' "9296" ;;
    product-service) printf '%s\n' "8080" ;;
    payment-service) printf '%s\n' "8081" ;;
    order-service) printf '%s\n' "8082" ;;
    user-service) printf '%s\n' "8083" ;;
    cloud-gateway) printf '%s\n' "9090" ;;
    redis) printf '%s\n' "6379" ;;
  esac
}

container_for_service() {
  case "$1" in
    service-registry) printf '%s\n' "shopify-service-registry" ;;
    config-server) printf '%s\n' "shopify-config-server" ;;
    product-service) printf '%s\n' "shopify-product-service" ;;
    payment-service) printf '%s\n' "shopify-payment-service" ;;
    order-service) printf '%s\n' "shopify-order-service" ;;
    user-service) printf '%s\n' "shopify-user-service" ;;
    cloud-gateway) printf '%s\n' "shopify-cloud-gateway" ;;
    redis) printf '%s\n' "shopify-redis" ;;
  esac
}

compose_build() {
  local -a args=(build)
  if [[ "${NO_CACHE:-true}" == "true" ]]; then
    args+=(--no-cache)
  fi
  compose "${args[@]}" "$@"
}

remove_service_images() {
  local service image

  for service in "$@"; do
    image="$(image_for_service "$service")"
    if docker image inspect "$image" >/dev/null 2>&1; then
      printf 'Removing image: %s\n' "$image"
      docker rmi -f "$image" >/dev/null
    fi
  done
}

clean_all() {
  compose down --remove-orphans --rmi local
}

remove_all_containers() {
  compose down --remove-orphans
}

remove_selected_containers() {
  local service container

  if [[ "$#" -eq 0 ]]; then
    remove_all_containers
    for service in $(all_services); do
      container="$(container_for_service "$service")"
      if [[ -n "$container" ]] && docker container inspect "$container" >/dev/null 2>&1; then
        docker rm -f "$container" >/dev/null
      fi
    done
    return 0
  fi

  compose rm -sf "$@"
  for service in "$@"; do
    container="$(container_for_service "$service")"
    if [[ -n "$container" ]] && docker container inspect "$container" >/dev/null 2>&1; then
      docker rm -f "$container" >/dev/null
    fi
  done
}

clean_selected() {
  if [[ "$#" -eq 0 ]]; then
    clean_all
    return 0
  fi

  remove_selected_containers "$@"
  remove_service_images "$@"
}

stop_local_services() {
  if [[ "${STOP_LOCAL_SERVICES:-true}" != "true" ]]; then
    return 0
  fi

  "${ROOT_DIR}/scripts/deploy-ecosystem.sh" stop
}

selected_services() {
  if [[ "$#" -eq 0 ]]; then
    all_services
  else
    printf '%s\n' "$@"
  fi
}

port_owner() {
  local port="$1"

  if command -v ss >/dev/null 2>&1; then
    ss -ltnp 2>/dev/null | awk -v port=":${port}" '$4 ~ port "$" {print}'
    return 0
  fi

  if command -v lsof >/dev/null 2>&1; then
    lsof -nP -iTCP:"$port" -sTCP:LISTEN 2>/dev/null
  fi
}

check_host_ports() {
  local service port owner
  local blocked=false

  for service in $(selected_services "$@"); do
    port="$(port_for_service "$service")"
    [[ -z "$port" ]] && continue

    owner="$(port_owner "$port")"
    if [[ -n "$owner" ]]; then
      printf 'Port %s is already in use for %s:\n%s\n' "$port" "$service" "$owner" >&2
      blocked=true
    fi
  done

  if [[ "$blocked" == "true" ]]; then
    printf 'Stop the process using the port, then run this script again.\n' >&2
    exit 1
  fi
}

build_all_jars() {
  "${ROOT_DIR}/scripts/deploy-ecosystem.sh" build
}

build_service_jar() {
  local compose_service="$1"
  local service_dir
  local -a prereq_dirs=()

  case "$compose_service" in
    service-registry) service_dir="service-registry" ;;
    config-server) service_dir="ConfigServer" ;;
    product-service) service_dir="ProductService" ;;
    payment-service) service_dir="PaymentService" ;;
    order-service)
      service_dir="OrderService"
      prereq_dirs=("ProductService")
      ;;
    user-service) service_dir="UserService" ;;
    cloud-gateway) service_dir="CloudGateway" ;;
    redis) return 0 ;;
    *)
      printf 'Unknown service: %s\n' "$compose_service" >&2
      usage >&2
      exit 1
      ;;
  esac

  local -a args=(clean install)
  if [[ "${SKIP_TESTS:-true}" == "true" ]]; then
    args+=("-DskipTests")
  fi

  local prereq_dir
  for prereq_dir in "${prereq_dirs[@]}"; do
    (cd "${ROOT_DIR}/${prereq_dir}" && ./mvnw "${args[@]}")
  done

  (cd "${ROOT_DIR}/${service_dir}" && ./mvnw "${args[@]}")
}

build_selected_jars() {
  if [[ "$#" -eq 0 ]]; then
    build_all_jars
    return 0
  fi

  local service
  for service in "$@"; do
    build_service_jar "$service"
  done
}

command="${1:-start}"
if [[ "$#" -gt 0 ]]; then
  shift
fi

case "$command" in
  start)
    stop_local_services
    remove_selected_containers "$@"
    check_host_ports "$@"
    remove_service_images $(selected_services "$@")
    build_selected_jars "$@"
    compose_build "$@"
    compose up -d --force-recreate "$@"
    compose ps
    ;;
  stop)
    if [[ "$#" -eq 0 ]]; then
      compose down
    else
      compose rm -sf "$@"
    fi
    ;;
  restart)
    stop_local_services
    remove_selected_containers "$@"
    check_host_ports "$@"
    remove_service_images $(selected_services "$@")
    build_selected_jars "$@"
    compose_build "$@"
    compose up -d --force-recreate "$@"
    compose ps
    ;;
  build)
    build_selected_jars "$@"
    compose_build "$@"
    ;;
  clean)
    clean_selected "$@"
    ;;
  logs)
    compose logs -f "$@"
    ;;
  status)
    compose ps "$@"
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    usage >&2
    exit 1
    ;;
esac
