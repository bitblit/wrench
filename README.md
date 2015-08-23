# Erigir Wrench General Purpose Tools

General tools for Erigir

# Useful Links

Some functions that arent in here, and why:

Null safe equals : replace by using Objects.equals (o,o) after in 1.7 and later
Throwable to string : replace by using commons lang exception utils
tomemoryformat : replace by FileUtils.byteCountToDisplaySize(long size) in commons io
date (calculate difference) : use JodaTime or JDK 1.8 and use Days.daysBetween

# Release Notes

## Version 1.000
Non-Backwards compatible feature release
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

