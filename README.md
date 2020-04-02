# Postal

> A security- and privacy-first, mobile-first, and offline-first mail service application

The Postal application is based on the AWS SES and S3 technologies/services in order to
create an email application. AWS SES stores encrypted MIME messages in a S3 bucket (messages
are checked by default to address spams and viruses); the Postal application obtains those
messages and handles it inside the application. Email messages are also sent through the
AWS SES infrastructure. The application also supports encrypting and decrypting messages.

The application is using Play + Java on the back-end and Vuejs + Vuex + TypeScript + LESS on
the front-end. It also uses a Vault scheme to store application configuration settings.

## Setup & Running

Please make sure to install Java 8+, Gradle, SBT, Node.js (Postal is developed over version
8.15.0), and Yarn. Also, you must create a `conf/secrets.conf` file according to the template
configuration in `conf/secrets.sample.conf` to connect to a Vault k/v secret. The secret
has the same format as the `conf/config.local.sample.json` file. You may create a
`conf/config.local.json` if you don't want to use the Vault solution, following the structure
defined by the template file (`conf/config.local.sample.json`). Once those requirements
are complete, you may run the following commands to start the development server:

```sh
$ yarn install && yarn build
$ sbt run
```

## Deployment using Docker

First, you must create a `conf/secrets.conf` file according to the template configuration in
`conf/secrets.sample.conf` to connect to a Hashicorp's Vault k/v secret. The secret has the same
format as the `conf/config.local.sample.json` file. You may create a `conf/config.local.json` if
you don't want to use the Vault solution, following the structure defined by the template file
(`conf/config.local.sample.json`). Once those requirements are complete, you may run the following
command to build and start the application through Docker containers:

```sh
$ binstubs/deploy.sh
```

It will create the PostgreSQL container and the Postal container (through `docker-compose`). The
application should be available at `http://{docker-hostname-or-ip}:8585`.

## Issues

Please take a look at [/issues](https://github.com/earaujoassis/postal/issues)

## License

[MIT License](http://earaujoassis.mit-license.org/) &copy; Ewerton Assis
