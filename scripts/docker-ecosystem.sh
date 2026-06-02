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
  DB_HOST=host                Default inside compose: host.docker.internal
  DB_PORT=port                Default: 3306
  DB_USER=user                Default: damu
  DB_PASSWORD=password        Default: password123
  SKIP_TESTS=true|false       Default for jar build: true
USAGE
}

compose() {
  docker compose -f "${ROOT_DIR}/docker-compose.yml" "$@"
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
    build_selected_jars "$@"
    compose up --build -d "$@"
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
    if [[ "$#" -eq 0 ]]; then
      compose down
    else
      compose rm -sf "$@"
    fi
    build_selected_jars "$@"
    compose up --build -d "$@"
    compose ps
    ;;
  build)
    build_selected_jars "$@"
    compose build "$@"
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
