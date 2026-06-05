#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_DIR="${ROOT_DIR}/.runtime"
LOG_DIR="${RUNTIME_DIR}/logs"
PID_DIR="${RUNTIME_DIR}/pids"

REDIS_CONTAINER="${REDIS_CONTAINER:-shopify-redis}"
REDIS_IMAGE="${REDIS_IMAGE:-redis:7-alpine}"
REDIS_HOST="${REDIS_HOST:-localhost}"
REDIS_PORT="${REDIS_PORT:-6379}"

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-damu}"
DB_PASSWORD="${DB_PASSWORD:-password123}"
DB_ADMIN_USER="${DB_ADMIN_USER:-$DB_USER}"
DB_ADMIN_PASSWORD="${DB_ADMIN_PASSWORD:-$DB_PASSWORD}"

EUREKA_SERVER_ADDRESS="${EUREKA_SERVER_ADDRESS:-http://localhost:8761/eureka}"
CONFIG_SERVER_URL="${CONFIG_SERVER_URL:-http://localhost:9296}"
ZIPKIN_ENABLED="${ZIPKIN_ENABLED:-false}"
ZIPKIN_ENDPOINT="${ZIPKIN_ENDPOINT:-http://localhost:9411/api/v2/spans}"

IAM_JWT_ISSUER="${IAM_JWT_ISSUER:-http://localhost:8083}"
IAM_JWT_AUDIENCE="${IAM_JWT_AUDIENCE:-shop-api}"
IAM_JWT_PUBLIC_KEY="${IAM_JWT_PUBLIC_KEY:-MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsQLVFs53lYpXcfuoifwwOl1MQhKqWsF/YwhgN+7Mtm2SUrOjdKqE/Sq+bjdQTNS2E65pQ+/qFKITkMJdPvh8it5W/IYW000mNL1WESfMLl0Jxuc3pGfmU25Hr7dF0IeLqeciZViq5STIf/2t0Y1ErgSVT5+4xxltTHTo1X6T5jlpU3DpH9TDspf1zc/yPmmSXyu1dUmpuV+PObdiIVOHp72C9YJIV6vVn188yjN88j8Bma62RQ2xoln88xm//uDvfruivXEn2+knVEvMtm2YKTebwMc5Y25Y1uqgImfvzQMTja1vHVz8yfqO+S3dhocizelJzHsglGLFbnCsXeIL5QIDAQAB}"
IAM_JWT_PRIVATE_KEY="${IAM_JWT_PRIVATE_KEY:-MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCxAtUWzneVildx+6iJ/DA6XUxCEqpawX9jCGA37sy2bZJSs6N0qoT9Kr5uN1BM1LYTrmlD7+oUohOQwl0++HyK3lb8hhbTTSY0vVYRJ8wuXQnG5zekZ+ZTbkevt0XQh4up5yJlWKrlJMh//a3RjUSuBJVPn7jHGW1MdOjVfpPmOWlTcOkf1MOyl/XNz/I+aZJfK7V1Sam5X485t2IhU4envYL1gkhXq9WfXzzGb/+4O9+u6K9cSfb6SdUS8y2bZgpN5vAxzljbljW6qAiZ+/NAxONrW8dXPzJ+o75Ld2GhyLN6UnMeyCUYsVucKxd4gvlAgMBAAECggEAH1W6hh6HodPLFhTwKXkYyoQwlzO5r3Y4iD331n7yA0tTT0uYoHL9NtnluxoUn306PDKwVMP4Qq3MW+BkZFzVozmn2W0dRkuOe2CXB1NtvDlhHTJaMcLozZUkoJEHjVopWaredmVjX0wLKEkuh32EDnAmRGGocqpoJFQ/0WdmZCcynKwy2YtT+dgq14Yw9wdK/Pff523pVECwBPpVeBXqimg6chlm1njapqPwqDhseHRf5FplcBUnZ/pBSZqzS+ZHNHqTCP51PFmv79Q+UlY7p3zxZObLwY8nihxi4j3fRrkMqCIShbZ8NnZSMkWsxvdwaiNhQeqxwEaIZ5cGDfHZ4wKBgQDy8vb8K0eH0Fno3pZa+vHqFBzdv9LYGiJVFB2rPJ+kPy8B+OR+h/NEFD5kguWjZG+vdeBLyWcYfl4SqEb4BJ8FnHTnxzQA7e3vuIkTIDy5KW5RGTQlINaGMRE9HY86AbddyRcUC0PpQBV51jeHbulnnUszyH8ywywHBRfD1moDowKBgQC6hRaPuHkILdZ1AUvfdYH2PzvLSjoWUj01yQDUnkJYJzDemydZkFqY5xNmZx8khrgRCjE15mHtwAsGTu2MrwhOxoh0NJNnR1Ql+i10lYy+WPQMHJNtwKApYkX5aF69QuuD9e7hrcCMeR+1bwyOR83Cvu/8lr8zBzyIPIsr9sLq1wKBgQC+Q7NT550cavqO2gtMcy0T0e6NZ5X7MfRjRt65ZT/tBKKO29oukc/dKDF4y96F6Lli42DmWXOJBiYsXGdAA3Z6kX4oqqQ6ehuWWqHXPa7wn9bKYn+o+B+pdjH29/hcSE3PgBW31962hD6NbUuNoDngsJndkw6ytZD8amujLn9WGwKBgQCqBMTTDymsTfQtxZe+wqNLrso3fZrDXAWVsHqEVRpOtrKyR6Wv24jp1cD2WEuoBPFl/u9qnW1oxeSGxI1+7tHY1NnvazluLDPFRm4G1odrvzP+klHE+pBECOOV/BBX3tdF+3jb2IEqywIfZXGBhGxH97pUeakhv5fq651E3vNAEwKBgQCHW9bfBT4qs+GgCrCSVvjFjflO4IEwEg3c48LW9BfCW52Ks8aWpYkHV/r1XF/zk8s4G3qHIz0Yuo9AjW45SRqxfc8/9ali4xArvGiDzh6bqP1UwvR2qUn3EFH5CwTQ+hcPq4VVPpqM1gu1lAkxwxcPbsGv41fRn9bUuG3HBi3UaQ==}"
IAM_ACCESS_TOKEN_TTL_MINUTES="${IAM_ACCESS_TOKEN_TTL_MINUTES:-15}"
IAM_REFRESH_TOKEN_TTL_DAYS="${IAM_REFRESH_TOKEN_TTL_DAYS:-30}"

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

