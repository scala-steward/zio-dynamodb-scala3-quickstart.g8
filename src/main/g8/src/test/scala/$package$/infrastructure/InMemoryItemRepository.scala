package $package$.infrastructure

import $package$.domain._
import zio._
import java.util.UUID

final class InMemoryItemRepository(
    random: Random,
    storeRef: Ref[Map[ItemId, ItemData]],
  ) extends ItemRepository:

  override def add(item: Item): IO[RepositoryError, Unit] =
    storeRef.update(store => store + (item.id -> item.data))

  override def delete(id: ItemId): IO[RepositoryError, Unit] =
    storeRef.modify { store =>
      if (!store.contains(id)) ((), store)
      else ((), store.removed(id))
    }

  override def getAll(): IO[RepositoryError, List[Item]] =
    storeRef.get.map { store =>
      store.toList.map(kv => Item.withData(kv._1, kv._2))
    }

  override def getById(id: ItemId): IO[RepositoryError, Option[Item]] =
    for {
      store    <- storeRef.get
      maybeItem = store.get(id).map(data => Item.withData(id, data))
    } yield maybeItem

  override def update(id: ItemId, data: ItemData): IO[RepositoryError, Option[Unit]] =
    storeRef.modify { store =>
      if (!store.contains(id)) (None, store)
      else (Some(()), store.updated(id, data))
    }

object InMemoryItemRepository:
  val layer: ZLayer[Random, Nothing, ItemRepository] =
    ZLayer(for {
      random   <- ZIO.service[Random]
      storeRef <- Ref.make(Map.empty[ItemId, ItemData])
    } yield InMemoryItemRepository(random, storeRef))
