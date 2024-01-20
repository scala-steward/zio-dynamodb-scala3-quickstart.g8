package scalac.application

import scalac.domain._
import zio._
import java.util.UUID

object ItemService:

  def addItem(name: String, price: BigDecimal): ZIO[ItemRepository, DomainError, Unit] =
    ZIO.serviceWithZIO[ItemRepository](_.add(Item(ItemId(UUID.randomUUID()), name, price)))

  def deleteItem(id: ItemId): ZIO[ItemRepository, DomainError, Unit] =
    ZIO.serviceWithZIO[ItemRepository](_.delete(id))

  def getAllItems(): ZIO[ItemRepository, DomainError, List[Item]] =
    ZIO.serviceWithZIO[ItemRepository](_.getAll())

  def getItemById(id: ItemId): ZIO[ItemRepository, DomainError, Option[Item]] =
    ZIO.serviceWithZIO[ItemRepository](_.getById(id))

  def updateItem(
      id: ItemId,
      name: String,
      price: BigDecimal,
    ): ZIO[ItemRepository, DomainError, Option[Unit]] =
    for {
      repo   <- ZIO.service[ItemRepository]
      data   <- ZIO.succeed(ItemData(name, price))
      result <- repo.update(id, data)
    } yield result

  def partialUpdateItem(
      id: ItemId,
      name: Option[String],
      price: Option[BigDecimal],
    ): ZIO[ItemRepository, DomainError, Option[Item]] =
    (for {
      repo        <- ZIO.service[ItemRepository]
      currentItem <- repo.getById(id).some
      data         = ItemData(name.getOrElse(currentItem.name), price.getOrElse(currentItem.price))
      _           <- repo.update(id, data).map(Some.apply).some
    } yield Item.withData(id, data)).unsome
