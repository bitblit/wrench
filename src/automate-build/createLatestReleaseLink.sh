#!/usr/bin/env bash
echo Creating latest release link for S3, build is $1



echo Creating cache manifest
cd dist
echo "CACHE MANIFEST" > cache.manifest
echo "# Built : "`date +%Y-%m-%d_%H:%M:%S` >> cache.manifest
cat ../cache-manifest-header-template.txt >> cache.manifest
find . -type f | sed "s#^\./##" | grep -vi "ds_store" | grep -vi "cache.manifest" >> cache.manifest
cat ../cache-manifest-footer-template.txt >> cache.manifest
echo Finished manifest
cd ..