mvn -s circleci/build_server_maven_settings.xml -DBUILD_NUMBER=x clean site:site
git checkout gh-pages
cp -R target/site/* .
git add .
git commit -a -m "Updating documentation"
git push origin gh-pages
git checkout master