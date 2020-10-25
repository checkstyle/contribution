# launch.groovy

Checkstyle report generation

**launch.groovy** is a script which allows you to generate Checkstyle report over target projects.
It invokes Maven Checkstyle plugin. In order to use the script you should run the following command
in your command line:

```bash
groovy launch.groovy --listOfProjects projects-to-test-on.properties --config my_check.xml
```

or with short command line arguments names:

```bash
groovy launch.groovy -l projects-to-test-on.properties -c my_check.xml
```

If you want to force Maven Checkstyle Plugin to ignore exceptions:

```bash
groovy launch.groovy \
  --listOfProjects projects-to-test-on.properties \
  --config my_check.xml --ignoreExceptions
```

or with short command line arguments names:

```bash
groovy launch.groovy -l projects-to-test-on.properties -c my_check.xml -i
```

The script receives the following command line arguments:

**listOfProjects** (l) - path to the file which contains the projects which sources
will be analyzed by Checkstyle during report generation (required);

**config** (c) - path to the file with Checkstyle configuration (required).
The default config should be changed in order to be appropriate for your use purposes;

**ignoreExceptions** (i) - whether Checkstyle Maven Plugin should ignore exceptions
(optional, default is false).

**ignoreExcludes** (g) - whether to ignore excludes specified in the list of projects
(optional, default is false).

**checkstyleVersion** (cv) - what version of Checkstyle to use
(optional, default the latest snapshot).

**sevntuVersion** (sv) - what version of Sevntu to use
(optional, default the latest release).

When the script finishes its work the following directory structure will be created
in the root of cehckstyle-tester directory:

*/repositories* - directory with downloaded projects sources which are specified
in projects-to-test-on.properties;

*/reports* - directory with Checkstyle reports.

*reports/{repository_name}/site* - directory with Checkstyle report generated
for the specific project (repository);

You will find *index.html* file in /reports/{repository_name}/site directory.
The file represents the main page of the report.

**First run** should be executed with all rules enabled to make sure that new check does not fail.
You may see failure of `TreeWalker` but as long as it is applied for no-compilable
sources (test, resources) you don't need to worry about it.
**Second run** shall be done to prove that check is working correctly and for this one select
the most accurate repositories.

Please use `-Dcheckstyle.consoleOutput=true` option if you need to see all
Checkstyle errors in console, but expect it to print very and very much lines of
terminal logs in this case. Launch command in this case will be:

```bash
groovy launch.groovy projects-to-test-on.properties my_check.xml -Dcheckstyle.consoleOutput=true
```

**Attention:** this project by default uses the latest SNAPSHOT version of [Checkstyle]
(https://github.com/checkstyle/contribution/search?utf8=%E2%9C%93&q=path%3Acheckstyle-tester+filename%3Apom.xml+%22checkstyle.version%22&type=)
and the latest released version of [sevntu.checkstyle]
(https://github.com/checkstyle/contribution/search?utf8=%E2%9C%93&q=path%3Acheckstyle-tester+filename%3Apom.xml+%22sevntu.checkstyle.version%22&type=).
If you need to use custom (snapshot) versions please update pom.xml to reference that versions
([checkstyle version]
(https://github.com/checkstyle/contribution/blob/35d35dfcc48e2022403231e41aac8bf96126acc9/checkstyle-tester/pom.xml#L15),
[sevntu.checkstyle version](https://github.com/checkstyle/contribution/blob/35d35dfcc48e2022403231e41aac8bf96126acc9/checkstyle-tester/pom.xml#L16)),
and please make sure that custom(newly generated) versions are located in your local maven repo

```bash
ls  ~/.m2/repository/com/puppycrawl/tools/checkstyle/
```

To build SNAPSHOT version of `checkstyle` please run in its folder (local git repository):

```bash
mvn clean install -Pno-validations
```

**Attention:**
Make sure that the versions of Checkstyle and SevNTU Checkstyle artifacts,
which are located in your local Maven repository, corresponds to the versions specified in pom.xml,
1) otherwise there will be the exception, because maven-checkstyle-plugin will not find
the required artifact in the local Maven repository;
2) you will not see any new/different violations from your changes, especially if you make changes
to version X with changed logic and pom.xml specifies version Y.
You will not get an exception message and unless you are expecting a specific difference,
you may just assume your report is correct when it really isn't.
launch.groovy has no way to protect against this as branch with change isn't specified.

If you want to validate new check from `sevntu.checkstyle`
(https://github.com/sevntu-checkstyle/sevntu.checkstyle) project, before you use
"launch.groovy" you need to clone it and deploy to your local maven repo by following command:

```bash
./deploy.sh --maven
```

WINDOWS USERS:

*./deploy.sh* can be luanched on Windows OS by usage [https://www.cygwin.com/](Cygwin).
Preinstall default commands - http://www.appveyor.com/updates/2015/05/30 search for
"Installation command used:" Follow example how we do this in Windows CI server -
https://github.com/checkstyle/checkstyle/blob/master/appveyor.yml#L71 search for
matrix item "checkstyle-tester on guava".
