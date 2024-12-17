FROM 416670754337.dkr.ecr.eu-west-2.amazonaws.com/ci-base-build:latest

RUN dnf update -y \
    && dnf install --allowerasing -y \
        gcc \
        glibc-devel \
        zlib-devel \
        libstdc++-static \
        curl \
        procps \
        maven  \
    && dnf clean all  \
    && curl -s "https://get.sdkman.io" | bash  \
    && source "$HOME/.sdkman/bin/sdkman-init.sh"  \
    && sdk install java 21.0.2-graalce
