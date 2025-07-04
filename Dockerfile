## Stage 1 : build with maven builder image with native capabilities
FROM quay.io/quarkus/centos-quarkus-maven:20.1.0-java11 AS build
COPY . /tmp/my-project
USER root
RUN chown -R quarkus /tmp/my-project
USER quarkus
RUN mvn -f /tmp/my-project/pom.xml -Pnative clean package

## Stage 2 : create the docker final image
#FROM registry.access.redhat.com/ubi8/ubi-minimal
FROM google/cloud-sdk:slim
#FROM bitnami/google-cloud-sdk
RUN apt-get update && apt-get -y --no-install-recommends install \
    kubectl \
    google-cloud-cli-gke-gcloud-auth-plugin \
    docker \
 && rm -rf /var/lib/apt/lists/*
COPY --from=build /tmp/my-project/target/*-runner /usr/local/bin/pipelining-tool
WORKDIR /workdir
#EXPOSE 8080
#CMD ["./application", "-XX:+PrintGC", "-XX:+PrintGCTimeStamps", "-XX:+VerboseGC", "+XX:+PrintHeapShape", "-Xmx128m", "-Dquarkus.http.host=0.0.0.0"]
ENTRYPOINT ["/usr/local/bin/pipelining-tool", "-Xmx128m", "-Dquarkus.http.host=0.0.0.0"]
