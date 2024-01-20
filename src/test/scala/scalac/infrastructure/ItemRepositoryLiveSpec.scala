package scalac.infrastructure

import scalac.domain._
import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._
import java.util.UUID
import scalac.infrastructure.dynamodb._
import scalac.config.Configuration._
import zio.aws.dynamodb.DynamoDb
import zio.dynamodb.DynamoDBExecutor
import zio.aws.core.config.AwsConfig
import zio.aws.netty.NettyHttpClient
import java.net.URI
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials

object ItemRepositoryLiveSpec extends ZIOSpecDefault:

  val containerLayer = ZLayer.scoped(DynamoContainer.make())

  // FIXME: create table on init
  val dynamoDbLayer: ZLayer[DynamoContainer, Throwable, DynamoDBExecutor] =
    NettyHttpClient.default
      >>> AwsConfig.default
      >>> DynamoDb.customized { builder =>
        builder
          .endpointOverride(URI.create(s"http://0.0.0.0:4567"))
          .region(Region.of("us-east-1"))
          .credentialsProvider(
            StaticCredentialsProvider
              .create(AwsBasicCredentials.create("dummy", "dummy"))
          )

      }
      >>> DynamoDBExecutor.live

  val repoLayer = ItemRepositoryLive.layer

  val id1 = UUID.randomUUID()
  val id2 = UUID.randomUUID()
  val id3 = UUID.randomUUID()

  override def spec =
    suite("item repository test with dynamodb test container")(
      test("save items returns their ids") {
        for {
          _ <- ItemRepository.add(Item(ItemId(id1), "first item", BigDecimal(1)))
          _ <- ItemRepository.add(Item(ItemId(id2), "second item", BigDecimal(2)))
          _ <- ItemRepository.add(Item(ItemId(id3), "third item", BigDecimal(3)))

        } yield assert(ItemId(id1))(equalTo(ItemId(id1)))
        && assert(ItemId(id2))(equalTo(ItemId(id2)))
        && assert(ItemId(id3))(equalTo(ItemId(id3)))
      },
      test("get all returns 3 items") {
        for {
          items <- ItemRepository.getAll()
        } yield assert(items)(hasSize(equalTo(3)))
      },
      test("delete first item") {
        for {
          _    <- ItemRepository.delete(ItemId(id1))
          item <- ItemRepository.getById(ItemId(id1))
        } yield assert(item)(isNone)
      },
      test("get item 2") {
        for {
          item <- ItemRepository.getById(ItemId(id2))
        } yield assert(item)(isSome) &&
        assert(item.get.name)(equalTo("second item")) &&
        assert(item.get.price)(equalTo(BigDecimal("2")))
      },
      test("update item 3") {
        for {
          _    <- ItemRepository.update(ItemId(id3), ItemData("updated item", BigDecimal(3)))
          item <- ItemRepository.getById(ItemId(id3))
        } yield assert(item)(isSome) &&
        assert(item.get.name)(equalTo("updated item")) &&
        assert(item.get.price)(equalTo(BigDecimal(3)))
      },
    ).provideShared(
      containerLayer,
      dynamoDbLayer,
      repoLayer,
    ) @@ sequential
