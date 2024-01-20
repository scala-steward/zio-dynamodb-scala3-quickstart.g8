package scalac

import scalac.api._
import scalac.api.healthcheck._
import scalac.config.Configuration.ApiConfig
import scalac.infrastructure._
import zio._
import zio.config._
import zio.http.Server
import zio.logging.backend.SLF4J
import zio.dynamodb.DynamoDBExecutor
import zio.aws.dynamodb.DynamoDb

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

object Boot extends ZIOAppDefault:

  override val bootstrap: ULayer[Unit] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  // REVIEW
  private val awsConfig = ZLayer.succeed(
    config.CommonAwsConfig(
      region = None,
      credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy")),
      endpointOverride = None,
      commonClientConfig = None,
    )
  )

  // REVIEW
  private val dynamoDbLayer: ZLayer[Any, Throwable, DynamoDb] =
    (netty.NettyHttpClient.default ++ awsConfig) >>> config.AwsConfig.default >>> dynamodb.DynamoDb.customized {
      builder =>
        builder.endpointOverride(URI.create("http://localhost:8000")).region(Region.US_EAST_1)
    }

  private val dynamoDbExecutorLayer = dynamoDbLayer >>> DynamoDBExecutor.live

  private val repoLayer = ItemRepositoryLive.layer

  private val healthCheckServiceLayer = HealthCheckServiceLive.layer

  private val serverLayer =
    ZLayer
      .service[ApiConfig]
      .flatMap { cfg =>
        Server.defaultWith(_.binding(cfg.get.host, cfg.get.port))
      }
      .orDie

  val routes = HttpRoutes.app ++ HealthCheckRoutes.app

  private val program = Server.serve(routes)

  override val run =
    program.provide(
      healthCheckServiceLayer,
      serverLayer,
      ApiConfig.layer,
      repoLayer,
      dynamoDbLayer,
      dynamoDbExecutorLayer,
    )
