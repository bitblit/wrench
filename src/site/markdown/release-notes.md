## Release Notes

# Version 2.1.5 / 2.1.6
Backwards compatible feature release.  Adding **deleteLogFilesModifiedMoreThanThisLongAgo** function to 
LogFileSynchronizer to allow purging of the log file directory explicitly.

# Version 2.1.4
Backwards compatible logging release.  LogFileSynchronizer was dumping far too much at the info level that should
be at the debug level.  Cleaned up.  

# Version 2.1.2  / 2.1.3
Backwards compatible bug-fix release.  Fixes LogFileSynchronizer using the wrong name in the logs, and also not
deleting files correctly upon successful upload and quiet time.

# Version 2.1.1
Backwards compatible feature release that deprecates LogFileUploader in favor of LogFileSynchronizer (I know, so soon?).
LogFileSynchronizer has all the features of the uploader but adds the ability to synchronize active files on a periodic
basis so you don't have to wait until the end of the day to upload the file.

# Version 2.1.0
Backwards compatible release that adds the RestartingFileDownloader class to wrench-web, the 
Cloudwatch/SLF4J log file uploader to wrench-aws, and moves Canonicalizer from wrench-web to 
wrench-common (patch class left in place for current backwards compatibility.  It will be removed
in the next major release).  Also upgrading version numbers of all applicable dependant libraries -
see pom files for details.

# Version 2.0.3 / 2.0.4
Backwards compatible release that converts coverage tool from Cobertura to Jacoco since Cobertura was throwing unwanted
classes into the build via compile time decoration.

# Version 2.0.2
Backwards compatible release that works around a JDK bug when using the AllowSelfSignedHttps adapter on 1.8 JDKs < build 141
See : http://www.oracle.com/technetwork/java/javase/2col/8u141-bugfixes-3720387.html

# Version 2.0.0 / 2.0.1
Code is not backwards compatible due to upping the Java version to 1.8 and upgrading a bunch of libraries to newer
versions.  Should pretty much straight compile (with the exception of some stuff moved into the fluke package)

* Updating a bunch of libraries to new versions
* Adding "Fluke" - weirdo one-off stuff pulled out of commons since it just bloated it with dependencies and was uncommon usage
* Adding "Steelpipe" - a tunneled MariaDB (or Mysql) DB connection over SSH
* Hopefully upgrading to version 2.0.0 will finally reclaim head on search.maven.org

# Version 1.2.1
Backwards compatible feature release
* Adds the SimpleStreamHttpUtils which is like SimpleHttpUtils but allows you to specify streams to read/write from
** NOTE : This follows a "you opened them, you close them" model - its up to you to deal with the streams you pass in
* Also adds the NullOutputStream class for when you really don't care what gets written (typically when calling a ping url)

# Version 1.2.0
Code is backwards compatible, but tokens created with previous versions
of TokenService will not parse correctly.  Not making this a major 
release is a judgement call - the tokens will invalidate, but they
are short-term tokens and this upgrade will look from that point of view
like a forced token expiration which is a normal operation
* Added a Crockford Base32 class to commons
* Switched TokenService to use Crockford Base32 for easier cross-language compatibility
* Upgraded versions for AWS, Spring, Jackson
* Did NOT upgrade servlet version as that requires Tomcat 8.5 - currently not supported by plugin
* Global reformat/cleanup of code

# Version 1.1.8
Backwards compatible bugfix/feature release
* Small tweak to cleanup of connections used by SimpleHttpUtils

# Version 1.1.7
Backwards compatible bugfix/feature release
* Better cleanup of connections used by SimpleHttpUtils
* Added InstanceInfo for ec2 instances in the AWS package

# Version 1.1.6
Backwards compatible bugfix release
* Fixed error with null pointer exception in simplehttputils when there is no error stream

## Version 1.1.5
Backwards compatible feature release
* Extended SimpleHttpUtils to allow PUT and DELETE
* Extended SimpleHttpUtils to allow fetching the details of the latest error

## Version 1.1.4
Backwards compatible feature release
* Extended SimpleHttpUtils to allow POST
* Extended SimpleHttpUtils to allow fetching headers and status code in addition to body contents

## Version 1.1.3
Backwards compatible feature release
* Extracted interface from DynamoCachedObject to AWSCachedObject
* Added implementation of S3CachedObject for things that are too big for DynamoDB

