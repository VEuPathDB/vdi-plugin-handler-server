FROM veupathdb/alpine-dev-base:jdk-18-gradle-7.5.1 AS build

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

RUN gradle test shadowJar

FROM veupathdb/rserve:5.1.3

ENV JAVA_HOME=/opt/jdk \
    PATH=/opt/jdk/bin:$PATH \
    JVM_MEM_ARGS="-Xms16m -Xmx64m" \
    JVM_ARGS="" \
    TZ="America/New_York"

RUN apt-get update && apt-get install -y wget tzdata \
    && cd /opt \
    && wget https://corretto.aws/downloads/resources/19.0.2.7.1/amazon-corretto-19.0.2.7.1-linux-x64.tar.gz -O jdk.tgz \
    && tar -xf jdk.tgz \
    && rm jdk.tgz \
    && mv amazon-corretto-19.0.2.7.1-linux-x64 jdk \
    && rm -rf /var/lib/apt/lists/* \
    && cp /usr/share/zoneinfo/America/New_York /etc/localtime \
    && echo "America/New_York" > /etc/timezone

COPY startup.sh startup.sh
COPY --from=build /project/service/build/libs/service.jar service.jar

CMD chmod +x startup.sh && /startup.sh
