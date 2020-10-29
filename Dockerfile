FROM fedora:33
LABEL MAINTAINER LukasWoodtli <woodtli.lukas@gmail.com>

RUN yum -y update && yum clean all
RUN yum -y install \
    grpc \
    grpc-devel \
    grpc-plugins \
    protobuf-compiler  \
    openssl \
    openssl-devel \
    protobuf \
    protobuf-devel \
    cmake \
    clang \
    binutils \
    ninja-build \
    java-11-openjdk \
    vim \
    golang \
    less \
    curl \
    git \
&& yum clean all

ENTRYPOINT ["/bin/bash", "-c"]