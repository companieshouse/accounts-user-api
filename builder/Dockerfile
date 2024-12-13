FROM ci-build-graalvm:latest AS loaded_deps

ARG M2_HOME=/root/.m2
ENV M2_HOME=$M2_HOME

COPY builder/settings.xml $M2_HOME/

WORKDIR /code
COPY pom.xml pom.xml
COPY src src
RUN mvn dependency:resolve-plugins dependency:resolve

####################################################
FROM ci-build-graalvm:latest AS builder

WORKDIR /code

COPY --from=loaded_deps /root/.m2/ /root/.m2/
COPY --from=loaded_deps /code /code/
COPY --chmod=700 ./builder/run-build.sh ./run-build.sh

RUN ./run-build.sh

####################################################
FROM 416670754337.dkr.ecr.eu-west-2.amazonaws.com/ci-base-build:latest AS runtime

COPY --from=builder /code/target/app /opt/app/

CMD [ "/opt/app/app" ]
