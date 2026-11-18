FROM amazoncorretto:21

COPY ./target/kupi-api-gateway-0.0.1-SNAPSHOT.jar /kupi-api-gateway

EXPOSE 8080

ENTRYPOINT ["java","-jar","/kupi-api-gateway"]