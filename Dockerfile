FROM veupathdb/alpine-dev-base:jdk-18-gradle-7.5.1 AS build

WORKDIR /project

COPY [ \
  "settings.gradle.kts", \
  "build.gradle.kts", \
  "./" \
]

COPY components/ components/
COPY service/ service/

RUN gradle test shadowJar

FROM veupathdb/rserve:2.1.3

ENV JAVA_HOME=/opt/jdk \
    PATH=/opt/jdk/bin:$PATH \
    JVM_MEM_ARGS="" \
    JVM_ARGS=""

RUN apt install -y wget python3 \
    && cd /opt \
    && wget https://corretto.aws/downloads/resources/19.0.2.7.1/amazon-corretto-19.0.2.7.1-linux-x64.tar.gz -O jdk.tgz \
    && tar -xf jdk.tgz \
    && rm jdk.tgz \
    && mv amazon-corretto-19.0.2.7.1-linux-x64 jdk

COPY startup.sh startup.sh
COPY --from=build /project/service/build/libs/service.jar service.jar

CMD chmod +x startup.sh && /startup.sh
