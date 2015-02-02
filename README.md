# Erigir Wrench General Purpose Tools

General tools for Erigir

# Useful Links

Some functions that arent in here, and why:

Null safe equals : replace by using Objects.equals (o,o) after in 1.7 and later
Throwable to string : replace by using commons lang exception utils
tomemoryformat : replace by FileUtils.byteCountToDisplaySize(long size) in commons io
date (calculate difference) : use JodaTime or JDK 1.8 and use Days.daysBetween

