Add Qulice dependency to Checkstyle pom:
```
+       <dependency>
+               <groupId>com.qulice</groupId>
+           <artifactId>qulice-maven-plugin</artifactId>
+               <version>0.12.1</version>
+       </dependency>
```

We do that because we want to use Qulice's custom checks for Checkstyle.

Build and install Checkstyle:
```
mvn clean install -DskipTests -Dcobertura.skip=true -Dfindbugs.skip=true -Dpmd.skip=true
```

Launch command for testing Checkstyle project against Qulice's custom config:
```
./launch.sh -Dcheckstyle.config.location=qulice-checks.xml
```
