package $package$

import $package$.api._
import $package$.api.healthcheck._
import $package$.config.Configuration._
import $package$.infrastructure.ConnectionBuilder
import $package$.infrastructure.ItemRepositoryLive
import zio._
import zio.config._
import zio.http.Server
import zio.logging.backend.SLF4J

object Boot extends ZIOAppDefault:

  override val bootstrap: ULayer[Unit] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val awsConfigLayer        = ConnectionBuilder.awsConfigLayer
  private val dynamoDbLayer         = ConnectionBuilder.dynamoDbLayer
  private val dynamoDbExecutorLayer = ConnectionBuilder.dynamoDbExecutorLayer

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
