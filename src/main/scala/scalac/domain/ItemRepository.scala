package scalac.domain

import zio._

trait ItemRepository:

  def add(item: Item): IO[RepositoryError, Unit]

  def delete(id: ItemId): IO[RepositoryError, Unit]

  def getAll(): IO[RepositoryError, List[Item]]

  def getById(id: ItemId): IO[RepositoryError, Option[Item]]

  def update(itemId: ItemId, data: ItemData): IO[RepositoryError, Option[Unit]]

object ItemRepository:

  def add(item: Item): ZIO[ItemRepository, RepositoryError, Unit] =
    ZIO.serviceWithZIO[ItemRepository](_.add(item))

  def delete(id: ItemId): ZIO[ItemRepository, RepositoryError, Unit] =
    ZIO.serviceWithZIO[ItemRepository](_.delete(id))

  def getAll(): ZIO[ItemRepository, RepositoryError, List[Item]] =
    ZIO.serviceWithZIO[ItemRepository](_.getAll())

  def getById(id: ItemId): ZIO[ItemRepository, RepositoryError, Option[Item]] =
    ZIO.serviceWithZIO[ItemRepository](_.getById(id))

  def update(itemId: ItemId, data: ItemData): ZIO[ItemRepository, RepositoryError, Option[Unit]] =
    ZIO.serviceWithZIO[ItemRepository](_.update(itemId, data))
