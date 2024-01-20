package scalac.config

import com.typesafe.config.ConfigFactory
import zio._
import zio.config._
import zio.config.typesafe._
import zio.Config._
import zio.config.typesafe.TypesafeConfigProvider

object Configuration:

  final case class ApiConfig(host: String, port: Int)

  object ApiConfig:

    private val serverConfigDescription: Config[ApiConfig] =
      (string("host") zip int("port"))
        .nested("api")
        .to[ApiConfig]

    val layer = ZLayer(
      read(
        serverConfigDescription.from(
          TypesafeConfigProvider.fromTypesafeConfig(
            ConfigFactory.defaultApplication()
          )
        )
      )
    )

  final case class AWSConfig(
      accessKeyId: String,
      secretAccessKey: String,
      region: String,
    )

  object AWSConfig:
    private val serverConfigDescription: Config[AWSConfig] =
      (string("accessKeyId") zip string("secretAccessKey") zip string("region"))
        .nested("aws")
        .to[AWSConfig]

    val layer = ZLayer(
      read(
        serverConfigDescription.from(
          TypesafeConfigProvider.fromTypesafeConfig(
            ConfigFactory.defaultApplication()
          )
        )
      )
    )

  final case class DynamoDbConfig(host: String, port: Int)

  object DynamoDbConfig:

    private val serverConfigDescription: Config[DynamoDbConfig] =
      (string("host") zip int("port"))
        .nested("dynamodb")
        .to[DynamoDbConfig]

    val layer = ZLayer(
      read(
        serverConfigDescription.from(
          TypesafeConfigProvider.fromTypesafeConfig(
            ConfigFactory.defaultApplication()
          )
        )
      )
    )
