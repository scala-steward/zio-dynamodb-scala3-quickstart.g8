package scalac

import scalac.api._
import scalac.api.healthcheck._
import scalac.config.Configuration._
import scalac.infrastructure._
import zio._
import zio.config._
import zio.http.Server
import zio.logging.backend.SLF4J

object Boot extends ZIOAppDefault:

  override val bootstrap: ULayer[Unit] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val awsConfigLayer        = DataSourceBuilderLive.awsConfigLayer
  private val dynamoDbLayer         = DataSourceBuilderLive.dynamoDbLayer
  private val dynamoDbExecutorLayer = DataSourceBuilderLive.dynamoDbExecutorLayer

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
      ApiConfig.layer,
      serverLayer,
      AWSConfig.layer,
      DynamoDbConfig.layer,
      awsConfigLayer,
      dynamoDbLayer,
      healthCheckServiceLayer,
      dynamoDbExecutorLayer,
      repoLayer,
    )
