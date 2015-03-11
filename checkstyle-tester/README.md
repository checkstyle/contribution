Launch command for testing against google_checks.xml: 
```
./launch.sh
```

Launch command for testing against your custom config: 
```
./launch.sh -Dcheckstyle.config.location=my_check.xml
```

Result Checkstyle report will appear at target folder (target/site/index.html). First run should be executed with all rules enabled to make sure that new check does not fail. You may see failure of `TreeWalker` but as long as it is applied for no-compilable sources (test, resources) you don't need to worry about it. Second run shall be done to prove that check is working correctly and for this one select the most accurate repositories.

Please use `-Dcheckstyle.consoleOutput=true` option if you need to see all Checkstyle errors in console, 
but expect it to print very and very much lines of terminal logs in this case. Launch command in this case will be:
```
./launch.sh -Dcheckstyle.config.location=my_check.xml -Dcheckstyle.consoleOutput=true
```

ATTENTION: If you want to validate new check from `sevntu.checkstyle`(https://github.com/sevntu-checkstyle/sevntu.checkstyle) project, 
before you use ".launch.sh" you need to clone it and deploy to your local maven repo by following command
```
[yourname@local sevntu.checkstyle]$ ./deploy.sh --maven
```

DEPLOY: to github pages repo (https://pages.github.com/) to share your report with other:

1) please follow instruction from https://pages.github.com/ to create your static web site on github

2) please copy whole "target/site" folder to newly created repo, do push the second run with the most proper repositories.

3) please make sure that report is available as http://YOURUSER.github.io/.

4) please make sure that at web site source links to violations are working as it is main part of report, just list of violations is not enough.
