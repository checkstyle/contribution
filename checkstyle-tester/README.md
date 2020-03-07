# CHECKSTYLE-TESTER

checkstyle-tester is a tool for Checkstyle report generation over very [basic set of external projects](https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/projects-to-test-on.properties).
checkstyle-tester generates reports from violations printed from a supplied user config over the aforementioned projects. The generated reports are in HTML format. They can be viewed in any browser and can be posted online for others to view.
Only 2 types of reports are currently supported, single and diff (short for difference). Single mode lets you turn violations from a project into a web version report that links to the code that generated the violations. Diff mode shows the differences between 2 different versions of checkstyle using the same or different configurations.

## Setup

The contribution repository must be cloned and all the folders must be kept together, like they are on the server. Not only does the tool use the `checkstyle-tester` directory and it's layout, but it also uses `patch-diff-report-tool` to help with building the final report.
The tool consists of one Groovy script: `diff.groovy`. Thus, in order to use the tool make sure you have the Groovy runtime installed on your developer machine (min required version is 2.4.8).
The tool runs Checkstyle through maven and makes use of your checkstyle branches, so maven and git is required to be installed as well.
Depending on the type of external projects you wish to generate reports for, you may require other tools like Git or Mericural, for Git and HG repositories respectively.

## Command Line Arguments

`diff.groovy` supports the following command line arguments:

**localGitRepo** (r) - path to the local Checkstyle repository (required);

**baseBranch** (b) - name of the base branch in local Checkstyle repository (optional, if asent, then the tool will use only patchBranch in case the tool mode is 'single', otherwise baseBrach will be set to 'master');

**mode** (m) - the mode of the tool: 'single' or 'diff' (optional, default is 'diff'. Set this option to 'single' if your patch branch contains changes for any option that can't work on master/base branch. 
For example, for new properties, new tokens, or new modules. For all other changes, 'diff' mode should be the preferred mode used. Note, that if the mode is 'single', then 'baseBranch', 'baseConfig', and 'config' should not be specified as the tool will finish the execution with the error.
You must specify 'patchBranch' and 'patchConfig' if the mode is 'single', and 'baseBranch', 'baseConfig', 'patchBranch', and 'patchConfig' if the mode is 'diff');

**patchBranch** (p) - name of the branch with your changes (required);

**baseConfig** (bc) - path to the base checkstyle configuration file. It will be applied to base branch (required if patchConfig is specified);

**patchConfig** (pc) - path to the patch checkstyle config file. It will be applied to patch branch (required if baseConfig is specified);

**config** (c) - path to the checkstyle config file. It will be applied to base and patch branches (required if baseConfig and patchConfig are not secified). The default config should be changed in order to be appropriate for your use purposes;

**listOfProjects** (l) - path to the file which contains the projects which sources will be analyzed by Checkstyle during report generation.

**shortFilePaths** (s) - whether to save report file paths as a shorter version to prevent long paths. This option is useful for Windows users where they are restricted to maximum directory depth (optional, default is false).

## Outputs

When the script finishes its work the following directory structure will be created in the root of checkstyle-tester directory:

*/repositories* - directory with downloaded projects sources which are specified in projects-to-test-on.properties;

*/reports/diff* - directory with diff reports;

*reports/baseBranch* - directory with Checkstyle reports which are generated with Checkstyle base version (based on specified base branch. If the mode is 'single', then the directory will not be created.);

*reports/patchBranch* - directory with Checkstyle reports which are generated with Checkstyle version that contains your changes (based on specified patch branch).

You will find *index.html* file in /reports/diff directory. The file represents the summary report and will link to each individual project with an overview of the number of violations.

## Preparation before Executing

Before you are ready to execute `diff.groovy`, you will have to prepare some external files and branches first.

### projects-to-test-on.properties

`projects-to-test-on.properties` lists all the projects that `diff.groovy` will execute. Anything that starts with `#` is considered a comment and is ignored.
`projects-to-test-on.properties` is expected to be in the following format:

> REPO_NAME|[local|git|hg]|URL|[COMMIT_ID]|[EXCLUDE FOLDERS]

You should modify `projects-to-test-on.properties` and test as many projects as possible. Each project has its own unique style and it is common to find new and different violations in 1 and not the others.

You can also specify projects that are already available on your local file system in `projects-to-test-on.properties`.
For this you can either use `git` or `hg` type which will clone the local repository into the workspace and use the specified branch.
Alternatively you can use `local` type, where the specified branch is ignored and the current state on the disk is used as is.
The latter does not depend on any specific version control being used.
See the following examples:

```
my_custom_project|local|/home/username/java/my_custom_project||
my_custom_checkstyle|git|/home/username/java/git-repos/checkstyle/checkstyle|master|**/ignoreFolderOrFiles/**/*
my_custom_repo|hg|/home/username/java/hg-repos/myRepo|default|
```

### Configuration File

You will need to modify your configuration file that you wish to generate a report for. If you are doing diff mode, you can specify 2 configuration files if a new property or value is being introduced.

**Note:** You can use `my_check.xml` as a base as it provides most of the necessary elements already added, but you are still required to customize it to what you are specifically building a report for. 

#### Special Configuration Additions

There are a few ways to modify the configuration file to generate special reports.

If you wish to generate an exception only report, where only exceptions are visible and violations are hidden, you can change the severity of your module to `ignore`. Exceptions are always reported with a severity of `error` which can't be turned off.
Example:
```
<module name="FinalLocalVariable">
    <property name="severity" value="ignore"/>
</module>
```

If you wish to ignore specific cases from a report, you can use `SuppressionSingleFilter` or `SuppressionXpathSingleFilter` to hide them.
Example:
```
    <module name="SuppressionSingleFilter">
      <property name="message" value="Exception occurred while parsing"/>
      <property name="checks" value="Checker"/>
    </module>
```