declare -a DOCKER_APP_CONTAINERS=(
  "shopify-cloud-gateway"
  "shopify-user-service"
  "shopify-order-service"
  "shopify-payment-service"
  "shopify-product-service"
  "shopify-config-server"
  "shopify-service-registry"
)

usage() {
  cat <<'USAGE'
Usage: scripts/deploy-ecosystem.sh [command]

Commands:
  start      Build, check local infrastructure, and launch all services
  up         Launch one service without rebuilding all services
  down       Stop one service
  stop       Stop services and Redis container
  restart    Stop then start
  build      Build all services only
  clean      Stop services and remove runtime logs, PID files, and build output
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
  CONFIG_SERVER_URL=url       Default: http://localhost:9296
  REDIS_HOST=host             Default: localhost
  REDIS_PORT=port             Default: 6379
  ZIPKIN_ENABLED=true|false   Default: false

Examples:
  scripts/deploy-ecosystem.sh up UserService
  scripts/deploy-ecosystem.sh down UserService
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

clean_runtime_files() {
  rm -rf "$LOG_DIR" "$PID_DIR"
  ensure_dirs
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

container_for_service() {
  case "$1" in
    service-registry) printf '%s\n' "shopify-service-registry" ;;
    ConfigServer) printf '%s\n' "shopify-config-server" ;;
    ProductService) printf '%s\n' "shopify-product-service" ;;
    PaymentService) printf '%s\n' "shopify-payment-service" ;;
    OrderService) printf '%s\n' "shopify-order-service" ;;
    UserService) printf '%s\n' "shopify-user-service" ;;
    CloudGateway) printf '%s\n' "shopify-cloud-gateway" ;;
  esac
}

