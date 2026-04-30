# === builder: Gradle ビルド → fat JAR 生成 ===
FROM amazoncorretto:25 AS builder
WORKDIR /app
RUN yum install -y findutils && yum clean all

# Gradle wrapper + 設定を先にコピーしてキャッシュ活用
COPY gradle/ gradle/
COPY gradlew settings.gradle build.gradle ./
RUN ./gradlew dependencies --no-daemon || true

# ソースコピー → ビルド
COPY src/ src/
COPY config/ config/
RUN ./gradlew bootJar --no-daemon -x test -x check

# === runtime: JRE のみ + fat JAR ===
FROM amazoncorretto:25-headless AS runtime
WORKDIR /app

RUN yum install -y shadow-utils \
 && groupadd --system --gid 1000 app \
 && useradd --system --uid 1000 --gid app --no-create-home app \
 && yum remove -y shadow-utils \
 && yum clean all
USER app

COPY --from=builder /app/build/libs/demo-*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
