GRADLE_CMD := $(shell command -v gradle || echo './gradlew') \
	-q \
	-Dgpr.user='${GITHUB_USERNAME}' \
	-Dgpr.key='${GITHUB_TOKEN}'

.PHONY: default
default:
	@echo "what are you doing?"

.PHONY: build
build:
	@$(GRADLE_CMD) build


.PHONY: clean
clean:
	@$(GRADLE_CMD) clean

docs/http-api.html: service/api.yml node_modules/.bin/redocly
	@node_modules/.bin/redocly build-docs -o $@ $<

node_modules/.bin/redocly:
	@npm i
