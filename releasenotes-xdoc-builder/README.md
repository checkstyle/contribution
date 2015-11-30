# Release notes xdoc builder for Checkstyle

## Compile and package

Release notes xdoc builder uses Maven as a build tool. So, you can use standart maven commands 
for compilation and packaging: 

```
mvn clean compile package
```

Compiled files will be located at

```
/releasenotes-xdoc-builder/target/classes/com/github/checkstyle/
``` 

Jar file which includes all required dependencies will be located at

```
/releasenotes-xdoc-builder/target/
```

## Command line usage
```
java -jar releasenotes-xdoc-builder-1.0-all.jar -localRepoPath <arg> -startRef <arg> [-endRef <arg>] \
     -releaseNumber <arg> [-outputFile <args>] [-authToken <arg>]
```

Release notes builder will do the generation of release notes and report warnings and errors to 
standard out in plain format.

Command line options are:

**localRepoPath** - path to the local git repository. For example,  ```/media/andreiselkin/checkstyle``` .

**startRef** - specifies the commit reference or tag from which to start the generation of 
release notes. For example, ```753bc06``` or just tag ```checkstyle-6.12.1``` .

**endRef** - (optional) specifies the number at which to end the generation of messages. 
```HEAD``` reference will be used if not specified (by default).

**releaseNumber** - the number of current release. For example, ```6.13``` .

**outputFile** - (optional) the name of an output file. For example, ```releasenotes_6.13.xml``` 
. ```releasenotes.xml``` name will be used if not specified (by default).

**authToken** - (optional) the GitHub authentication access token to establish private connection to remote repository. For example, `0f95514f36200ebaadb8f28d8ba54300360a9e90` .

--------------------

Command line usage example:

The following example demonstrates how to generate releasenotes.xml for checkstyle 6.13 release starting with checkstyle-6.12.1 release tag.

```
java -jar releasenotes-xdoc-builder-1.0-all.jar -localRepoPath /media/andreiselkin/checkstyle/ \
    -startRef checkstyle-6.12.1 -releaseNumber 6.13
```
