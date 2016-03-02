patch-diff-report-tool - for creating a symmetrical difference from two checkstyle-result.xml files. It is useful when you have two different large checkstyle XML reports and want to know difference between them. It deletes identical lines present in both XML files, merges remaining lines into a result site.

Compile source with `mvn clean install`, the application is named
`patch-diff-report-tool-0.1-SNAPSHOT-jar-with-dependencies.jar`.

usage example:

You have 2 different checkstyle repos, original and forked, for each of them do 1-4:

1. `mvn clean install` 
2. go to `checkstyle-tester` directory, uncomment all links in `projects-to-test-on.properties`, edit `my_check.xml`
3. execute `./launch.sh -Dcheckstyle.config.location=my_check.xml`
4. copy `checkstyle-result.xml` from `checkstyle-tester/target` to some other location.

Now execute this utility with 4 command line args:
```
--baseReportPath - path to the directory containing first checkstyle-result.xml;
--patchReportPath - path to the directory containing second checkstyle-result.xml;
--sourcePath - path to the source file under check;
--resultPath - path to the resulting site (default: ~/XMLDiffGen_report_yyyy.mm.dd_hh:mm:ss)
-h - simply shows help message.
```

Example:
`java -jar ./patch-diff-report-tool-0.1-SNAPSHOT-jar-with-dependencies.jar --baseReportPath ~/checkstyle-tester/location1 --patchReportPath ~/checkstyle-tester/location2 --sourcePath ~/checkstyle-tester/src/main/java --resultPath ~/checkstyle-tester/site_result`
