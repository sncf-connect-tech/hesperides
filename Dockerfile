FROM openjdk:8-jre-alpine

COPY bootstrap/target/hesperides-*.jar hesperides-spring.jar

ENTRYPOINT ["/usr/bin/java"]

CMD ["-jar","/hesperides-spring.jar"]

EXPOSE 8080
