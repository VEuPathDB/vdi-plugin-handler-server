IMAGE_NAME = "veupathdb/vdi-plugin-handler-server"

ifeq '$(shell command -v podman 2>&1 >/dev/null; echo $$?)' '0'
CONTAINER_CMD := podman
else
CONTAINER_CMD := docker
endif

default:
	@echo "what are you doing?"

.PHONY: build
build:
	@$(CONTAINER_CMD) compose \
        -f docker-compose.dev.yml \
        --env-file=.env build \
		--build-arg GITHUB_USERNAME=${GITHUB_USERNAME} \
		--build-arg GITHUB_TOKEN=${GITHUB_TOKEN}

.PHONY: up
up:
	@mkdir -p '.tmp/mount/build-68'
	@$(CONTAINER_CMD) compose -f docker-compose.dev.yml --env-file=.env up

.PHONY: down
down:
	@$(CONTAINER_CMD) compose -f docker-compose.dev.yml --env-file=.env down --remove-orphans -v
	@rm -rf '.tmp/mount/build-68/*'

docs/http-api.html: service/api.yml node_modules/.bin/redocly
	@node_modules/.bin/redocly build-docs -o $@ $<

node_modules/.bin/redocly:
	@npm i
