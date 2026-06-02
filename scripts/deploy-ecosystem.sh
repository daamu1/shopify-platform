#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_DIR="${ROOT_DIR}/.runtime"
LOG_DIR="${RUNTIME_DIR}/logs"
PID_DIR="${RUNTIME_DIR}/pids"

REDIS_CONTAINER="${REDIS_CONTAINER:-shopify-redis}"
REDIS_IMAGE="${REDIS_IMAGE:-redis:7-alpine}"

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-damu}"
DB_PASSWORD="${DB_PASSWORD:-password123}"
DB_ADMIN_USER="${DB_ADMIN_USER:-$DB_USER}"
DB_ADMIN_PASSWORD="${DB_ADMIN_PASSWORD:-$DB_PASSWORD}"

EUREKA_SERVER_ADDRESS="${EUREKA_SERVER_ADDRESS:-http://localhost:8761/eureka}"
ZIPKIN_ENABLED="${ZIPKIN_ENABLED:-false}"
ZIPKIN_ENDPOINT="${ZIPKIN_ENDPOINT:-http://localhost:9411/api/v2/spans}"

SKIP_TESTS="${SKIP_TESTS:-true}"
STARTUP_TIMEOUT="${STARTUP_TIMEOUT:-120}"

declare -a SERVICES=(
  "service-registry:8761"
  "ConfigServer:9296"
  "ProductService:8080"
  "PaymentService:8081"
  "OrderService:8082"
  "UserService:8083"
  "CloudGateway:9090"
)

usage() {
  cat <<'USAGE'
Usage: scripts/deploy-ecosystem.sh [command]

Commands:
  start      Build, check local infrastructure, and launch all services
  stop       Stop services and Redis container
  restart    Stop then start
  build      Build all services only
  status     Show service and infrastructure status
  logs       Tail service logs

Useful environment variables:
  SKIP_TESTS=true|false       Default: true
  STARTUP_TIMEOUT=seconds     Default: 120
  DB_HOST=host                Default: localhost
  DB_PORT=port                Default: 3306
  DB_USER=user                Default: damu
  DB_PASSWORD=password        Default: password123
  DB_ADMIN_USER=user          Default: same as DB_USER
  DB_ADMIN_PASSWORD=password  Default: same as DB_PASSWORD
  ZIPKIN_ENABLED=true|false   Default: false
USAGE
}

log() {
  printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    printf 'Required command not found: %s\n' "$1" >&2
    exit 1
  fi
}

ensure_dirs() {
  mkdir -p "$LOG_DIR" "$PID_DIR"
}

docker_running() {
  docker info >/dev/null 2>&1
}

container_exists() {
  docker ps -a --format '{{.Names}}' | grep -qx "$1"
}

container_running() {
  docker ps --format '{{.Names}}' | grep -qx "$1"
}

ensure_redis() {
  require_command docker
  if container_running "$REDIS_CONTAINER"; then
    log "Redis container already running: ${REDIS_CONTAINER}"
  elif container_exists "$REDIS_CONTAINER"; then
    log "Starting existing Redis container: ${REDIS_CONTAINER}"
    docker start "$REDIS_CONTAINER" >/dev/null
  else
    log "Creating Redis container: ${REDIS_CONTAINER}"
    docker run -d --name "$REDIS_CONTAINER" -p 6379:6379 "$REDIS_IMAGE" >/dev/null
  fi
}

wait_for_mysql() {
  log "Checking local MySQL at ${DB_HOST}:${DB_PORT}"
  for _ in $(seq 1 "$STARTUP_TIMEOUT"); do
    if host_port_open "$DB_HOST" "$DB_PORT"; then
      return 0
    fi
    sleep 1
  done

  printf 'Timed out waiting for local MySQL at %s:%s. Start MySQL and retry.\n' "$DB_HOST" "$DB_PORT" >&2
  exit 1
}

