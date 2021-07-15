FROM rust:1.53.0

RUN rustup install nightly-2020-08-20
RUN rustup default nightly-2020-08-20
RUN apt update
RUN apt install maven -y

RUN mkdir -p /zkStrata

WORKDIR /zkStrata

COPY src ./src
COPY pom.xml ./pom.xml
COPY examples ./examples
