patch-diff-report-tool - for creating a symmetrical difference from two checkstyle-result.xml files. It is useful when you have two different large checkstyle XML reports and want to know difference between them. It deletes identical lines present in both XML files, merges residues into result site.

usage example:

You have 2 different checkstyle repos, original and forked, for each of them do 1-4:

    1. mvn clean install
    2. go to checkstyle-tester directory, uncomment all links in projects-to-test-on.properties, edit my_check.xml
    3. execute ./launch.sh -Dcheckstyle.config.location=my_check.xml
    4. copy checkstyle-result.xml from checkstyle-tester/target to some other location.

Now execute this utility with 4 command line args:

	    -baseReportPath - path to the directory containing first checkstyle-result.xml;
        -patchReportPath - path to the directory containing second checkstyle-result.xml;
        -sourcePath - path to the data under check (facultative, if absent then file structure for cross reference files won't be relativized, full paths will be used);
        -resultPath - path to the resulting site (facultative, if absent then default path will be used: ~/XMLDiffGen_report_yyyy.mm.dd_hh:mm:ss), remember, if this folder exists its content will be purged;
        -h - simply shows help message.
        i.e. java -jar ./patch-diff-report-tool.jar -baseReportPath ~/eclipse_workspace/tester-checkstyle/checkstyle-tester/location1 -patchReportPath ~/eclipse_workspace/tester-checkstyle/checkstyle-tester/location2 -sourcePath ~/eclipse_workspace/tester-checkstyle/checkstyle-tester/src/main/java -resultPath ~/eclipse_workspace/tester-checkstyle/checkstyle-tester/site_result