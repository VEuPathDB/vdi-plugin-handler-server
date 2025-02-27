FROM veupathdb/alpine-dev-base:jdk23-gradle8.11 AS build

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

WORKDIR /project

COPY [ \
  "settings.gradle.kts", \
  "build.gradle.kts", \
  "./" \
]

COPY components/ components/
COPY service/ service/
COPY gradle/libs.versions.toml gradle/libs.versions.toml

RUN gradle test shadowJar --info

FROM veupathdb/gus-apidb-base:1.2.7

ENV JVM_MEM_ARGS="-Xms16m -Xmx64m" \
    JVM_ARGS="" \
    TZ="America/New_York"

RUN apt-get update && apt-get install -y wget tzdata \
    && rm -rf /var/lib/apt/lists/* \
    && cp /usr/share/zoneinfo/America/New_York /etc/localtime \
    && echo ${TZ} > /etc/timezone

COPY startup.sh startup.sh
COPY --from=build /project/service/build/libs/service.jar service.jar

CMD /startup.sh