create_databases() {
  if ! command -v mysql >/dev/null 2>&1; then
    cat >&2 <<SQL
mysql CLI not found, so databases could not be created automatically.
Create these databases in your local MySQL before starting services:
  productdb
  paymentdb
  orderdb
  userdb
SQL
    return 0
  fi

  log "Creating application databases if missing"
  MYSQL_PWD="$DB_ADMIN_PASSWORD" mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_ADMIN_USER" <<SQL
CREATE DATABASE IF NOT EXISTS productdb;
CREATE DATABASE IF NOT EXISTS paymentdb;
CREATE DATABASE IF NOT EXISTS orderdb;
CREATE DATABASE IF NOT EXISTS userdb;
SQL
}

ensure_mysql() {
  wait_for_mysql
  create_databases
}

build_service() {
  local service="$1"
  local -a args=(clean install)
  if [[ "$SKIP_TESTS" == "true" ]]; then
    args+=("-DskipTests")
  fi

  log "Building ${service}"
  (
    cd "${ROOT_DIR}/${service}"
    ./mvnw "${args[@]}"
  )
}

build_all() {
  require_command java
  build_service service-registry
  build_service ConfigServer
  build_service ProductService
  build_service PaymentService
  build_service UserService
  build_service OrderService
  build_service CloudGateway
}

jar_for_service() {
  local service="$1"
  find "${ROOT_DIR}/${service}/target" -maxdepth 1 -type f -name '*.jar' \
    ! -name '*-sources.jar' ! -name '*-javadoc.jar' ! -name '*.original' \
    | head -n 1
}

pid_file_for() {
  printf '%s/%s.pid\n' "$PID_DIR" "$1"
}

db_name_for_service() {
  case "$1" in
    ProductService) printf '%s\n' "productdb" ;;
    PaymentService) printf '%s\n' "paymentdb" ;;
    OrderService) printf '%s\n' "orderdb" ;;
    UserService) printf '%s\n' "userdb" ;;
  esac
}

