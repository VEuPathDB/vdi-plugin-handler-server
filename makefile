default:
	@echo "What are you doing?"

docker-build:
	@docker build -t veupathdb/vdi-plugin-handler-server:latest .