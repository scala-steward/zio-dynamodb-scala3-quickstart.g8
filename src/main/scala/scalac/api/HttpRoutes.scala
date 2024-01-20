package scalac.api

import java.nio.charset.StandardCharsets

import scalac.api.Extensions._
import scalac.application.ItemService
import scalac.domain._
import zio._
import zio.http._
import zio.json._

object HttpRoutes extends JsonSupport:

  val app: HttpApp[ItemRepository] = Routes(
    Method.GET / "items"                       -> handler { (_: Request) =>
      val effect: ZIO[ItemRepository, DomainError, List[Item]] =
        ItemService.getAllItems()

      effect.foldZIO(Utils.handleError, _.toResponseZIO)

    },
    Method.GET / "items" / string("itemId")    -> handler { (itemId: String, _: Request) =>
      val effect: ZIO[ItemRepository, DomainError, Item] =
        for {
          id        <- Utils.extractUUID(itemId)
          maybeItem <- ItemService.getItemById(ItemId(id))
          item      <- maybeItem
                         .map(ZIO.succeed(_))
                         .getOrElse(ZIO.fail(NotFoundError))
        } yield item

      effect.foldZIO(Utils.handleError, _.toResponseZIO)

    },
    Method.DELETE / "items" / string("itemId") -> handler { (itemId: String, _: Request) =>
      val effect: ZIO[ItemRepository, DomainError, Unit] =
        (for {
          id <- Utils.extractUUID(itemId)
          _  <- ItemService.deleteItem(ItemId(id))
        } yield ()).either.flatMap {
          case Left(_)  => ZIO.fail(NotFoundError)
          case Right(_) => ZIO.unit
        }

      effect.foldZIO(Utils.handleError, _.toEmptyResponseZIO)

    },
    Method.POST / "items"                      -> handler { (req: Request) =>
      val effect: ZIO[ItemRepository, DomainError, Unit] =
        for {
          createItem <- req.jsonBodyAs[CreateItemRequest]
          _          <- ItemService.addItem(createItem.name, createItem.price)
        } yield ()

      effect.foldZIO(Utils.handleError, _.toResponseZIO(Status.Created))

    },
    Method.PUT / "items" / string("itemId")    -> handler { (itemId: String, req: Request) =>
      val effect: ZIO[ItemRepository, DomainError, Unit] =
        (for {
          id         <- Utils.extractUUID(itemId)
          updateItem <- req.jsonBodyAs[UpdateItemRequest]
          _          <- ItemService.updateItem(ItemId(id), updateItem.name, updateItem.price)
        } yield ()).either.flatMap {
          case Left(_)  => ZIO.fail(NotFoundError)
          case Right(_) => ZIO.unit
        }

      effect.foldZIO(Utils.handleError, _.toResponseZIO)

    },
    Method.PATCH / "items" / string("itemId")  -> handler { (itemId: String, req: Request) =>
      val effect: ZIO[ItemRepository, DomainError, Item] =
        for {
          id                <- Utils.extractUUID(itemId)
          partialUpdateItem <- req.jsonBodyAs[PartialUpdateItemRequest]
          maybeItem         <- ItemService.partialUpdateItem(
                                 id = ItemId(id),
                                 name = partialUpdateItem.name,
                                 price = partialUpdateItem.price,
                               )
          item              <- maybeItem
                                 .map(ZIO.succeed(_))
                                 .getOrElse(ZIO.fail(NotFoundError))
        } yield item

      effect.foldZIO(Utils.handleError, _.toResponseZIO)
    },
  ).toHttpApp
