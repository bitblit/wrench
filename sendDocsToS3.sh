echo Uploading docs folders to S3, root is $1
aws s3 cp target/site $1 --recursive
#aws s3 cp ape/target/site $1/ape --recursive
#aws s3 cp aws/target/site $1/aws --recursive
aws s3 cp commons/target/site $1/commons --recursive
#aws s3 cp drigo/target/site $1/drigo --recursive
#aws s3 cp fluke/target/site $1/fluke --recursive
#aws s3 cp shiro-oauth/target/site $1/shiro-oauth --recursive
#aws s3 cp steelpipe/target/site $1/steelpipe --recursive
#aws s3 cp web/target/site $1/web --recursive
#aws s3 cp zk/target/site $1/zk --recursive
