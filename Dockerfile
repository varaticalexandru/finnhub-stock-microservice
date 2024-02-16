FROM bellsoft/liberica-openjdk-alpine
LABEL authors="alexandruvaratic"

COPY ./target/StockService-0.0.1-SNAPSHOT.jar .

CMD ["java", "-jar", "StockService-0.0.1-SNAPSHOT.jar"]