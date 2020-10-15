FROM fedora:33
MAINTAINER LukasWoodtli <woodtli.lukas@gmail.com>

RUN yum -y update && yum clean all
RUN yum -y install grpc cmake clang vim && yum clean all

ENTRYPOINT ["/bin/bash"]