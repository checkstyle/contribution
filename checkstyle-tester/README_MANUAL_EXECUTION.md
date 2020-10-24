# Manual execution

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
