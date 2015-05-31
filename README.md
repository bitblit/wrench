# Erigir Wrench General Purpose Tools

General tools for Erigir

# Useful Links

Some functions that arent in here, and why:

Null safe equals : replace by using Objects.equals (o,o) after in 1.7 and later
Throwable to string : replace by using commons lang exception utils
tomemoryformat : replace by FileUtils.byteCountToDisplaySize(long size) in commons io
date (calculate difference) : use JodaTime or JDK 1.8 and use Days.daysBetween

# Release Notes

## Version 0.6
Backwards compatible feature release
* Adds the ability to set a limiting maximum to FilteredDynamicListModel
* Adding the new shiro-oauth adapter (see README.md in shiro-oauth for more details)

## Version 0.5
Bugfix release - fixes issue with FilteredDynamicListModel consuming too much memory on large datasets

## Version 0.4
A backwards compatible new-feature release
Added FilteredDynamicListModel to ZK module