##### Javadoc Regression

Many javadoc comments found in other projects contain various errors since Checkstyle's Javadoc parser is slightly more strict. To avoid polluting the report with all these parsing errors, it is recommended to add and keep suppressions from `my_checks.xml` in your own config when working with the Javadoc checks.

Example:
```
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
  "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
  "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
  <property name="haltOnException" value="false"/>
  <module name="TreeWalker">
    <module name="SummaryJavadoc" />

    <!-- suppress javadoc parsing errors -->
    <module name="SuppressionXpathSingleFilter">
      <property name="message" value="Javadoc comment at column \d+ has parse error"/>
    </module>
  </module>
  <!-- suppress java parsing errors -->
  <module name="SuppressionSingleFilter">
    <property name="message" value="Exception occurred while parsing"/>
    <property name="checks" value="Checker"/>
  </module>
</module>
```

## Executing diff.groovy

`diff.groovy` is executed by calling groovy and passing in the command line arguments that specify the report you want generated.

`groovy diff.groovy`

### Examples

#### Basic Difference Report

`groovy diff.groovy --localGitRepo /home/johndoe/projects/checkstyle --baseBranch master --patchBranch i111-my-fix --config my_check.xml --listOfProjects projects-to-test-on.properties`

or with short command line arguments names:

`groovy diff.groovy -r /home/johndoe/projects/checkstyle -b master -p i111-my-fix -c my_check.xml -l projects-to-test-on.properties`

#### Difference Report with Different Base and Patch Config

If you want to specify different Checkstyle configs for base branch and patch branch use the following command:

`groovy diff.groovy --localGitRepo /home/johndoe/projects/checkstyle --baseBranch master --patchBranch i111-my-fix --baseConfig base_config.xml --patchConfig patch_config.xml --listOfProjects projects-to-test-on.properties`

or with short command line arguments names:

`groovy diff.groovy -r /home/johndoe/projects/checkstyle -b master -p i111-my-fix -bc base_config.xml -pc patch_config.xml -l projects-to-test-on.properties`

#### Basic Single Report

To generate the report only for the patch branch which contains your changes, use the following command:

`groovy diff.groovy --localGitRepo /home/johndoe/projects/checkstyle --patchBranch i111-my-fix --patchConfig patch_config.xml --listOfProjects projects-to-test-on.properties --mode single`

or with short command line arguments names:

`groovy diff.groovy -r /home/johndoe/projects/checkstyle -p i111-my-fix -pc patch_config.xml -l projects-to-test-on.properties -m single`

## Deploying Report

The created report can be deployed to github pages repo (https://pages.github.com/) or your own private web server to share with others.

The following instructions are how to deploy to github pages repo.

1) please follow instruction from https://pages.github.com/ to create your static web site on github;

2) please copy the whole "reports/diff" folder to the newly created repo;

3) please make sure that report is available as http://YOURUSER.github.io/ ;

4) please make sure that at web site source links to violations are working as it is main part of report, just list of violations is not clear for most cases.

If you are required to create multiple reports, you should deploy each one to their own sub-directory.

## Checkstyle pitest Regression

Main checkstyle project uses pitest to help remove unnecessary code and ensure all lines are fully tested when introduced to the project. Sometimes is extremely hard to resolve pitest and fix all mutations. In this case, diff report regression can help find and kill mutations. It is not always a guarantee that it can help, especially if the code can truly never be hit, but it helps to show that removing the code has no noticeable impact on the functionality of the check.

First, you must create a config that has the check using as many permutations of customizable options as possible. It is best to give each permutation a unique **id** property to be used in the final report to identify which specific configuration instance created the difference. The following is an example:

````
<module name="JavadocMethod">
  <property name="id" value="JavadocMethod1" />
</module>
<module name="JavadocMethod">
  <property name="validateThrows" value="true" />
  <property name="id" value="JavadocMethod2" />
</module>
````

Next you must create a new branch off the PR branch which must be modified to embed the applied mutation into the code. If your surviving mutation is `replaced equality check with true → SURVIVED`, then you must physically change the code in the new branch and replace the original condition with `true`. You can ensure you changed the code correctly because the test suite will still pass with this change in place.

With all that done, you can now call groovy. **baseBranch** will be your PR branch that has the failing pitest mutations. **patchBranch** must be the new branch you have created off the PR branch mentioned in the previous paragraph. **config** must be the custom configuration created with all the permutations. 

`groovy diff.groovy --localGitRepo /home/johndoe/projects/checkstyle --baseBranch i111-my-fix --patchBranch i111-my-fix-mutation --config config.xml --listOfProjects projects-to-test-on.properties`

ATTENTION: 

It is recommended to only do 1 mutation per module for a single regression. Too many mutations at once could cancel each other out and falsely produce no differences.

It is possible mutating the code in this way will cause unpredictable and even random results where you won't be able to reproduce a case locally. Checkstyle doesn't guarantee order of events so hash-based classes like `HashSet` and `HashMap` could interfere.

Even if the regression proves no differences, it may be a false that there is no way to kill the mutation. Regression is only based on sources others have made and may just mean that this form of code is uncommon. If regression fails to find a difference, you must analyze the code manually and see if there is a way to determine if it is logically impossible to hit the code and kill the mutation.

## Testing Sevntu

`diff.groovy` script does not currently support generating reports for sevntu. Sevntu can only be run with patch only branch and config. Running a full difference report will always produce no results because the scripts do no install the different versions of sevntu needed to function.

First you must build sevntu checks:
```
cd sevntu-checks
mvn  -Pno-validations clean install
``` 
Sevntu's current version must be referenced at https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/pom.xml#L16 .

Finally, just run checkstyle-tester as described above.
