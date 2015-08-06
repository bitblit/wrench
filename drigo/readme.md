* Drigo

Drigo is an extensible tool for doing batch processing on files (typically in preparation for uploading to S3)

Main user is the seedy-maven-plugin, also by Erigir.  Extracted to make it easier to unit test by breaking the
wiring to S3 within the tool.

