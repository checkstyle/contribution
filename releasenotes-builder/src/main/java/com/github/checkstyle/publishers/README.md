#Publication examples

Build jar:
```
mvn clean package
```
and navigate to it:
```
cd target
```

##Xdoc
Prepare releasenotes 7.3 for commit in /home/user/opensource/checkstyle:
```
java -jar releasenotes-builder-1.0-all.jar -localRepoPath /home/user/opensource/checkstyle -startRef checkstyle-7.2 \
     -releaseNumber 7.3 -githubAuthToken a0b1234567890ad1234e5678fc9e01234a56d789 -generateXdoc -publishXdoc
```
to make push just add `-publishXdocWithPush` option

##Twitter
```
java -jar releasenotes-builder-1.0-all.jar -localRepoPath /home/user/opensource/checkstyle -startRef checkstyle-7.2 \
     -releaseNumber 7.3 -githubAuthToken a0b1234567890ad1234e5678fc9e01234a56d789 -generateTwit -publishTwit \
     -twitterConsumerKey Fgsdf456sdffe8f4cs1dcsd8S -twitterConsumerSecret VvGSzCIFDCSzscS64cCSc8c4sczSCCScs41Ew2vJZ5TKyYhP6F \
     -twitterAccessToken 712345678921234567-4SVDS6sdvzx1vdVDV54dVDP5RcaDaEz \
     -twitterAccessTokenSecret t5N3IoGVSHAvcvTCvJ465c4ZCXcc66548CS48SCFC1ccz
```
or using /home/user/documents/twitter.properties
```
java -jar releasenotes-builder-1.0-all.jar -localRepoPath /home/user/opensource/checkstyle -startRef checkstyle-7.2 \
     -releaseNumber 7.3 -authToken a0b1234567890ad1234e5678fc9e01234a56d789 -generateTwit -publishTwit \
     -twitterProperties /home/user/documents/twitter.properties
```
where twitter.properties looks like:
```
twitterConsumerKey=Fgsdf456sdffe8f4cs1dcsd8S
twitterConsumerSecret=VvGSzCIFDCSzscS64cCSc8c4sczSCCScs41Ew2vJZ5TKyYhP6F
twitterAccessToken=712345678921234567-4SVDS6sdvzx1vdVDV54dVDP5RcaDaEz
twitterAccessTokenSecret=t5N3IoGVSHAvcvTCvJ465c4ZCXcc66548CS48SCFC1ccz
```
