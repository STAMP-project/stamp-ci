# stamp-cicd-utils
STAMP ci/cd tools library: utilitary classes for miscellaneous STAMP needs.
Includes:
* Gitlab and Git stuff (based on [Gitlab4j](https://github.com/gmessner/gitlab4j-api) and [Eclipse jGit](https://www.eclipse.org/jgit/))
* Tools related to STAMP [Botsing](https://github.com/STAMP-project/botsing), including a Botsing maven invoker and an exceptions parser/extractor class.
* Miscellaneous utilities.

This project is intended for leveraging STAMP development, providing building blocks that can be used to provide STAMP-related services (eg. a Botsing Gitlab web hook, servlet-based STAMP services...).

Build: mvn clean install

Project can be used by adding the following dependency to your pom.xml:

```xml
  <dependency>
     <groupId>eu.stamp-project</groupId>
     <artifactId>stamp-cicd-utils</artifactId>
     <version>1.0.0-SNAPSHOT</version>
  </dependency>
```

Javadoc is available [here](https://stamp-project.github.io/stamp-cicd-utils/apidocs/index.html).