## Version 1.1.2
Backwards compatible feature release
* Added AllowSelfSignHTTPS to commons
* Added MemoryAppender/LoggingRingBuffer to commons
* Added SystemClock to commons
* Added ExtendedQuietObjectMapper to commons
* Added DynamoCachedObject to aws/dynamo
* Updating AWS library version to 1.10.56


## Version 1.1.1
Backwards compatible feature release
* Added FakeAPIFilter to wrench-web
* Switched default version number to LOCAL-SNAPSHOT so it will work when depended on locally 
* Updating AWS library version to 1.10.49

## Version 1.1.0
* Switched to CircleCI construction and Semantic Versioning (http://semver.org/)
* Updated AWS library version
* Added the AWS-Lambda passthrough object to Ape

## Version 1.0.28
DO NOT USE THIS RELEASE
* This was an attempt at semantic versioning that failed - the POM is corrupt

## Version 1.003
Backwards compatible feature release
* Drigo's logging was pretty over the top - moved a lot of it down to trace level and cleaned up some

## Version 1.002
Backwards compatible feature release
* Added new implementation for SimpleIncludesProcessor that maps patterns to strings (match the right, replace with the
left)
* Added replacer based on that implementation into Drigo (DrigoReplaceProcessor)
* Added MD5 handling to Drigo
* Added HTML compression to Drigo


## Version 1.001
NON-Backwards compatible feature release
-- Only not-backwards compatible if you created ObjectStorageImplementations for SOS - everything else is backwards 
compatible.  Also, tokens generated by the previous version of TokenService are invalid with this one - generate new
tokens
* Changed SimpleObjectService to use streams instead of byte arrays to save on memory consumption
* Slight tweak to error handling in Ape - it now detects when Spring returns a 400 (typically because the JSON supplied
in the body either isn't valid or can't be deserialized into the appropriate object) and returns a 400 instead of the
default 404 object
* Changed TokenService to use Base32 instead of Base64 - this makes it safe to put the resulting strings into URLs
without having to URL encode them

## Version 1.000
NON-Backwards compatible feature release
* Changed Ape to use servlet error handling instead of filtering - this should catch all the pesky "non-spring" errors
and handle them correctly as JSON endpoints
* Added ServerErrorNotifier to SNS for wrench-aws to allow simple forwarding of all uncaught exceptions to a SNS
endpoint.  Plugs into the Ape exception handler for rethrow.
* Updated SimpleCORSFilter to allow smart filtering of incoming origin header like I've wanted for a while (still
defaults to allow all origins for backward(ish) compatibility.  Used to allow "no origin" header but that is in 
violation of the spec

## Version 0.921
Backwards compatible bugfix release
* Renamed drigo to wrench-drigo to fit naming convention

## Version 0.92
Backwards compatible feature release
* Added SimpleIncludesProcessor to commons
* Added SimpleIncludesFilter (uses the processor) to web
* Created the "Drigo" package, extracting all file processing that used to be in shiro-maven-plugin for reuse
-- Note: Error in this release, Drigo should have been wrench-drigo

## Version 0.91
Backwards compatible feature release
* Added the ability for shiro-oauth to read the host/scheme values from headers (eg, from Nginx proxy)
* Added the Ape package for simplifying API development (mainly what was in Scribe before)

## Version 0.9
Backwards compatible feature release
* Added the DateTime and Percent converters to the ZK package
* Added DumpDatabase to the commons/Mysql package
* Added BackupMysqlToS3 to AWS package

## Version 0.8
Bugfix release - JSON dump of HitMeasuringFilter didn't actually have the count and date data.  Fixed and test added

## Version 0.7
Backwards compatible feature release
* Added S3PropertiesReader - (Reads properties objects from an S3 location) (AWS module)
* Added SimpleObjectStorage to commons - (Easy storage of a JSON object representation, include Jackson 2 library)
** Commons module includes a file system implementation
* Added S3ObjectStore implementation for SimpleObjectStorage (aws module)
* Added CollectionUtils, DateUtils, ZipUtils to commons module
* Added QuietObjectMapper - an extension to ObjectMapper from Jackson that throws no checked exceptions
* Converted HitMeasuringFilter to dump JSON instead of TXT at its monitoring location
 

## Version 0.6
Backwards compatible feature release
* Adds the ability to set a limiting maximum to FilteredDynamicListModel
* Adding the new shiro-oauth adapter (see README.md in shiro-oauth for more details)

## Version 0.5
Bugfix release - fixes issue with FilteredDynamicListModel consuming too much memory on large datasets

## Version 0.4
A backwards compatible new-feature release
Added FilteredDynamicListModel to ZK module

