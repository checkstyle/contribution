About
========

The tool is designed for generation and publishing of release notes posts for Google Plus, Twitter, Sourceforge, RSS and Checkstyle mailing list.

Usage
========

Please notice that **jdk 1.8 is required** (not tested with jdk 1.9).

To build a jar use:
`./gradlew jar`

To build a jar with dependencies use:
`./gradlew allJar`

Command line options are:
- `-r releaseNotesFile` - the location of the input release notes file;
- `-o outputFolderLocation` - the location of the output folder;
- `-generateXXX` - generate post for XXX;
- `-publishXXX` - publish post to XXX;
- `-loginXXX loginForXXX` - login which will be used to publish a post to XXX;
- `-pwdXXX passwordForXXX` - password which will be used to publish a post to XXX;
- `-propXXX propertiesFileLocation` - the location of the properties file which contains login and password for XXX.

Options for generating and publishing are:
- `Gplus` - Google Plus
- `Twitter` - Twitter
- `Sf` - SourceForge
- `Mlist` - mailing list
- `Rss` - RSS
- `All` - all of the above

To publish on Twitter the following options in command line or properties file are required:
- `-consKeyTw key` - consumer key
- `-consSecretTw secret` - consumer secret
- `-accessTokenTw token` - access token
- `-accessTokenSecretTw secret` - access token secret

Please notice that properties files have *lower* priority than login and password from command line options.

Example of jar usage:
`java jar social-posts-builder-all-1.0-SNAPSHOT
        -r ~/Documents/releaseNotes.xml
        -o ~/Target
        -generateAll
        -publishSf -loginSf myLogin -pwdSf myPassword`
