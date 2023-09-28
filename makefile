IMAGE_NAME = "veupathdb/vdi-plugin-handler-server"

default:
	@echo "What are you doing?"

docker-build:
	@docker build \
		-t $(IMAGE_NAME):latest \
		--build-arg GITHUB_USERNAME=${GITHUB_USERNAME} \
		--build-arg GITHUB_TOKEN=${GITHUB_TOKEN} \
		.

docker-run:
	@docker run -it --rm --env-file=.env -p8080:8080 $(IMAGE_NAME):latest
