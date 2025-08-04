# mod-mosaic
Copyright (C) 2025 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

This software uses a copyleft (LGPL-2.1-or-later) licensed software library: org.hibernate.orm:hibernate-core

## Table of contents

* [Introduction](#introduction)
* [Additional Information](#additional-information)
  * [Issue tracker](#issue-tracker)
  * [API Documentation](#api-documentation)
  * [Module Documentation](#module-documentation)
  * [Code analysis](#code-analysis)
  * [Download and configuration](#download-and-configuration)
  * [Development tips](#development-tips)


## Introduction

This module provides a functionality to integrate with Mosaic ordering system.

## Installing and deployment

### Compiling

```shell
mvn install
```

See that it says "BUILD SUCCESS" near the end.


### Running it

Run locally with proper environment variables set (see
[Environment variables](#environment-variables) below) on listening port 8081 (default
listening port):

```
java -Dserver.port=8081 -jar target/mod-mosaic-*.jar
```

### Docker

Build the docker container with:

```shell
docker build -t mod-mosaic .
```

Test that it runs with:

```shell
docker run -t -i -p 8081:8081 mod-mosaic
```

### Environment variables

| Name          | Default value | Description                                                                                                                                                                   |
|:--------------|:-------------:|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DB_HOST       |   postgres    | Postgres hostname                                                                                                                                                             |
| DB_PORT       |     5432      | Postgres port                                                                                                                                                                 |
| DB_USERNAME   |  folio_admin  | Postgres username                                                                                                                                                             |
| DB_PASSWORD   |       -       | Postgres username password                                                                                                                                                    |
| DB_DATABASE   | okapi_modules | Postgres database name                                                                                                                                                        |
| OKAPI_URL     |       -       | Okapi url                                                                                                                                                                     |
| ENV           |     folio     | The logical name of the deployment, must be unique across all environments using the same shared Kafka/Elasticsearch clusters, a-z (any case), 0-9, -, _ symbols only allowed |

## Additional Information
### Issue tracker

See project [MODMO](https://issues.folio.org/browse/MODMO)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker/).

### API Documentation

This module's [API documentation](https://dev.folio.org/reference/api/#mod-mosaic).

### Code analysis

[SonarQube analysis](https://sonarcloud.io/dashboard?id=org.folio%3Amod-mosaic).

### Download and configuration

The built artifacts for this module are available.
See [configuration](https://dev.folio.org/download/artifacts) for repository access,
and the [Docker image](https://hub.docker.com/r/folioorg/mod-mosaic/)
