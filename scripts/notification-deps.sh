#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.yml"
SERVICES=(redis rabbitmq)
CONTAINERS=(shopify-redis shopify-rabbitmq)

usage() {
  cat <<'USAGE'
Usage: scripts/notification-deps.sh [command]

Commands:
  up         Start Redis and RabbitMQ, then wait for health
  down       Stop and remove Redis and RabbitMQ containers
  restart    Stop, start, and wait for health
  status     Show Redis and RabbitMQ compose status
  logs       Tail Redis and RabbitMQ logs

Default command: up

Local endpoints:
  Redis:      localhost:6379
  RabbitMQ:   localhost:5672
  Management: http://localhost:15672  (guest / guest)
USAGE
}

compose() {
  docker compose -f "${COMPOSE_FILE}" "$@"
}

remove_stale_containers() {
  local container status

  for container in "${CONTAINERS[@]}"; do
    if ! docker container inspect "$container" >/dev/null 2>&1; then
      continue
    fi

    status="$(docker inspect -f '{{.State.Status}}' "$container")"
    if [[ "$status" == "running" ]]; then
      continue
    fi

    printf 'Removing stale container: %s\n' "$container"
    docker rm "$container" >/dev/null
  done
}

wait_for_service() {
  local service="$1"
  local container health
  local deadline=$((SECONDS + 90))

  container="$(compose ps -q "$service")"
  if [[ -z "$container" ]]; then
    printf 'No container found for %s\n' "$service" >&2
    exit 1
  fi

  printf 'Waiting for %s to become healthy' "$service"
  while (( SECONDS < deadline )); do
    health="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container")"
    if [[ "$health" == "healthy" || "$health" == "running" ]]; then
      printf ' ok\n'
      return 0
    fi

    printf '.'
    sleep 2
  done

  printf '\n%s did not become healthy in time. Recent logs:\n' "$service" >&2
  compose logs --tail=40 "$service" >&2
  exit 1
}

wait_for_dependencies() {
  local service

  for service in "${SERVICES[@]}"; do
    wait_for_service "$service"
  done
}

command="${1:-up}"

case "$command" in
  up|start)
    remove_stale_containers
    compose up -d "${SERVICES[@]}"
    wait_for_dependencies
    compose ps "${SERVICES[@]}"
    ;;
  down|stop)
    compose rm -sf "${SERVICES[@]}"
    ;;
  restart)
    compose rm -sf "${SERVICES[@]}"
    remove_stale_containers
    compose up -d "${SERVICES[@]}"
    wait_for_dependencies
    compose ps "${SERVICES[@]}"
    ;;
  status)
    compose ps "${SERVICES[@]}"
    ;;
  logs)
    compose logs -f "${SERVICES[@]}"
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    usage >&2
    exit 1
    ;;
esac
