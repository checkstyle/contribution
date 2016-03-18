patch-diff-report-tool - for creating a symmetrical difference from two checkstyle-result.xml files. It is useful when you have two different large checkstyle XML reports and want to know difference between them. It deletes identical lines present in both XML files, merges remaining lines into a result site.

usage example:

1) Create local copy of checkstyle/contribution repository with command `git clone https://github.com/checkstyle/contribution`<br/>
2) `cd ./contribution/patch-diff-report-tool`<br/>
3) Compile source with `mvn clean install`, your application is located in `cd ./contribution/patch-diff-report-tool/target` and named `patch-diff-report-tool-0.1-SNAPSHOT-jar-with-dependencies.jar`, you may rename it and move to another folder.<br/>

You have 2 different checkstyle repos, original (base) and forked (patch), for each of them do 4-7:

4) go to repo folder and execute in console `mvn clean install` <br/>
5) go to `./contribution/checkstyle-tester` directory, uncomment all links in `projects-to-test-on.properties`, edit `my_check.xml`<br/>
6) execute `./launch.sh -Dcheckstyle.config.location=my_check.xml`<br/>
7) copy `checkstyle-result.xml` from `checkstyle-tester/target` to some other location.<br/>

8) Now execute this utility with 6 command line arguments:<br/>

`--baseReportPath` - path to the directory containing base checkstyle-result.xml;<br/>
`--patchReportPath` - path to the directory containing patch checkstyle-result.xml;<br/>
`--sourcePath` - path to the source file under check;<br/>
`--resultPath` - path to the resulting site (default: ~/XMLDiffGen_report_yyyy.mm.dd_hh:mm:ss)<br/>
`--baseConfigPath` - path to the base checkstyle configuration xml file;<br/>
`--patchConfigPath` - path to the patch checkstyle configuration xml file;<br/>
`-h` - shows help message.<br/>

Note that only two first arguments are obligatory.


Example:
`java -jar ./patch-diff-report-tool-0.1-SNAPSHOT-jar-with-dependencies.jar --baseReportPath ~/checkstyle-tester/location1 --patchReportPath ~/checkstyle-tester/location2 --sourcePath ~/checkstyle-tester/src/main/java --resultPath ~/checkstyle-tester/site_result --baseConfigPath ~/checkstyle-tester/my_check.xml --patchConfigPath ~/checkstyle-tester/my_other_check.xml`
