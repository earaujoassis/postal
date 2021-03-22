FROM openjdk:13-alpine3.10

LABEL "com.quatrolabs.postal"="quatroLABS Postal"
LABEL "description"="A security- and privacy-first, mobile-first, and offline-first mail service application"

RUN apk add --update --no-cache \
    binutils-gold \
    curl \
    wget \
    bash \
    bc \
    g++ \
    gcc \
    gnupg \
    libgcc \
    linux-headers \
    make \
    unzip \
    python \
    postgresql \
    postgresql-contrib \
    postgresql-libs \
    postgresql-dev \
    ca-certificates

RUN apk add --update --no-cache nodejs
RUN apk add --update --no-cache yarn

ENV SCALA_VERSION=2.12.8
ENV SCALA_HOME=/usr/share/scala
ENV SBT_VERSION=1.3.3

RUN cd "/tmp" && \
    wget "https://downloads.typesafe.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz" && \
    tar xzf "scala-${SCALA_VERSION}.tgz" && \
    mkdir "${SCALA_HOME}" && \
    rm "/tmp/scala-${SCALA_VERSION}/bin/"*.bat && \
    mv "/tmp/scala-${SCALA_VERSION}/bin" "/tmp/scala-${SCALA_VERSION}/lib" "${SCALA_HOME}" && \
    ln -s "${SCALA_HOME}/bin/"* "/usr/bin/" && \
    rm -rf "/tmp/"*

RUN update-ca-certificates && \
    curl -fsL https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz | tar xfz - -C /usr/local && \
    $(mv /usr/local/sbt-launcher-packaging-${SBT_VERSION} /usr/local/sbt || true) && \
    ln -s /usr/local/sbt/bin/* /usr/local/bin/

ENV PATH=/usr/local/bin:$PATH

RUN java -version && scala -version && scalac -version && sbt sbtVersion

ENV NODE_ENV=production
ENV POSTAL_ENV=production
ENV POSTAL_VERSION=0.1.2

RUN mkdir -p /app
WORKDIR /app
COPY . /app

RUN yarn install && yarn build
RUN sbt dist && \
    unzip -d dist target/universal/postal-${POSTAL_VERSION}.zip && \
    mv dist/postal-${POSTAL_VERSION} dist/postal-build && \
    rm -f dist/postal-build/bin/*.bat

EXPOSE 9000

ENTRYPOINT [ "./dist/postal-build/bin/postal" ]
CMD [ "-Dhttp.port=9000" ]
