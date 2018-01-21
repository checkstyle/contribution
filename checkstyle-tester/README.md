# CHECKSTYLE-TESTER

checkstyle-tester is a tool for Checkstyle report generation over very [basic set of java projects](https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/projects-to-test-on.properties).
It consists of one Groovy script: diff.groovy. Thus, in order to use the tool make sure you have the Groovy runtime installed on your developer machine (min required version is 2.4.8).

Content:

- [Diff report generation](https://github.com/checkstyle/contribution/tree/master/checkstyle-tester#diffgroovy-diff-report-generation)
- [Deploy Report](https://github.com/checkstyle/contribution/tree/master/checkstyle-tester#deploy-report)

## [diff.groovy] Diff report generation

In order to generate a compact diff report before and/or after your changes you can use diff.groovy script which performs all required work automatically. Note, diff.groovy ignores excludes specified in the list of projects file.
Please execute the following command in your command line to run diff.groovy:

```
groovy diff.groovy --localGitRepo /home/johndoe/projects/checkstyle --baseBranch master --patchBranch i111-my-fix --config my_check.xml --listOfProjects projects-to-test-on.properties
```

or with short command line arguments names:

```
groovy diff.groovy -r /home/johndoe/projects/checkstyle -b master -p i111-my-fix -c my_check.xml -l projects-to-test-on.properties
```

If you want to specify different Checkstyle configs for base branch and patch branch use the following command:

```
groovy diff.groovy --localGitRepo /home/johndoe/projects/checkstyle --baseBranch master --patchBranch i111-my-fix --baseConfig base_config.xml --patchConfig patch_config.xml --listOfProjects projects-to-test-on.properties
```

or with short command line arguments names:

```
groovy diff.groovy -r /home/johndoe/projects/checkstyle -b master -p i111-my-fix -bc base_config.xml -pc patch_config.xml -l projects-to-test-on.properties
```

To generate the report only for the patch branch which contains your changes, use the following command:

```
groovy diff.groovy --localGitRepo /home/johndoe/projects/checkstyle --patchBranch i111-my-fix --patchConfig patch_config.xml --listOfProjects projects-to-test-on.properties --mode single
```

or with short command line arguments names:

```
groovy diff.groovy -r /home/johndoe/projects/checkstyle -p i111-my-fix -pc patch_config.xml -l projects-to-test-on.properties -m single
```

The script receives the following set of command line arguments:

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

When the script finishes its work the following directory structure will be created in the root of cehckstyle-tester directory:

*/repositories* - directory with downloaded projects sources which are specified in projects-to-test-on.properties;

*/reports/diff* - directory with diff reports;

*reports/baseBranch* - directory with Checkstyle reports which are generated with Checkstyle base version (based on specified base branch. If the mode is 'single', then the directory will not be created.);

*reports/patchBranch* - directory with Checkstyle reports which are generated with Checkstyle version that contains your changes (based on specified patch branch).

You will find *index.html* file in /reports/diff directory. The file represents summary diff report.

To generate a diff report before and after your changes manually please use the tool
[https://github.com/attatrol/ahsm](https://github.com/attatrol/ahsm)

ATTENTION: 

you can specify at projects-to-test-on.properties path to local file system if you have some 
repositories on your local, example: "checkstyle=/home/username/java/git-repos/checkstyle/checkstyle".

## Testing sevntu checks:
build sevntu checks and sevntu maven plugin by:
```
cd sevntu-checks
mvn  -Pno-validations clean install
cd ../sevntu-checkstyle-maven-plugin/
mvn clean install
``` 
Sevntu it is already referenced at https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/pom.xml#L16 .
Change config file to reference your  Check - https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/my_check.xml#L22 .
Run checkstyle-tester as described in README.

## Deploy Report: 

The created report can be deployed to github pages repo (https://pages.github.com/) to share with others:

1) please follow instruction from https://pages.github.com/ to create your static web site on github;

2) please copy the whole "reports/diff" (if you use diff.groovy script) or "reports" folder (if you use only launch.groovy) to newly created repo;

3) please make sure that report is available as http://YOURUSER.github.io/ ;

4) please make sure that at web site source links to violations are working as it is main part of report, just list of violations is not enough.
