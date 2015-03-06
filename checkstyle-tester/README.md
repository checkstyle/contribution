Launch command for testing against google_checks.xml : 
"./launch.sh" - 

Launch command for testing against your custom config : 
"./launch.sh -Dcheckstyle.config.location=my_checks.xml"

Result Checkstyle report will appear at target folder (target/site/index.html). 

Please use '-Dcheckstyle.consoleOutput=true' option if you need to see all Checkstyle errors in console, but expect it to print very and very much lines of terminal logs in this case. Launch command in this case will be:

./launch.sh -Dcheckstyle.config.location=my_checks.xml -Dcheckstyle.consoleOutput=true
