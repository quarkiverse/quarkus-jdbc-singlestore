# Quarkus JDBC Singlestore

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-2-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->
[![Build](https://github.com/quarkiverse/quarkus-jdbc-singlestore/workflows/Build/badge.svg)](https://github.com/quarkiverse/quarkus-jdbc-singlestore/actions?query=workflow%3ABuild)
[![Maven Central](https://img.shields.io/maven-central/v/io.quarkiverse.jdbc/quarkus-jdbc-singlestore.svg?label=Maven%20Central&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.jdbc/quarkus-jdbc-singlestore)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)

Quarkus Jdbc Singlestore is a Quarkus extension for the Singlestore jdbc driver.For more information about
the Singlestore, please refer [Singlestore](https://www.singlestore.com/)

## Compatibility

Each release is built and tested against one Quarkus version, one Java level, and one SingleStore JDBC client.

| Extension | Quarkus | Java | SingleStore JDBC |
| --- | --- | --- | --- |
| 1.6.0 | 3.40.1.2 | 21 | 1.2.12 |
| 1.5.3 | 3.33.4.1 | 21 | 1.2.12 |
| 1.5.0 – 1.5.2 | 3.33.2 – 3.33.4 | 17 | 1.2.11 – 1.2.12 |
| 1.4.0 | 3.32.4 | 17 | 1.2.9 |
| 1.3.0 – 1.3.3 | 3.27.0 – 3.27.6 | 17 | 1.2.8 – 1.2.9 |
| 1.2.0 | 3.25.1 | 17 | 1.2.8 |
| 1.1.0 | 3.20.0 | 17 | 1.2.8 |
| 1.0.3 | 3.19.3 | 17 | 1.2.7 |
| 1.0.0 – 1.0.2 | 3.15.1 | 17 | 1.2.5 |

The current source on `main` targets Quarkus 4.0.0, Java 21, and SingleStore JDBC 1.2.12. That line is not released yet.

## Getting Started
Add the following dependency in your pom.xml to get started,

```xml
<dependency>
    <groupId>io.quarkiverse.jdbc</groupId>
    <artifactId>quarkus-jdbc-singlestore</artifactId>
</dependency>
```

## User Documentation

https://docs.quarkiverse.io/quarkus-jdbc-singlestore/dev/index.html

## Contributors ✨

Thanks go to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://uk.linkedin.com/in/hchigadani"><img src="https://avatars.githubusercontent.com/u/12896715?v=4?s=100" width="100px;" alt="Hemantkumar Chigadani"/><br /><sub><b>Hemantkumar Chigadani</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-jdbc-singlestore/commits?author=Hemantkumar-Chigadani" title="Code">💻</a> <a href="#maintenance-Hemantkumar-Chigadani" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://melloware.com"><img src="https://avatars.githubusercontent.com/u/4399574?v=4?s=100" width="100px;" alt="Melloware"/><br /><sub><b>Melloware</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-jdbc-singlestore/pulls?q=is%3Apr+reviewed-by%3Amelloware" title="Reviewed Pull Requests">👀</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
