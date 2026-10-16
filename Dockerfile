# syntax=docker/dockerfile:1

# ---------------------------------------------------------------- 1. Build
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Dependencies first: this layer is only rebuilt when pom.xml changes, not on every code change
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 sh mvnw -B -q dependency:go-offline

COPY src src
# Tests need Docker (Testcontainers) and run in CI instead
RUN --mount=type=cache,target=/root/.m2 sh mvnw -B -q package -DskipTests

# ---------------------------------------------------------------- 2. Split the jar into layers
FROM eclipse-temurin:25-jdk AS layers
WORKDIR /workspace
COPY --from=build /workspace/target/tshop-*.jar app.jar
# Spring Boot's layered jar: dependencies, loader, snapshot dependencies and our code go to
# separate folders, copied below as separate image layers
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# ---------------------------------------------------------------- 3. Run
FROM eclipse-temurin:25-jre
WORKDIR /app

# Never run as root inside the container
RUN useradd --system --uid 1001 tshop
USER tshop

# Least to most frequently changed, so a code change only replaces the last (small) layer
COPY --from=layers /workspace/extracted/dependencies/ ./
COPY --from=layers /workspace/extracted/spring-boot-loader/ ./
COPY --from=layers /workspace/extracted/snapshot-dependencies/ ./
COPY --from=layers /workspace/extracted/application/ ./

EXPOSE 9000
# Size the heap from the container's memory limit instead of the host's
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
