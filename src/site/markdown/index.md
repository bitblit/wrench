# Erigir Wrench General Purpose Tools

A set of generally useful tools that I use everywhere.  Quick overview:

* Commons : Stuff I use everywhere - quiet wrappers, general output streams, simple http manipulation, zip files
* Ape : Stuff to make it easier to serve APIs from Spring MVC
* AWS : Stuff for working in Amazon Web Services
* Drigo : A library for preprocessing directories full of html/js/css to ready it for upload to S3
* Fluke : Random stuff that is really one-off (like parsing Google Contacts)
* Shiro-Oauth : A plugin for shiro to use Oauth providers like Google/Facebook
* Steelpipe : A library to tunnel SSH and then connect to a MariaDB/Mysql instance on the other side
* Web : Random stuff useful for Webapps served through MVC
* Zk : Extensions to the ZK library

# Missing Functions

Some functions that arent in here (any more!), and why:

* Null safe equals : replace by using Objects.equals (o,o) after in 1.7 and later
* Throwable to string : replace by using commons lang exception utils
* toMemoryFormat : replace by FileUtils.byteCountToDisplaySize(long size) in commons io
* date (calculate difference) : use JodaTime or JDK 1.8 and use Days.daysBetween

# To Release

Since I'm always forgetting how to do this

* Commit all changes
* Tag the current location with a tag of the form "release-xxx" where xxx is the version number, eg "git tag -a release-1.1.1"
* Push the branch and tags to master, CircleCI will take it from there, eg "git push origin master --tags"
* Login to Sonatype and close/release
