FROM veupathdb/alpine-dev-base:jdk-18-gradle-7.5.1 AS build

WORKDIR /project
RUN touch settings.gradle.kts

COPY build.gradle.kts build.gradle.kts
COPY src src/

RUN gradle test shadowJar

FROM amazoncorretto:18-alpine3.16 AS run

COPY --from=build /project/build/libs/service.jar service.jar

CMD java -jar service.jar