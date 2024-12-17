FROM ci-graalvm-builder:latest AS builder

ARG M2_HOME=/root/.m2
ENV M2_HOME=$M2_HOME

COPY builder/settings.xml $M2_HOME/

WORKDIR /code
COPY pom.xml pom.xml
COPY src src
RUN mvn dependency:resolve-plugins dependency:resolve

WORKDIR /code
COPY --chmod=700 ./builder/run-build.sh ./run-build.sh
RUN ./run-build.sh

####################################################
FROM ci-graalvm-runtime:latest AS runtime

COPY --from=builder /code/target/app /opt/app/

CMD [ "/opt/app/app" ]
