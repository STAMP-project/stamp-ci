# STAMP DSpot Jenkins plugin
[![Build Status](https://travis-ci.org/STAMP-project/dspot-jenkins-plugin.svg?branch=master)](https://travis-ci.org/STAMP-project/dspot-jenkins-plugin) 

The plugin is meant to run DSpot as a build step within a Jenkins Freestyle job. (Pipeline support to come soon).

Developed in the context of [STAMP project](https://stamp.ow2.org/)

* v.1.0.0
Dspot Jenkins Plugin

## Install
* Install the plugin in Jenkins (for details on how to install a plugin see [here](https://jenkins.io/doc/book/managing/plugins/)).

* You can download the released hpi file or build from source with 

```
mvn package
```

## Configure

* Create a freestyle job that complies your tests
* Add a build action to run Dspot 

### Options

| Option  | Usage   | Default                                            |
| -------- | ------  | --------------------------------------------------- |
| `Project Location`   | 	path to the target project root from the folder where dspot is executed.     | Defaults to Workspace|
| `Source location`    |  path to the source code folder	| `src/main/java/` |
| `Tests location`  | 	path to the test source folder | `src/test/java/`  |
| `Source binary location`  |  path to the compiled code folder. (.class files) | `target/classes/`  |
| `Tests binary location`  | 	path to the compiled tests folder. (.class files) | `target/test-classes/`  |
| `Filter`  |   filter on the package name containing tests to be amplified | all tests  |
| `Output directory`  |  path to the output folder | `dspot-out`  |
| `Run on changes`  | 	Runs only on new or changed tests since the last build | `false`  |

For detailed information on the options have a look at [DSpot documentation](https://github.com/STAMP-project/dspot).

## Use 
Build your project. 
DSpot will be run on the selected tests and the reports / amplified tests will be stored in the specified folder in your workspace.

_Initial development by Engineering_
