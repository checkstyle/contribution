patch-diff-report-tool - for creating a symmetrical difference from two checkstyle-result.xml files. It is useful when you have two different large checkstyle XML reports and want to know difference between them. It deletes identical lines present in both XML files, merges remaining lines into a result site.

usage example:

1) Create local copy of checkstyle/contribution repository with command `git clone https://github.com/checkstyle/contribution`<br/>
2) `cd ./contribution/patch-diff-report-tool`<br/>
3) Compile source with `mvn clean install`, your application is located in `cd ./contribution/patch-diff-report-tool/target` and named `patch-diff-report-tool-0.1-SNAPSHOT-jar-with-dependencies.jar`<br/>

You have 2 different checkstyle repos, original (base) and forked (patch), for each of them do 4-7:

4) go to repo folder and execute in console `mvn clean install` <br/>
5) go to `./contribution/checkstyle-tester` directory, uncomment all links in `projects-to-test-on.properties`, edit `my_check.xml`<br/>
6) execute `./launch.sh -Dcheckstyle.config.location=my_check.xml`<br/>
7) copy `checkstyle-result.xml` from `checkstyle-tester/target` to some other location.<br/>

8) Now execute this utility with 6 command line arguments:<br/>

`--baseReport` - path to the base checkstyle-result.xml (required argument);<br/>
`--patchReport` - path to the patch checkstyle-result.xml (required argument);<br/>
`--refFiles` - path to the source file under check (optional argument);<br/>
`--output` - path to the resulting site (optional argument, default: ~/XMLDiffGen_report_yyyy.mm.dd_hh:mm:ss)<br/>
`--baseConfig` - path to the base checkstyle configuration xml file (optional argument);<br/>
`--patchConfig` - path to the patch checkstyle configuration xml file (optional argument);<br/>
`-h` - shows help message.<br/>



Example:
`java -jar ./patch-diff-report-tool-0.1-SNAPSHOT-jar-with-dependencies.jar --baseReportPath ~/checkstyle-tester/location1 --patchReportPath ~/checkstyle-tester/location2 --sourcePath ~/checkstyle-tester/src/main/java --resultPath ~/checkstyle-tester/site_result --baseConfigPath ~/checkstyle-tester/my_check.xml --patchConfigPath ~/checkstyle-tester/my_other_check.xml`
