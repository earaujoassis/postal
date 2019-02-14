# Postal

> A security- and privacy-first, mobile-first, and offline-first mail service application

The Postal application is based on the AWS SES and S3 technologies/services in order to
create an email application. AWS SES stores encrypted MIME messages in a S3 bucket (messages
are checked by default to address spams and viruses); the Postal application obtains those
messages and handles it inside the application. Email messages are also sent through the
AWS SES infrastructure. The application also supports encrypting and decrypting messages.

The application is using Play + Java on the back-end and Less + vanilla TypeScript on the front-end.

## Setup & Running

Please make sure to install Java 8, Gradle and SBT. Also, you must create a `conf/secrets.conf`
file according to the template configuration in `conf/secrets.sample.conf`. Once that is done,
you may run `sbt run` to start the development server.

## Issues

Please take a look at [/issues](https://github.com/earaujoassis/postal/issues)

## License

[MIT License](http://earaujoassis.mit-license.org/) &copy; Ewerton Assis
