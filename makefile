default:
	@echo "What are you doing?"

docker-build:
	@docker build \
		-t veupathdb/vdi-plugin-handler-server:latest \
		--build-arg GITHUB_USERNAME=${GITHUB_USERNAME} \
		--build-arg GITHUB_TOKEN=${GITHUB_TOKEN} \
		.