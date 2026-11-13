
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY src ./src

RUN mkdir -p out && \
    javac -d out $(find src -name "*.java")

EXPOSE 8080

CMD ["java", "-cp", "out", "yapspace.Main", "--data=/var/data"]
