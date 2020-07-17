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

## Report generation

### Executing generation

You can generate report in different ways:
1) generate report yourself: [manual generation](./README_MANUAL_EXECUTION.md#executing-diffgroovy)
2) generate report using github action.

To generate report using github action, you need to specify the URI for the 'raw' version of your configuration and projects-to-test-on.properties files in the description for the pull request. URI must be specified in the following format:

Diff Regression projects: {{URI to projects-to-test-on.properties}}

Diff Regression config: {{URI to my_checks.xml}}

Examples of URIs:
- https://raw.githubusercontent.com/checkstyle/contribution/master/checkstyle-tester/projects-to-test-on.properties
- https://raw.githubusercontent.com/checkstyle/contribution/master/checkstyle-tester/my_check.xml

After that, you need to create specific comment to generate the desired report:
- comment 'diff report' will start generating [Basic Difference Report](./README_MANUAL_EXECUTION.md#basic-difference-report) (for fix bugs);
- comment 'single report' will start generating [Basic Single Report](./README_MANUAL_EXECUTION.md#basic-single-report) (for new checks).

If you want to generate [Difference Report with Different Base and Patch Config](./README_MANUAL_EXECUTION.md#difference-report-with-different-base-and-patch-config) (for split properties, change property types, add a new property, etc...), you need to add one more URI to the pull request description. This URI must refer to patch config. The additional URI format should be like this:

Diff Regression patch config: {{URI to patch_config.xml}}

Then create comment 'diff report'. The Github action will use two config files to generate difference report.

### Generation examples

#### Basic Difference Report and Basic Single Report

![Alt text](./screenshots/diff_report_example1.png?raw=true "Basic reports")

#### Difference Report with Different Base and Patch Config

![Alt text](./screenshots/diff_report_example2.png?raw=true "Report with Different Base and Patch Config")

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

## Testing ANTLR Grammar Changes

After modifying Checkstyle's ANTLR grammar, it is neccessary to compare how Checkstyle's checks are affected by your changes.  This is done by comparing the check violation output of your modified branch of Checkstyle against Checkstyle's master branch, and comparing the abstract syntax trees of both branches. 

#### Check Regression Report

To generate the check difference report, you must separate the modules and settings in the [`checkstyle_checks.xml`](https://github.com/checkstyle/checkstyle/blob/master/config/checkstyle_checks.xml) configuration file, found in the checkstyle repository, into separate configuration files (this helps avoid 'out of memory' errors).  The best way to separate the checks is to determine which typically produce the most violations, such as Indentation and WhiteSpace Checks, and put them each into their own configuration file, then separating the rest into files with roughly ten checks each.

You may modify all the checks that depend on external files to use default settings.  **For each of the configuration files, you should use the [`my_check.xml`](https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/my_check.xml) file as a base**, and add the checks from `checkstyle_checks.xml` to it. Then `diff.groovy` should be run on all projects in `projects-to-test-on.properties`, using the `diff.groovy` script once for each configuration file.  [See instructions above](https://github.com/checkstyle/contribution/tree/master/checkstyle-tester#basic-difference-report) for "Basic Difference Report".

#### ANTLR Regression Report

 This report is generated using the [`launch_diff_antlr.sh`](https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/launch_diff_antlr.sh) script.  This script generates a report based on the differences in the ASTs generated from your PR branch and Checkstyle's 'main' branch using projects that are selected (uncommented) in the [`projects-to-test-on.properties`](https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/projects-to-test-on.properties) file. For the ANTLR regression report, we usually only want to see that changes to the Checkstyle project. To ensure that you test against any new inputs that you have created (unit test inputs, etc.), please make sure that you comment out all other projects, and add the following line to [`projects-to-test-on.properties`](https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/projects-to-test-on.properties):
 
 `my-checkstyle|git|https://github.com/<username>/checkstyle.git|<pr-branch>||`

  Where `<username>` and `<pr-branch>` are your github username and the branch that your Checkstyle PR is based on, respectively. To use [`launch_diff_antlr.sh`](https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/launch_diff_antlr.sh), you must modify the [`launch_diff_variables.sh`](https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/launch_diff_variables.sh) file to reflect the location of the variables that  [`launch_diff_antlr.sh`](https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/launch_diff_antlr.sh) uses to produce the report. Please note that `PULL_REMOTE` will need to be set to the name of the remote repository where your branch resides. If your branch is found at `origin/name-of-your-branch-here`, you will set `PULL_REMOTE=origin`. Then, run:
  
  `./launch_diff_antlr.sh name-of-your-branch-here`.

*Note: if you are experiencing maven 'out of memory' errors from maven, see https://cwiki.apache.org/confluence/display/MAVEN/OutOfMemoryError*


## Troubleshooting

To generate a report in debug mode, use the `MAVEN_OPTS` environment variable:
```
export MAVEN_OPTS=-Dorg.slf4j.simpleLogger.defaultLogLevel=debug
groovy diff.groovy --localGitRepo ...

```
Windows users should use the `SET` command instead of the `export` command.
