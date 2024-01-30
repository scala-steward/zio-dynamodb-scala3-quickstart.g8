package $package$.api

import java.nio.charset.StandardCharsets

import $package$.api.Extensions._
import $package$.application.ItemService
import $package$.domain._
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
      val effect: ZIO[ItemRepository, DomainError, String] =
        for {
          id  <- Utils.extractUUID(itemId)
          _   <- ItemService.deleteItem(ItemId(id))
        } yield ().toString

      effect.foldZIO(Utils.handleError, _.toEmptyResponseZIO)

    },
    Method.POST / "items" / string("itemId")    -> handler { (itemId: String, req: Request) =>
      val effect: ZIO[ItemRepository, DomainError, String] =
        for {
          id         <- Utils.extractUUID(itemId)
          createItem <- req.jsonBodyAs[CreateItemRequest]
          _          <- ItemService.addItem(ItemId(id), createItem.name, createItem.price)
        } yield ().toString

      effect.foldZIO(Utils.handleError, _.toResponseZIO(Status.Created))

    },
    Method.PUT / "items" / string("itemId")    -> handler { (itemId: String, req: Request) =>
      val effect: ZIO[ItemRepository, DomainError, String] =
        for {
          id            <- Utils.extractUUID(itemId)
          updateItem    <- req.jsonBodyAs[UpdateItemRequest]
          maybeResult   <- ItemService.updateItem(ItemId(id), updateItem.name, updateItem.price)
          maybeUpdated  <- maybeResult
                          .map(ZIO.succeed(_))
                          .getOrElse(ZIO.fail(NotFoundError))
        } yield maybeUpdated.toString

      effect.foldZIO(Utils.handleError, _.toResponseZIO)

    },
    Method.PATCH / "items" / string("itemId")  -> handler { (itemId: String, req: Request) =>
      val effect: ZIO[ItemRepository, DomainError, String] =
        for {
          id                <- Utils.extractUUID(itemId)
          partialUpdateItem <- req.jsonBodyAs[PartialUpdateItemRequest]
          maybeResult       <- ItemService.partialUpdateItem(
                                 id = ItemId(id),
                                 name = partialUpdateItem.name,
                                 price = partialUpdateItem.price,
                               )
          maybeUpdated      <- maybeResult
                                 .map(ZIO.succeed(_))
                                 .getOrElse(ZIO.fail(NotFoundError))
        } yield maybeUpdated.toString

      effect.foldZIO(Utils.handleError, _.toResponseZIO)
    },
  ).toHttpApp
