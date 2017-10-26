FROM java:8-jre

COPY ./docker/docker-conf.yml /etc/hesperides/hesperides.yml

COPY ./target/hesperides.jar /jars/hesperides.jar

EXPOSE 8080
EXPOSE 8081

CMD java -jar /jars/hesperides.jar server /etc/hesperides/hesperides.yml
