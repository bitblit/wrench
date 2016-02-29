# Erigir Wrench General Purpose Tools

General tools for Erigir

See the official site documentation at 
<a href="http://bitblit.github.io/wrench">http://bitblit.github.io/wrench</a>

This always builds locally as "LOCAL-SNAPSHOT".  If you are developing it in tandem with another project set that
project to LOCAL-SNAPSHOT.  CircleCI replaces LOCAL-SNAPSHOT with the release from the tag when performing a 
release.

## Performing releases

Mainly to aid my own failing memory here:

git tag -a release-{number} (eg git tag -a release-1.1.2)
git push origin master --tags