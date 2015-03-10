Launch command for testing against google_checks.xml: 
```
./launch.sh
```

Launch command for testing against your custom config: 
```
./launch.sh -Dcheckstyle.config.location=my_check.xml
```

Result Checkstyle report will appear at target folder (target/site/index.html). 

Please use '-Dcheckstyle.consoleOutput=true' option if you need to see all Checkstyle errors in console, 
but expect it to print very and very much lines of terminal logs in this case. Launch command in this case will be:
```
./launch.sh -Dcheckstyle.config.location=my_check.xml -Dcheckstyle.consoleOutput=true
```

ATTENTION: If you want to validate new check from `sevntu.checkstyle`(https://github.com/sevntu-checkstyle/sevntu.checkstyle) project, 
before you use ".launch.sh" you need to clone it and deploy to your local maven repo by following command
```
[yourname@local sevntu.checkstyle]$ ./deploy.sh --maven
```
