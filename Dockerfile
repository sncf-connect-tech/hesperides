FROM maven:3-jdk-11
WORKDIR /usr/local/src
COPY pom.xml .
COPY bootstrap bootstrap
COPY commons commons
COPY core core
COPY tests tests
COPY pom.xml .
RUN mvn clean package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true


FROM openjdk:11-jre-slim
LABEL maintainer="Team Avengers @ oui.sncf"

# Installing curl & MongoDB shell:
RUN apt-get update -y && apt-get install -y curl gnupg
RUN curl -s https://www.mongodb.org/static/pgp/server-4.2.asc | apt-key add -
RUN echo "deb http://repo.mongodb.org/apt/debian buster/mongodb-org/4.2 main" | tee /etc/apt/sources.list.d/mongodb-org-4.2.list
RUN apt-get update -y && apt-get install -y mongodb-org-shell

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

ARG PORT=8080
ENV PORT=$PORT
EXPOSE $PORT

HEALTHCHECK --interval=5s --timeout=3s --retries=3 CMD curl --fail http://localhost:$PORT/rest/manage/health || exit 1

RUN cp /usr/local/openjdk-*/bin/java /usr/local/bin/java

ARG UID=101
RUN useradd --uid $UID hesperides
USER $UID

# -XX:+ExitOnOutOfMemoryError : an OutOfMemoryError will often leave the JVM in an inconsistent state. Terminating the JVM will allow it to be restarted by an external process manager
# -XX:+HeapDumpOnOutOfMemoryError : get a heap dump when the app crashes
# -XX:-OmitStackTraceInFastThrow : avoid missing stacktraces cf. https://plumbr.io/blog/java/on-a-quest-for-missing-stacktraces
CMD /usr/local/bin/java $JAVA_OPTS \
     -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:-OmitStackTraceInFastThrow \
     -Xms2g -Xmx4g \
     -jar /hesperides.jar
