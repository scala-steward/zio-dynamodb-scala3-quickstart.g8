package scalac.api

import scalac.domain.{ Item, ItemId }
import zio.json._
import java.util.UUID
import zio.json.ast.Json
import zio.json.internal.Write

final case class UpdateItemRequest(name: String, price: BigDecimal)
final case class PartialUpdateItemRequest(name: Option[String], price: Option[BigDecimal])
final case class CreateItemRequest(name: String, price: BigDecimal)

trait JsonSupport:
  implicit val itemIdEncoder: JsonEncoder[ItemId] = JsonEncoder[UUID].contramap(_.value)
  implicit val itemEncoder: JsonEncoder[Item]     = DeriveJsonEncoder.gen[Item]

  implicit val updateItemDecoder: JsonDecoder[UpdateItemRequest] = DeriveJsonDecoder.gen[UpdateItemRequest]

  implicit val partialUpdateItemDecoder: JsonDecoder[PartialUpdateItemRequest] =
    DeriveJsonDecoder.gen[PartialUpdateItemRequest]

  implicit val createItemDecoder: JsonDecoder[CreateItemRequest] = DeriveJsonDecoder.gen[CreateItemRequest]

  implicit val unitEncoder: JsonEncoder[Unit] = new JsonEncoder[Unit] {
    def f(unit: Unit): String = "unit"

    def unsafeEncode(
        a: Unit,
        indent: Option[Int],
        out: Write,
      ): Unit = out.write("unit")

    final override def toJsonAST(a: Unit): Either[String, Json] = Right(Json.Str(f(a)))
  }

object JsonSupport extends JsonSupport
