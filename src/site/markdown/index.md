# Erigir Wrench General Purpose Tools

A set of generally useful tools that I use everywhere.

# Functions that AREN'T in here, and where to find them

Some functions that arent in here (any more!), and why:

* Null safe equals : replace by using Objects.equals (o,o) after in 1.7 and later
* Throwable to string : replace by using commons lang exception utils
* toMemoryFormat : replace by FileUtils.byteCountToDisplaySize(long size) in commons io
* date (calculate difference) : use JodaTime or JDK 1.8 and use Days.daysBetween

# How To Perform a Release

Since I'm always forgetting how to do this

* Commit all changes
* Tag the current location with a tag of the form "release-xxx" where xxx is the version number, eg "git tag -a release-1.1.1"
* Push the branch and tags to master, CircleCI will take it from there, eg "git push origin master --tags"
* Login to Sonatype and close/release
