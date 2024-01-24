package $package$.api

import $package$.api.Extensions._
import $package$.domain._
import zio._
import zio.http._
import java.util.UUID

private[api] object Utils:

  def extractUUID(str: String): IO[ValidationError, UUID] =
    ZIO
      .attempt(UUID.fromString(str))
      .refineToOrDie[NumberFormatException]
      .mapError(err => ValidationError(err.getMessage))

  def handleError(err: DomainError): UIO[Response] = err match {
    case NotFoundError          => ZIO.succeed(Response.status(Status.NotFound))
    case ValidationError(msg)   => msg.toResponseZIO(Status.BadRequest)
    case RepositoryError(cause) =>
      ZIO.logErrorCause(cause.getMessage, Cause.fail(cause)) *>
        "Internal server error, contact system administrator".toResponseZIO(Status.InternalServerError)
  }