ensure_redis() {
  if host_port_open "$REDIS_HOST" "$REDIS_PORT"; then
    log "Redis already reachable at ${REDIS_HOST}:${REDIS_PORT}"
    return 0
  fi

  require_command docker
  if ! docker_running; then
    printf 'Docker is not running. Start Docker and retry, or start Redis locally on %s:%s.\n' "$REDIS_HOST" "$REDIS_PORT" >&2
    exit 1
  fi
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
  local mvnw="${ROOT_DIR}/${service}/mvnw"
  if [[ "$SKIP_TESTS" == "true" ]]; then
    args+=("-DskipTests")
  fi

  log "Building ${service}"
  (
    cd "${ROOT_DIR}/${service}"
    if [[ -x "$mvnw" ]]; then
      "$mvnw" "${args[@]}"
    else
      require_command mvn
      mvn "${args[@]}"
    fi
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
    -print -quit
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
  if ! [[ -f "$pid_file" ]]; then
    return 1
  fi

  if kill -0 "$(cat "$pid_file")" >/dev/null 2>&1; then
    return 0
  fi

  rm -f "$pid_file"
  return 1
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

stop_stale_service_processes() {
  local service="$1"
  local service_target="${ROOT_DIR}/${service}/target/"
  local pid args

  while read -r pid args; do
    if [[ -z "${pid:-}" || "$pid" == "$$" ]]; then
      continue
    fi

    if [[ "$args" == *"java"* && "$args" == *"$service_target"*".jar"* ]]; then
      log "Stopping stale ${service} process with PID ${pid}"
      kill "$pid" >/dev/null 2>&1 || true
      for _ in $(seq 1 30); do
        if ! kill -0 "$pid" >/dev/null 2>&1; then
          break
        fi
        sleep 1
      done
      if kill -0 "$pid" >/dev/null 2>&1; then
        log "Force stopping stale ${service} process with PID ${pid}"
        kill -9 "$pid" >/dev/null 2>&1 || true
      fi
    fi
  done < <(ps -eo pid=,args=)
}

ensure_service_port_free() {
  local service="$1"
  local port="$2"
  local container

  if ! port_open "$port"; then
    return 0
  fi

  container="$(container_for_service "$service")"
  if [[ -n "$container" ]] && command -v docker >/dev/null 2>&1 && container_running "$container"; then
    log "Stopping container ${container} because it is using port ${port}"
    docker stop "$container" >/dev/null
  fi

  stop_stale_service_processes "$service"

  if port_open "$port"; then
    printf 'Port %s is already in use before starting %s.\n' "$port" "$service" >&2
    printf 'Stop the process using that port and retry. Current listener:\n' >&2
    ss -ltnp "sport = :${port}" >&2 || true
    exit 1
  fi
}

wait_for_port() {
  local service="$1"
  local port="$2"
  local pid_file
  pid_file="$(pid_file_for "$service")"

  log "Waiting for ${service} on port ${port}"
  for _ in $(seq 1 "$STARTUP_TIMEOUT"); do
    if [[ -f "$pid_file" ]] && ! kill -0 "$(cat "$pid_file")" >/dev/null 2>&1; then
      printf '%s exited before opening port %s. Last log lines:\n' "$service" "$port" >&2
      tail -n 80 "${LOG_DIR}/${service}.log" >&2 || true
      rm -f "$pid_file"
      exit 1
    fi
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
    "CONFIG_SERVER_URL=${CONFIG_SERVER_URL}"
    "EUREKA_SERVER_ADDRESS=${EUREKA_SERVER_ADDRESS}"
    "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=${EUREKA_SERVER_ADDRESS}"
    "SPRING_DATA_REDIS_HOST=${REDIS_HOST}"
    "SPRING_DATA_REDIS_PORT=${REDIS_PORT}"
    "ZIPKIN_ENABLED=${ZIPKIN_ENABLED}"
    "ZIPKIN_ENDPOINT=${ZIPKIN_ENDPOINT}"
    "IAM_JWT_ISSUER=${IAM_JWT_ISSUER}"
    "IAM_JWT_AUDIENCE=${IAM_JWT_AUDIENCE}"
    "IAM_JWT_PUBLIC_KEY=${IAM_JWT_PUBLIC_KEY}"
    "IAM_ACCESS_TOKEN_TTL_MINUTES=${IAM_ACCESS_TOKEN_TTL_MINUTES}"
    "IAM_REFRESH_TOKEN_TTL_DAYS=${IAM_REFRESH_TOKEN_TTL_DAYS}"
  )

  if [[ "$service" == "UserService" ]]; then
    env_vars+=("IAM_JWT_PRIVATE_KEY=${IAM_JWT_PRIVATE_KEY}")
  fi

  pid_file="$(pid_file_for "$service")"
  if is_service_running "$service"; then
    log "${service} already running with PID $(cat "$pid_file")"
    return 0
  fi

  ensure_service_port_free "$service" "$port"

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
  : >"${LOG_DIR}/${service}.log"
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

find_service_entry() {
  local requested="$1"
  local entry service port

  for entry in "${SERVICES[@]}"; do
    IFS=':' read -r service port <<<"$entry"
    if [[ "$requested" == "$service" ]]; then
      printf '%s:%s\n' "$service" "$port"
      return 0
    fi
  done

  printf 'Unknown service: %s\n' "$requested" >&2
  printf 'Available services:\n' >&2
  for entry in "${SERVICES[@]}"; do
    IFS=':' read -r service _ <<<"$entry"
    printf '  %s\n' "$service" >&2
  done
  exit 1
}

start_one_service() {
  local entry service port

  if [[ $# -ne 1 ]]; then
    printf 'Usage: scripts/deploy-ecosystem.sh up <service>\n' >&2
    exit 1
  fi

  ensure_dirs
  ensure_mysql
  ensure_redis
  entry="$(find_service_entry "$1")"
  IFS=':' read -r service port <<<"$entry"

  if [[ -z "$(jar_for_service "$service")" ]]; then
    build_service "$service"
  fi

  start_service "$service" "$port"
  status
}

stop_services() {
  for ((i = ${#SERVICES[@]} - 1; i >= 0; i--)); do
    IFS=':' read -r service _ <<<"${SERVICES[$i]}"
    stop_service "$service"
  done
}

stop_one_service() {
  local entry service

  if [[ $# -ne 1 ]]; then
    printf 'Usage: scripts/deploy-ecosystem.sh down <service>\n' >&2
    exit 1
  fi

  ensure_dirs
  entry="$(find_service_entry "$1")"
  IFS=':' read -r service _ <<<"$entry"
  stop_service "$service"
  status
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
  for container in "${DOCKER_APP_CONTAINERS[@]}"; do
    if container_running "$container"; then
      log "Stopping container ${container}"
      docker stop "$container" >/dev/null
    fi
  done
  if container_running "$REDIS_CONTAINER"; then
    log "Stopping container ${REDIS_CONTAINER}"
    docker stop "$REDIS_CONTAINER" >/dev/null
  fi
}

clean_build_output() {
  local entry service
  for entry in "${SERVICES[@]}"; do
    IFS=':' read -r service _ <<<"$entry"
    rm -rf "${ROOT_DIR}/${service}/target"
  done
}

clean_all() {
  ensure_dirs
  stop_services
  stop_infra
  clean_runtime_files
  clean_build_output
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
  elif host_port_open "$REDIS_HOST" "$REDIS_PORT"; then
    printf '  %-18s reachable %s:%s\n' "redis" "$REDIS_HOST" "$REDIS_PORT"
  else
    printf '  %-18s unreachable %s:%s\n' "redis" "$REDIS_HOST" "$REDIS_PORT"
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
  stop_services
  stop_infra
  clean_runtime_files
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
  up)
    start_one_service "${2:-}"
    ;;
  down)
    stop_one_service "${2:-}"
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
  clean)
    clean_all
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