is_service_running() {
  local service="$1"
  local pid_file
  pid_file="$(pid_file_for "$service")"
  [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" >/dev/null 2>&1
}

port_open() {
  local port="$1"
  (echo >"/dev/tcp/127.0.0.1/${port}") >/dev/null 2>&1
}

host_port_open() {
  local host="$1"
  local port="$2"
  (echo >"/dev/tcp/${host}/${port}") >/dev/null 2>&1
}

wait_for_port() {
  local service="$1"
  local port="$2"

  log "Waiting for ${service} on port ${port}"
  for _ in $(seq 1 "$STARTUP_TIMEOUT"); do
    if port_open "$port"; then
      return 0
    fi
    sleep 1
  done

  printf 'Timed out waiting for %s on port %s. See %s/%s.log\n' "$service" "$port" "$LOG_DIR" "$service" >&2
  exit 1
}

start_service() {
  local service="$1"
  local port="$2"
  local pid_file
  local jar
  local db_name
  local -a env_vars=(
    "DB_HOST=${DB_HOST}"
    "EUREKA_SERVER_ADDRESS=${EUREKA_SERVER_ADDRESS}"
    "ZIPKIN_ENABLED=${ZIPKIN_ENABLED}"
    "ZIPKIN_ENDPOINT=${ZIPKIN_ENDPOINT}"
  )

  pid_file="$(pid_file_for "$service")"
  if is_service_running "$service"; then
    log "${service} already running with PID $(cat "$pid_file")"
    return 0
  fi

  if port_open "$port"; then
    printf 'Port %s is already in use before starting %s. Stop that process and retry.\n' "$port" "$service" >&2
    exit 1
  fi

  jar="$(jar_for_service "$service")"
  if [[ -z "$jar" ]]; then
    printf 'No jar found for %s. Run build first.\n' "$service" >&2
    exit 1
  fi

  db_name="$(db_name_for_service "$service")"
  if [[ -n "$db_name" ]]; then
    env_vars+=(
      "SPRING_DATASOURCE_URL=jdbc:mysql://${DB_HOST}:${DB_PORT}/${db_name}"
      "SPRING_DATASOURCE_USERNAME=${DB_USER}"
      "SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}"
    )
  fi

  log "Starting ${service} from ${jar##*/}"
  (
    cd "${ROOT_DIR}/${service}"
    nohup env "${env_vars[@]}" java -jar "$jar" >"${LOG_DIR}/${service}.log" 2>&1 &
    printf '%s\n' "$!" >"$pid_file"
  )

  wait_for_port "$service" "$port"
}

start_services() {
  for entry in "${SERVICES[@]}"; do
    IFS=':' read -r service port <<<"$entry"
    start_service "$service" "$port"
  done
}

stop_services() {
  for ((i = ${#SERVICES[@]} - 1; i >= 0; i--)); do
    IFS=':' read -r service _ <<<"${SERVICES[$i]}"
    stop_service "$service"
  done
}

stop_service() {
  local service="$1"
  local pid_file
  pid_file="$(pid_file_for "$service")"

  if ! [[ -f "$pid_file" ]]; then
    return 0
  fi

  local pid
  pid="$(cat "$pid_file")"
  if kill -0 "$pid" >/dev/null 2>&1; then
    log "Stopping ${service} with PID ${pid}"
    kill "$pid" >/dev/null 2>&1 || true
    for _ in $(seq 1 30); do
      if ! kill -0 "$pid" >/dev/null 2>&1; then
        break
      fi
      sleep 1
    done
    if kill -0 "$pid" >/dev/null 2>&1; then
      log "Force stopping ${service}"
      kill -9 "$pid" >/dev/null 2>&1 || true
    fi
  fi
  rm -f "$pid_file"
}

stop_infra() {
  if ! command -v docker >/dev/null 2>&1; then
    return 0
  fi
  if container_running "$REDIS_CONTAINER"; then
    log "Stopping container ${REDIS_CONTAINER}"
    docker stop "$REDIS_CONTAINER" >/dev/null
  fi
}

status() {
  printf '\nServices:\n'
  for entry in "${SERVICES[@]}"; do
    IFS=':' read -r service port <<<"$entry"
    if is_service_running "$service"; then
      printf '  %-18s running  pid=%s port=%s\n' "$service" "$(cat "$(pid_file_for "$service")")" "$port"
    else
      printf '  %-18s stopped  port=%s\n' "$service" "$port"
    fi
  done

  printf '\nInfrastructure:\n'
  if host_port_open "$DB_HOST" "$DB_PORT"; then
    printf '  %-18s reachable %s:%s\n' "local-mysql" "$DB_HOST" "$DB_PORT"
  else
    printf '  %-18s unreachable %s:%s\n' "local-mysql" "$DB_HOST" "$DB_PORT"
  fi

  if command -v docker >/dev/null 2>&1 && container_running "$REDIS_CONTAINER"; then
    printf '  %-18s running\n' "$REDIS_CONTAINER"
  else
    printf '  %-18s stopped\n' "$REDIS_CONTAINER"
  fi
}

tail_logs() {
  ensure_dirs
  if ! compgen -G "${LOG_DIR}/*.log" >/dev/null; then
    printf 'No logs found in %s\n' "$LOG_DIR" >&2
    exit 1
  fi
  tail -n 80 -f "${LOG_DIR}"/*.log
}

start_all() {
  ensure_dirs
  build_all
  ensure_mysql
  ensure_redis
  start_services
  status
  log "Gateway: http://localhost:9090"
  log "Eureka:  http://localhost:8761"
}

command="${1:-start}"
case "$command" in
  start)
    start_all
    ;;
  stop)
    ensure_dirs
    stop_services
    stop_infra
    ;;
  restart)
    ensure_dirs
    stop_services
    stop_infra
    start_all
    ;;
  build)
    ensure_dirs
    build_all
    ;;
  status)
    ensure_dirs
    status
    ;;
  logs)
    tail_logs
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    usage >&2
    exit 1
    ;;
esac
