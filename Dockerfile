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

FROM amazoncorretto:18-alpine3.16 AS run

COPY --from=build /project/service/build/libs/service.jar service.jar

CMD java -jar service.jar