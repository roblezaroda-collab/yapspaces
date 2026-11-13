FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY src ./src

RUN mkdir -p out && \
    javac -d out $(find src -name "*.java")

ENV PORT=8080

EXPOSE 8080

CMD ["sh", "-c", "java -cp out yapspace.Main --port=${PORT} --data=/var/data"]
