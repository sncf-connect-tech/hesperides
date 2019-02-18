FROM openjdk:8-jre-alpine

RUN apk add curl mongodb
# We only need the MongoDB shell:
RUN rm /etc/init.d/mongo* /etc/conf.d/mongo* /usr/bin/mongod /usr/bin/mongos /etc/logrotate.d/mongodb /usr/bin/install_compass

COPY bootstrap/target/hesperides-*.jar hesperides.jar
COPY mongo_create_collections.js /
COPY docker_entrypoint.sh /
RUN chmod u+x /docker_entrypoint.sh

ENTRYPOINT ["/docker_entrypoint.sh"]

CMD ["/usr/bin/java", "-jar","/hesperides.jar"]

EXPOSE 8080

HEALTHCHECK --interval=5s --timeout=3s --retries=3 CMD curl --fail http://localhost:8080/rest/manage/health || exit 1
