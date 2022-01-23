package pers.rdara.akka.http.entity.service

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import pers.rdara.akka.http.entity.model.Entity
import pers.rdara.akka.http.jackson.JacksonUtil
import pers.rdara.akka.http.test.server.common.ApplicationContext
import pers.rdara.akka.http.test.server.model.ServiceRoutes

case class message(msg: String)
case class InputMessage(utterance: String)

class EntityService(appContext: ApplicationContext) extends ServiceRoutes with Directives with JacksonUtil.AkkaHttpSupport {
  override val routes: Route = {
    ignoreTrailingSlash {
      pathPrefix("entities") {
        (post & path("resolve")) {
          entity(as[InputMessage]) { inputMessage =>
            complete {
              StatusCodes.OK -> Entity(inputMessage.utterance)
            }
          }
        }
      }
    }
  }
}
