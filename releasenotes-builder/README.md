# Release notes builder for Checkstyle

## Compile and package

Release notes builder uses Maven as a build tool. So, you can use standard maven commands
for compilation and packaging:

```
mvn clean compile package
```

Jar file which includes all required dependencies will be located at

```
/releasenotes-builder/target/
```

## Command line usage
```
java -jar releasenotes-builder-1.0-all.jar -localRepoPath <arg> -startRef <arg> [-endRef <arg>] \
     -releaseNumber <arg> [-outputLocation <args>] [-authToken <arg>] \
     [-generateAll] [-generateTw] [-generateRss] [-generateGplus] [-generateMlist]
```

Release notes builder will do the generation of release notes and report warnings and errors to
standard out in plain format.

Command line options are:

**localRepoPath** - path to the local git repository. For example,  ```/home/user/checkstyle``` .

**startRef** - specifies the commit reference or tag from which to start the generation of
release notes. For example, ```753bc06``` or just tag ```checkstyle-6.12.1``` .

**endRef** - (optional) specifies the number at which to end the generation of messages.
```HEAD``` reference will be used if not specified (by default).

**releaseNumber** - the number of current release. For example, ```6.13``` .

**outputLocation** - (optional) the name of an output folder. For example, ```/home/user/releasenotes/```
. Current folder will be used if not specified (by default).

**authToken** - (optional) the GitHub authentication access token to establish private connection to remote repository. For example, `0f95514f36200ebaadb8f28d8ba54300360a9e90` .

**generateTw** - (optional) generate a release notes post to publish on Twitter. Generated file will be ```twitter.txt```.

**generateRss** - (optional) generate a release notes post to publish on RSS. Generated file will be ```rss.txt```.

**generateGplus** - (optional) generate a release notes post to publish on Google plus. Generated file will be ```gplus.txt```.

**generateMlist** - (optional) generate a release notes post to publish on Mailing list. Generated file will be ```mailing_list.txt```.

**generateAll** - (optional) generate all possible posts. Generated files will be at specified output location.

--------------------

Command line usage example:

The following example demonstrates how to generate releasenotes.xml for checkstyle 6.13 release starting with checkstyle-6.12.1 release tag.

```
java -jar releasenotes-builder-1.0-all.jar -localRepoPath /home/user/checkstyle/ \
    -startRef checkstyle-6.12.1 -releaseNumber 6.13 -generateAll
```
