echo Uploading docs folders to S3, root is $1
aws s3 cp target/site $1 --recursive
aws s3 cp ape/target/site $1/wrench-ape --recursive
aws s3 cp aws/target/site $1/wrench-aws --recursive
aws s3 cp commons/target/site $1/wrench-commons --recursive
aws s3 cp drigo/target/site $1/wrench-drigo --recursive
aws s3 cp fluke/target/site $1/wrench-fluke --recursive
aws s3 cp shiro-oauth/target/site $1/wrench-shiro-oauth --recursive
aws s3 cp steelpipe/target/site $1/wrench-steelpipe --recursive
aws s3 cp web/target/site $1/wrench-web --recursive
aws s3 cp zk/target/site $1/wrench-zk --recursive
