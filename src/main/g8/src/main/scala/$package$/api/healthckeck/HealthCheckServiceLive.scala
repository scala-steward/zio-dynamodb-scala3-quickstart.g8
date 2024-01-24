package $package$.api.healthcheck

import zio.aws.dynamodb
import zio.aws.dynamodb.DynamoDb

import zio.*
import zio.aws.dynamodb.model.ListTablesRequest

final class HealthCheckServiceLive(dynamoDb: DynamoDb)(implicit trace: zio.Trace) extends HealthCheckService {

  override def check: UIO[DbStatus] =
    dynamoDb
      .listTables(ListTablesRequest(None, None))
      .runCount
      .either
      .map(e => DbStatus(e.isRight))
}

object HealthCheckServiceLive:

  val layer: URLayer[DynamoDb, HealthCheckServiceLive] = ZLayer {
    for {
      dynamoDb <- ZIO.service[DynamoDb]
    } yield HealthCheckServiceLive(dynamoDb)
  }
