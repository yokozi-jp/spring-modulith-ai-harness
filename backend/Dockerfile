# === dev: 開発用（bootRun + DevTools ホットリロード）===
FROM amazoncorretto:25 AS dev
WORKDIR /app
RUN yum install -y findutils && yum clean all
EXPOSE 8081 5005
CMD ["./gradlew", "bootRun"]

# === builder: Gradle ビルド → fat JAR 生成 ===
FROM amazoncorretto:25 AS builder
WORKDIR /app
RUN yum install -y findutils && yum clean all

COPY gradle/ gradle/
COPY gradlew settings.gradle build.gradle ./
RUN ./gradlew dependencies --no-daemon || true

COPY src/ src/
COPY config/ config/
RUN ./gradlew bootJar --no-daemon -x test -x check

# === runtime: JRE のみ + fat JAR（本番用）===
FROM amazoncorretto:25-headless AS runtime
WORKDIR /app

RUN yum install -y shadow-utils \
 && groupadd --system --gid 1000 app \
 && useradd --system --uid 1000 --gid app --no-create-home app \
 && yum remove -y shadow-utils \
 && yum clean all
USER app

COPY --from=builder /app/build/libs/demo-*-SNAPSHOT.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
