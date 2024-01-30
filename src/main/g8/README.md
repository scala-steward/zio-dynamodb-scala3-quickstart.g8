# Zio-Http-DynamoDB
A Simple REST API with CRUD operations, in Zio using Scala, ZIO-Http and DynamoDB local instance.

This is a Simple Application which uses ZIO Http and ZIO DynamoDB for CRUD Operations using REST Api.


## Steps to follow:

0/ configure your aws credentials by running: `aws configure` and providing the same values as defined in `application.conf`.
   also make sure you have the aws-cli v2 command installed [link](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)

1/ run dynamoDB on local instance on port 8080, as defined in `application.conf`, by running: `docker-compose up -d` from root folder.

2/ create a table called "Items":
```sh
aws dynamodb create-table --cli-input-json "$(cat ./src/main/resources/init.json)" --endpoint-url http://localhost:8000 --region us-east-1
```

3/ make sure your table exists and is active:
```sh
aws dynamodb list-tables --endpoint-url http://localhost:8000 --region us-east-1

aws dynamodb describe-table --table-name Items --endpoint-url http://localhost:8000 --region us-east-1 --query "Table.RegionName" --output text
```

4/ `sbt clean compile`

5/ `sbt run`


## endpoints
curl -X GET localhost:8080/healthcheck

curl -X GET localhost:8080/items

curl -X POST -H "Content-Type: application/json" -d '{"name": "example-item", "price": 19.99}' http://localhost:8080/items/06cb1888-66be-4fd5-9ecc-bc801f95653f

curl -X PUT -H "Content-Type: application/json" -d '{"name": "example-item", "price": 20.99}' http://localhost:8080/items/06cb1888-66be-4fd5-9ecc-bc801f95653f

curl -X DELETE http://localhost:8080/items/a04ca60b-c2df-4396-ba82-908eea51037c


## TODO
- create table during boot
https://github.com/zio/zio-dynamodb/blob/series/2.x/examples/src/main/scala/zio/dynamodb/examples/dynamodblocal/DynamoDBLocalMain.scala

- zio-http -> openapi -> swagger endpoints
https://www.youtube.com/watch?v=f8wwS1anL1s&embeds_referring_euri=https%3A%2F%2Fwww.google.com%2Fsearch%3Fq%3Dzio-http%2B%252B%2Bswagger%2Bendpoints%26rlz%3D1C5CHFA_enES1068ES1068%26oq%3Dzio-http%2B%252B%2Bswagger%2Bendpoints%26gs_l&source_ve_path=MjM4NTE&feature=emb_title