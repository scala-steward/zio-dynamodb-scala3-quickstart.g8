package scalac.infrastructure

import scalac.domain._
import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._
import java.util.UUID
import scalac.infrastructure.dynamodb._
import zio.aws.dynamodb.DynamoDb
import zio.dynamodb.DynamoDBExecutor
import zio.dynamodb.TestDynamoDBExecutor
import zio.dynamodb.{ Item => DynamoItem, PrimaryKey }
object ItemRepositoryLiveSpec extends ZIOSpecDefault:

  val repoLayer = ItemRepositoryLive.layer

  val id1 = UUID.randomUUID()
  val id2 = UUID.randomUUID()
  val id3 = UUID.randomUUID()

  override def spec =
    suite("item repository test with dynamodb test container")(
      test("should support crud opperations") {
        for {
          _            <- TestDynamoDBExecutor.addTable("Items", "id")
          // save items returns their ids
          _            <- ItemRepository.add(Item(ItemId(id1), "first item", BigDecimal(1)))
          _            <- ItemRepository.add(Item(ItemId(id2), "second item", BigDecimal(2)))
          _            <- ItemRepository.add(Item(ItemId(id3), "third item", BigDecimal(3)))
          // get all returns 3 items
          items        <- ItemRepository.getAll()
          testResult1  <- assert(items)(hasSize(equalTo(3)))
          // "delete first item"
          _            <- ItemRepository.delete(ItemId(id1))
          deleted      <- ItemRepository.getById(ItemId(id1))
          testResult2  <- assert(deleted)(isNone)
          // "get item 2"
          fetchedItem2 <- ItemRepository.getById(ItemId(id2))
          testResult3  <-
            assert(fetchedItem2)(isSome) &&
            assert(fetchedItem2.get.name)(equalTo("second item")) &&
            assert(fetchedItem2.get.price)(equalTo(BigDecimal("2")))
          // "update item 3"
          _            <- ItemRepository.update(ItemId(id3), ItemData("updated item", BigDecimal(3)))
          updated      <- ItemRepository.getById(ItemId(id3))
          testResult4  <-
            assert(updated)(isSome) &&
            assert(updated.get.name)(equalTo("updated item")) &&
            assert(updated.get.price)(equalTo(BigDecimal(3)))
        } yield testResult1 && testResult2 && testResult3 && testResult4
      }
    ).provideShared(
      DynamoDBExecutor.test,
      repoLayer,
    ) @@ sequential
