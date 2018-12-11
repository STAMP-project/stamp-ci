# STAMP DSpot Jenkins plugin
[![Build Status](https://travis-ci.org/STAMP-project/dspot-jenkins-plugin.svg?branch=master)](https://travis-ci.org/STAMP-project/dspot-jenkins-plugin) 

The plugin is meant to run DSpot as a build step within Jenkins and visualize reports in the Jenkins UI.
Both freestyle jobs and pipelines are supported.

Developed in the context of [STAMP project](https://stamp.ow2.org/)

## Latest release:
v.1.0.1-SNAPSHOT (DSpot 1.2.2-SNAPSHOT)

## Install
* Install the plugin in Jenkins (for details on how to install a plugin see [here](https://jenkins.io/doc/book/managing/plugins/)).

* You can download the released hpi file or build from source with 

```
mvn package
```

## Configure

### Freestyle job
* Create a freestyle job that complies your tests
* Add a build action to run Dspot 

### Pipeline
add the `dspot` step in the build stage:
`dspot variable1: value1, ..., variable1: valueN`
see the table below for the variable list.
All variables are optional and default to the values in the table


### Reports Only
You can use the plugin only to visualize reports of DSpot in the Jenkins UI if DSpot was used as a Maven Plugin or by any other mean.
For this you can add the Post-build action `STAMP DSpot Reports` in freestyle jobs or the step with symbol `dspot-report`.
Note that the step must be in the same node where DSpot run.


## Options

### Plugin Options
| Option  | pipeline variable | Usage   | Default   |
| -------- | ------  | --------------------------------------------------- | ------- |
| `Run on changes`  | `onlyChanges` | 	Runs only on new or changed tests since the last build | `false`  |
| `Show reports`  | `showReports` | 	shows the DSpot reports in a visual format in the Jenkins UI | `false`  |

### Base DSPot Options
| Option  | pipeline variable | Usage   | Default   |
| -------- | ------  | --------------------------------------------------- | ------- |
| `Project Location`   |  `projectPath`  | 	path to the target project root from the folder where dspot is executed. | Defaults to Workspace |
| `Source location`    | `srcCode` |  path to the source code folder	| `src/main/java/` |
| `Tests location`  | `testCode` | 	path to the test source folder | `src/test/java/`  |
| `Source binary location`  | `srcClasses` |  path to the compiled code folder. (.class files) | `target/classes/`  |
| `Tests binary location`  | `testClasses` |	path to the compiled tests folder. (.class files) | `target/test-classes/`  |
| `Filter`  | `testFilter` |   filter on the package name containing tests to be amplified | all tests  |
| `Output directory` | `outputDir` |  path to the output folder | `dspot-out`  |


### Advanced Options
| Option  | pipeline variable | Usage   | Default   |
| -------- | ------  | --------------------------------------------------- | ------- |
| `Amplifiers`   |  `lAmplifiers`  | List of amplifiers to use | NONE |
| `Selector`    | `selector` | specify the test adequacy criterion to be maximized	| `PitMutantScoreSelector` |
| `Budgetizer`  | `budgetizer` | 	specify a Bugdetizer | `NoBudgetizer`  |
| `Number of Iterations`  | `numIterations` |  the number of amplification iterations | 3  |
| `Second Version path`    | `secondFolder` | Path to the root of the second version of the project. Must be specified when using `ChangeDetectorSelector`	| EMPTY |


For detailed information on the options have a look at [DSpot documentation](https://github.com/STAMP-project/dspot).

### Report  Options

| Option  | pipeline variable | Usage   | Default   |
| -------- | ------  | --------------------------------------------------- | ------- |
| `Output directory` | `outputDir` |  path to the output folder of Dspot results (relative to the workspace folder) | `dspot-out`  |

(these options will be ignored when the  `showReports` option is enabled in the Dspot build step)

## Use 
Build your project. 
DSpot will be run on the selected tests and the reports / amplified tests will be stored in the specified folder in your workspace.

## Graphical Reports 
A build dashboard is created to display STAMP DSpot reports.
From the dashboard the user can directly access the content of the output forlder of DSpot.

### Build views

For each build, a menu item is created to show the detailed Report of the DSpot run.

![Build dashboard](docs/img/build.PNG?raw=true "DSpot dashboard")
 
If test cases are successfully amplified, the user can navigate the details of the test class, getting more information on the amplified test cases.
Reports adapt based on the Selector used. 

![PIT selector](docs/img/mutant.PNG?raw=true "PIT Mutant selector view")
 
 
_Initial development by Engineering in the context of STAMP H2020 project_
