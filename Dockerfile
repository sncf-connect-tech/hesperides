FROM openjdk:8-jre-alpine

COPY bootstrap/target/hesperides-*.jar hesperides.jar

ENTRYPOINT ["/usr/bin/java"]

CMD ["-jar","/hesperides.jar"]

EXPOSE 8080
