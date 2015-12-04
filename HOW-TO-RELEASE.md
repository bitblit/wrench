# How to perform a release

These are mainly notes for myself to remember the steps of performing a release on CircleCI

* Make sure everything is committed, pushed, and a regular build worked on CircleCI
* Add a 'release' tag, like:

*git tag -a "release-1.0__2015.12.02_16.44.00"*

The number won't be used, but it does need to be unique and start with "release-"

* Push the tag to master

*git push origin master --tags*

* Log in to Sonatype and work through their release process after CircleCI finished deploying


# Skipping GPG
To skip GPG locally, use -Dgpg.skip