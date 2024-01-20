package scalac.infrastructure.dynamodb

import com.dimafeng.testcontainers.DynaliteContainer
import org.testcontainers.utility.DockerImageName
import zio._

object DynamoDbContainer:

  def make(imageName: String = "amazon/dynamodb-local") =
    ZIO.acquireRelease {
      ZIO.attempt {
        val c = new DynaliteContainer(
          dockerImageName = DockerImageName.parse(imageName)
        )
        // FIXME: create table on init
        // .configure { a =>
        //   a.withInitScript("item_schema.json")
        //   ()
        // }
        c.start()
        c
      }
    } { container =>
      ZIO.attempt(container.stop()).orDie
    }
