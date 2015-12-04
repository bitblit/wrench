# Erigir Wrench General Purpose Tools

General tools for Erigir

# Useful Links

Some functions that arent in here, and why:

Null safe equals : replace by using Objects.equals (o,o) after in 1.7 and later
Throwable to string : replace by using commons lang exception utils
tomemoryformat : replace by FileUtils.byteCountToDisplaySize(long size) in commons io
date (calculate difference) : use JodaTime or JDK 1.8 and use Days.daysBetween

# Release Notes

## Version 1.1.x
* Switched to CircleCI construction and Semantic Versioning (http://semver.org/)
* Updated AWS library version
* Initial creation of the LambdaDescriptor annotation (nothing supporting it yet though)

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

