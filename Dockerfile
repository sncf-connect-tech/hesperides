FROM maven:3-jdk-8-alpine
WORKDIR /usr/local/src
COPY bootstrap bootstrap
COPY commons commons
COPY core core
COPY tests tests
COPY pom.xml .
RUN mvn clean package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true


FROM openjdk:8-jre-alpine

RUN apk add curl mongodb
# We only need the MongoDB shell:
RUN rm /etc/init.d/mongo* /etc/conf.d/mongo* /usr/bin/mongod /usr/bin/mongos /etc/logrotate.d/mongodb /usr/bin/install_compass

COPY --from=0 /usr/local/src/bootstrap/target/hesperides-*.jar hesperides.jar
COPY mongo_create_collections.js /
COPY docker_entrypoint.sh /
RUN chmod u+x /docker_entrypoint.sh

ARG BUILD_TIME
ENV BUILD_TIME=$BUILD_TIME
ARG GIT_BRANCH
ENV GIT_BRANCH=$GIT_BRANCH
ARG GIT_COMMIT
ENV GIT_COMMIT=$GIT_COMMIT
ARG GIT_COMMIT_MSG
ENV GIT_COMMIT_MSG=$GIT_COMMIT_MSG
ARG GIT_TAG
ENV GIT_TAG=$GIT_TAG
ENV SENTRY_TAGS=GIT_BRANCH:$GIT_BRANCH,GIT_COMMIT:$GIT_COMMIT,GIT_TAG:$GIT_TAG

ENTRYPOINT ["/docker_entrypoint.sh"]

EXPOSE 8080

HEALTHCHECK --interval=5s --timeout=3s --retries=3 CMD curl --fail http://localhost:8080/rest/manage/health || exit 1

# -XX:+ExitOnOutOfMemoryError // an OutOfMemoryError will often leave the JVM in an inconsistent state. Terminating the JVM will allow it to be restarted by an external process manager
# -XX:+HeapDumpOnOutOfMemoryError // get a heap dump when the app crashes
CMD /usr/bin/java $JAVA_OPTS \
     -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError \
     -Xms2g -Xmx4g \
     -jar /hesperides.jar
