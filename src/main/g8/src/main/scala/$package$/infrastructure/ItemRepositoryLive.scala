package $package$.infrastructure

import java.sql.SQLException

import $package$.domain._
import zio.{ IO, URLayer, ZIO, ZLayer }
import zio.aws.dynamodb.DynamoDb
import zio.aws.dynamodb.model.PutItemRequest
import zio.dynamodb.AttributeValue
import zio.aws.dynamodb.model.ReturnValue
import zio.aws.dynamodb.model.PutItemResponse
import zio.aws.dynamodb.model.primitives.TableName
import zio.dynamodb.{ DynamoDBExecutor, Item => DynamoItem, PrimaryKey }
import zio.aws.dynamodb.model.primitives.AttributeName
import zio.dynamodb.DynamoDBQuery.{ getItem, putItem, scanAllItem, deleteItem }
import zio.dynamodb.DynamoDBQuery
import java.util.UUID

final class ItemRepositoryLive(dynamoDbExecutor: DynamoDBExecutor) extends ItemRepository:
  private val tableName = TableName("Items")

  override def add(item: Item): IO[RepositoryError, Unit] =
    dynamoDbExecutor
      .execute(
        putItem(
          tableName,
          DynamoItem(
            "id"    -> item.id.value,
            "name"  -> item.name,
            "price" -> item.price,
          ),
        )
      )
      .map(_ => ())
      .refineOrDie {
        case e => RepositoryError(e)
      }

  override def delete(id: ItemId): IO[RepositoryError, Unit] =
    dynamoDbExecutor
      .execute(
        deleteItem(
          tableName,
          PrimaryKey("id" -> id.value),
        )
      )
      .map(_ => ())
      .refineOrDie {
        case e => RepositoryError(e)
      }

  override def getAll(): IO[RepositoryError, List[Item]] =
    dynamoDbExecutor
      .execute(
        scanAllItem(tableName)
      )
      .flatMap { streamOfItems =>
        streamOfItems
          .map(toItem)
          .runCollect
          .map(_.iterator.toList)
      }
      .refineOrDie {
        case e => RepositoryError(e)
      }

  override def getById(id: ItemId): IO[RepositoryError, Option[Item]] =
    dynamoDbExecutor
      .execute(getItem(tableName, PrimaryKey("id" -> id.value)))
      .map {
        case Some(i) => Some(toItem(i))
        case None    => None
      }
      .refineOrDie {
        case e => RepositoryError(e)
      }

  override def update(itemId: ItemId, data: ItemData): IO[RepositoryError, Option[Unit]] =
    getById(itemId).either.flatMap {
      case Left(repositoryError) => ZIO.fail(repositoryError)
      case Right(maybeItem)      =>
        maybeItem match
          case None    => ZIO.succeed(Some(()))
          case Some(_) => add(Item.withData(itemId, data)).map(Some.apply)

    }

  private def toItem(dynamoItem: DynamoItem): Item =
    val id: String        = dynamoItem.get[String]("id").fold(error => error.toString(), success => success.toString)
    val name: String      = dynamoItem.get[String]("name").fold(error => error.toString(), success => success.toString)
    val price: BigDecimal = dynamoItem.get[BigDecimal]("price").toOption.get

    Item(ItemId(UUID.fromString(id)), name, price)

object ItemRepositoryLive:

  val layer: URLayer[DynamoDBExecutor, ItemRepository] =
    ZLayer.fromZIO(for {
      dynamoDbExecutor <- ZIO.service[DynamoDBExecutor]
    } yield ItemRepositoryLive(dynamoDbExecutor))
