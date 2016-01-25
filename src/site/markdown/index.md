# Erigir Wrench General Purpose Tools

General tools for Erigir

# Useful Links

Some functions that arent in here, and why:

Null safe equals : replace by using Objects.equals (o,o) after in 1.7 and later
Throwable to string : replace by using commons lang exception utils
tomemoryformat : replace by FileUtils.byteCountToDisplaySize(long size) in commons io
date (calculate difference) : use JodaTime or JDK 1.8 and use Days.daysBetween

# To Release

# Commit all changes
# Tag the current location with a tag of the form "release-xxx" where xxx is the version number, eg "git tag -a release-1.1.1"
# Push the branch and tags to master, CircleCI will take it from there, eg "git push origin master --tags"
# Login to Sonatype and close/release
