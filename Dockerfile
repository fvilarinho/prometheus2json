# Base image defintopn.
FROM alpine:3.19.1

# Matadata definition.
LABEL authors="fvilarinho@gmail.com"

# OS environment variables.
ENV HOME_DIR=/home/prometheus2json
ENV BIN_DIR=${HOME_DIR}/bin
ENV ETC_DIR=${HOME_DIR}/etc
ENV LIBS_DIR=${HOME_DIR}/libs
ENV LOGS_DIR=${HOME_DIR}/logs

# Creates the directory structure.
RUN mkdir -p ${HOME_DIR} ${BIN_DIR} ${ETC_DIR} ${LIBS_DIR} ${LOGS_DIR}

# Installs all required software.
RUN apk update && \
    apk add --no-cache bash ca-certificates wget curl unzip vim net-tools bind-tools openjdk20-jre

# Copies all binaries, libraries and scripts.
COPY bin/*.sh ${BIN_DIR}/
COPY build/libs/prometheus2json.jar ${LIBS_DIR}/

# Gives the execution permission.
RUN chmod +X ${BIN_DIR}/*.sh && \
    ln -s ${BIN_DIR}/startup.sh /entrypoint.sh

# Entrypoint definition.
ENTRYPOINT ["/entrypoint.sh"]