FROM openjdk:8-jre-alpine

COPY bootstrap/target/hesperides-*.jar hesperides.jar
RUN apk install curl

ENTRYPOINT ["/usr/bin/java"]

CMD ["-jar","/hesperides.jar"]

EXPOSE 8080

HEALTHCHECK --interval=5s --timeout=3s --retries=3 CMD curl --fail http://localhost:8080/rest/manage/health || exit 1
