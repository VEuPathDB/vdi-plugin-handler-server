IMAGE_NAME = "veupathdb/vdi-plugin-handler-server"

default:
	@echo "What are you doing?"

compose-build:
	@docker compose \
        -f docker-compose.dev.yml \
        --env-file=.env build \
		--build-arg GITHUB_USERNAME=${GITHUB_USERNAME} \
		--build-arg GITHUB_TOKEN=${GITHUB_TOKEN}

compose-up:
	@mkdir -p '.tmp/mount/build-68'
	@docker compose -f docker-compose.dev.yml --env-file=.env up

compose-down:
	@docker compose -f docker-compose.dev.yml --env-file=.env down --remove-orphans -v
	@rm -rf '.tmp/mount/build-68/*'
