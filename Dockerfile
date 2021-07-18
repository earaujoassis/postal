# FROM Dockerfile.openjdk-sbt
ARG BASE_IMAGE_NAME
FROM ${BASE_IMAGE_NAME}

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

USER 1010
ENTRYPOINT [ "./dist/postal-build/bin/postal" ]
CMD [ "-Dhttp.port=9000" ]
