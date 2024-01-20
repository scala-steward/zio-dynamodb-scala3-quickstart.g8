package scalac.service

import scalac.application.ItemService._
import scalac.domain._
import scalac.infrastructure._
import zio._
import zio.mock.Expectation._
import zio.stream._
import zio.test._
import zio.test.Assertion._
import java.util.UUID

object ItemServiceSpec extends ZIOSpecDefault:
  private val itemIdA = UUID.randomUUID()
  private val itemIdB = UUID.randomUUID()

  val exampleItem = Item(ItemId(itemIdA), "foo", BigDecimal(123))

  val getItemMock: ULayer[ItemRepository] = ItemRepoMock.GetById(
    equalTo(ItemId(itemIdA)),
    value(Some(exampleItem)),
  ) ++ ItemRepoMock.GetById(equalTo(ItemId(itemIdB)), value(None))

  val getByNonExistingId: ULayer[ItemRepository] =
    ItemRepoMock.GetById(equalTo(ItemId(itemIdB)), value(None))

  val updateMock: ULayer[ItemRepository] =
    ItemRepoMock.Update(
      hasField("id", _._1, equalTo(exampleItem.id)),
      value(Some(())),
    ) ++ ItemRepoMock.Update(
      hasField("id", _._1, equalTo(ItemId(itemIdB))),
      value(None),
    )

  def spec = suite("item service test")(
    test("get item id accept long") {
      for {
        found   <- assertZIO(getItemById(ItemId(itemIdA)))(isSome(equalTo(exampleItem)))
        missing <- assertZIO(getItemById(ItemId(itemIdB)))(isNone)
      } yield found && missing
    }.provide(getItemMock),
    test("update item") {
      for {
        found   <- assertZIO(updateItem(ItemId(itemIdA), "foo", BigDecimal(123)))(isSome(equalTo(())))
        missing <- assertZIO(updateItem(ItemId(itemIdB), "bar", BigDecimal(124)))(isNone)
      } yield found && missing
    }.provide(updateMock),
  )
