# patch-diff-report-tool

A tool for creating a symmetrical difference from two checkstyle-result.xml files.
It is useful when you have two different large checkstyle XML reports and want
to know difference between them. It deletes identical lines present in both XML files,
merges remaining lines into a result site.

usage example:

1) Create local copy of checkstyle/contribution repository with command
   `git clone https://github.com/checkstyle/contribution`
2) `cd ./contribution/patch-diff-report-tool`
3) Compile source with `mvn clean install`, your application is located
   in `cd ./contribution/patch-diff-report-tool/target` and
   named `patch-diff-report-tool-0.1-SNAPSHOT-jar-with-dependencies.jar`

You have 2 different checkstyle repos, original (base) and forked (patch), for each of them do 4-7:

4) go to repo folder and execute in console `mvn clean install`
5) go to `./contribution/checkstyle-tester` directory, uncomment all/required lines
   in `projects-to-test-on.properties`, edit  `my_check.xml`
6) execute `groovy launch.groovy --listOfProjects projects-to-test-on.properties
  --config my_check.xml --ignoreExceptions`
7) copy `checkstyle-result.xml` from `checkstyle-tester/reports/project-name/`
   to some other location.

8) Now execute this utility with 6 command line arguments:

`--baseReport` - path to the base checkstyle-result.xml (optional argument,
   if absent then only configuration and violations for patch will be in the report); \
`--patchReport` - path to the patch checkstyle-result.xml (required argument); \
`--refFiles` - path to the source files under check (optional argument); \
`--output` - path to store the resulting diff report (optional argument,
   default: ~/XMLDiffGen_report_yyyy.mm.dd_hh_mm_ss) \
`--baseConfig` - path to the base checkstyle configuration xml file (optional argument); \
`--patchConfig` - path to the patch checkstyle configuration xml file (optional argument); \
`--shortFilePaths` - Option to save report file paths as a shorter version to prevent long paths.
   This option is useful for Windows users where they are restricted to maximum directory depth. \
`-h` - shows help message.

Example:

```bash
java -jar ./patch-diff-report-tool-0.1-SNAPSHOT-jar-with-dependencies.jar \
  --baseReport ~/contribution/checkstyle-tester/location1/checkstyle-result.xml \
  --patchReport ~/contribution/checkstyle-tester/location2/checkstyle-result.xml \
  --refFiles ~/contribution/checkstyle-tester/src/main/java \
  --output ~/contribution/checkstyle-tester/site_result \
  --baseConfig ~/contribution/checkstyle-tester/my_check.xml \
  --patchConfig ~/contribution/checkstyle-tester/my_other_check.xml
```
