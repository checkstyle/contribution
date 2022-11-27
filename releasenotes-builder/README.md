# Release notes builder for Checkstyle

## Compile and package

Release notes builder uses Maven as a build tool. So, you can use standard maven commands
for compilation and packaging:

```bash
mvn clean compile package
```

Jar file which includes all required dependencies will be located at

```bash
/releasenotes-builder/target/
```

## Command line usage

```bash
java -jar releasenotes-builder-1.0-all.jar -localRepoPath <arg> \
     -remoteRepoPath <arg> -startRef <arg> [-endRef <arg>] \
     -releaseNumber <arg> [-outputLocation <args>] [-githubAuthToken <arg>] \
     [-generateAll] [-generateXdoc] [-generateTwit]
     [-generateGitHub] \
     [-xdocTemplate] [-twitterTemplate] [-gitHubTemplate] \
     [-publishAllSocial] [-publishTwit] [-twitterConsumerKey <arg>]
     [-twitterConsumerSecret <arg>] \
     [-twitterAccessToken <arg>] [-twitterAccessTokenSecret <arg>] [-twitterProperties <arg>] \
     [-validateVersion]
```

Release notes builder will do the generation of release notes and report warnings and errors to
standard out in plain format.

Command line options are:

**localRepoPath** - path to the local git repository. For example,  ```/home/user/checkstyle``` .

**remoteRepoPath** - path to the remote GitHub repository. For example, ```checkstyle/checkstyle``` .

**startRef** - specifies the commit reference or tag from which to start the generation of
release notes. For example, ```753bc06``` or just tag ```checkstyle-6.12.1``` .

**endRef** - (optional) specifies the number at which to end the generation of messages. ```HEAD```
reference
will be used if not specified (by default).

**releaseNumber** - the number of current release. For example, ```6.13``` .

**outputLocation** - (optional) the name of an output folder.
For example, ```/home/user/releasenotes/```
. Current folder will be used if not specified (by default).

**githubAuthToken** - (optional) the GitHub authentication access token to establish
private connection to remote repository. For example, `0f95514f36200ebaadb8f28d8ba54300360a9e90`.

**generateXdoc** - (optional) generate release notes. Generated file will be ```xdoc.xml```.

**generateTwit** - (optional) generate a release notes post to publish on Twitter.
Generated file will be ```twitter.txt```.

**generateGitHub** - (optional) generate a release notes post to publish on GitHub Page.
Generated file will be ```github_post.txt```.

**generateAll** - (optional) generate all possible posts.
Generated files will be at specified output location.

**xdocTemplate** - (optional) path to the external xdoc freemarker template file.

**twitterTemplate** - (optional) path to the external twitter freemarker template file.

**publishAllSocial** - (optional) publish all possible posts. Posts are read from generated files.

**gitHubTemplate** - (optional) path to the external GitHub Page freemarker template file.

**publishTwit** - (optional) publish on Twitter from ```twitter.txt```.

**twitterConsumerKey** - (optional) consumer key for Twitter.

**twitterConsumerSecret** - (optional) consumer secret for Twitter.

**twitterAccessToken** - (optional) access token for Twitter.

**twitterAccessTokenSecret** - (optional) access token secret for Twitter.

**twitterProperties** - (optional) path to a properties file for connection to Twitter.

**validateVersion** - (optional) validate that release version matches issues labels.

_**Please, notice!**_
Options in property files have the same names as in command line and have lower priority.

--------------------

Command line usage example:

The following example demonstrates how to generate releasenotes.xml for checkstyle 6.13 release
starting with checkstyle-6.12.1 release tag.

```bash
java -jar releasenotes-builder-1.0-all.jar -localRepoPath /home/user/checkstyle/ \
    -remoteRepoPath checkstyle/checkstyle \
    -startRef checkstyle-6.12.1 -releaseNumber 6.13 -generateAll
```
