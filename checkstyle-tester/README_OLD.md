Launch command for testing against google_checks.xml and very [basic set of java projects](https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/projects-to-test-on.properties): 
```
./launch.sh
```

Launch command for testing against your custom config (one or few Checks): 
```
./launch.sh -Dcheckstyle.config.location=my_check.xml
```

Launch command for testing against all Checks to validate that there is no Exceptions (all validation warnings are ignored) : 
```
./launch.sh -Dcheckstyle.config.location=checks-nonjavadoc-error.xml
```

Attention: this project by deafult use released version of Checkstyle and sevntu.checkstyle
If you you need to use custom(snapshot) versons please update pom.xml to reference that versions
([checkstyle version](https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/pom.xml#L29),
 [sevntu.checkstyle version](https://github.com/checkstyle/contribution/blob/master/checkstyle-tester/pom.xml#L34)), 
and please make sure that
custom(newly generated) versions are located in your local maven repo 
```
ls  ~/.m2/repository/com/puppycrawl/tools/checkstyle/
ls  ~/.m2/repository/com/github/sevntu/checkstyle/sevntu-checkstyle-maven-plugin

```

to build SNAPSHOT version of `checkstyle` please run in his repo
```
mvn clean install -DskipTests -Djacoco.skip=true -Dspotbugs.skip=true -Dpmd.skip=true
```

If you want to validate new check from `sevntu.checkstyle`(https://github.com/sevntu-checkstyle/sevntu.checkstyle) project, 
before you use ".launch.sh" you need to clone it and deploy to your local maven repo by following command
```
./deploy.sh --maven
```

=============================================

Result Checkstyle report will appear at target folder (target/site/index.html). 
First run should be executed with all rules enabled to make sure that new check does not fail. 
You may see failure of `TreeWalker` but as long as it is applied for no-compilable sources (test, resources) 
you don't need to worry about it. 
Second run shall be done to prove that check is working correctly and for this one select the most accurate repositories.

Please use `-Dcheckstyle.consoleOutput=true` option if you need to see all Checkstyle errors in console, 
but expect it to print very and very much lines of terminal logs in this case. Launch command in this case will be:
```
./launch.sh -Dcheckstyle.config.location=my_check.xml -Dcheckstyle.consoleOutput=true
```

DIFF-REPORT:
to generate compact diff report before and after your change please use tool
[https://github.com/attatrol/ahsm](https://github.com/attatrol/ahsm)

ATTENTION: 

you can specify at projects-to-test-on.properties path to local file system if you have some 
repositories on your local, example: "checkstyle=/home/username/java/git-repos/checkstyle/checkstyle".

WINDOWS USERS:
this project can be luanched on Windows OS by usage [https://www.cygwin.com/](Cygwin).
Preinstall default commands - http://www.appveyor.com/updates/2015/05/30 search for "Installation command used:"
Follow example how we do this in Windows CI server - https://github.com/checkstyle/checkstyle/blob/master/appveyor.yml#L71 search for matrix item "checkstyle-tester on guava"

DEPLOY: 

to github pages repo (https://pages.github.com/) to share your report with other:

1) please follow instruction from https://pages.github.com/ to create your static web site on github

2) please copy whole "target/site" folder to newly created repo. Please rename target/­site/checkstyle.html to target/­site/index.html to let github.io open it by default.

3) please make sure that report is available as http://YOURUSER.github.io/.

4) please make sure that at web site source links to violations are working as it is main part of report, just list of violations is not enough.
