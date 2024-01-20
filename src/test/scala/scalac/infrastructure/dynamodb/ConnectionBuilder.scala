package scalac.infrastructure.dynamodb

import zio._
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import zio.ZLayer
import zio.aws.core.config
import zio.aws.dynamodb
import zio.aws.dynamodb.DynamoDb
import zio.aws.netty
import zio.dynamodb.DynamoDBExecutor

import java.net.URI
import zio.ZIO
import zio.dynamodb.DynamoDBQuery
import zio.dynamodb.KeySchema
import zio.dynamodb.BillingMode
import zio.dynamodb.AttributeDefinition
import com.dimafeng.testcontainers.DynaliteContainer

object DataSourceBuilderLive:

  val awsConfigLayer = ZLayer.succeed(
    config.CommonAwsConfig(
      region = None,
      credentialsProvider = StaticCredentialsProvider
        .create(AwsBasicCredentials.create("dummy", "dummy")),
      endpointOverride = None,
      commonClientConfig = None,
    )
  )

  val dynamoDbLayer: ZLayer[Any, Throwable, DynamoDb] =
    (netty.NettyHttpClient.default ++ awsConfigLayer)
      >>> config.AwsConfig.default >>> dynamodb.DynamoDb.customized { builder =>
        builder.endpointOverride(URI.create("http://localhost:8000")).region(Region.US_EAST_1)
      }

  val dynamoDbExecutorLayer: ZLayer[DynaliteContainer, Throwable, DynamoDBExecutor] =
    dynamoDbLayer >>> DynamoDBExecutor.live
