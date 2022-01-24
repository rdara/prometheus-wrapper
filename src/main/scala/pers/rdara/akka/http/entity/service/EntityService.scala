package pers.rdara.akka.http.entity.service

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.typesafe.scalalogging.LazyLogging
import pers.rdara.akka.http.entity.model.Entity
import pers.rdara.akka.http.jackson.JacksonUtil
import pers.rdara.akka.http.test.server.common.ApplicationContext
import pers.rdara.akka.http.test.server.model.ServiceRoutes

case class message(msg: String)
case class InputMessage(utterance: String)

class EntityService(appContext: ApplicationContext) extends ServiceRoutes
  with Directives
  with LazyLogging
  with JacksonUtil.AkkaHttpSupport {

  override val routes: Route = {
    ignoreTrailingSlash {
      pathPrefix("entities") {
        (post & path("resolve")) {
          entity(as[InputMessage]) { inputMessage =>
            complete {
              val response = Entity(inputMessage.utterance)
              logger.info(s"Entity Resolver: ${inputMessage.utterance} is resolved as ${JacksonUtil.objectMapper.writeValueAsString(response)}")
              StatusCodes.OK -> response
            }
          }
        }
      }
    }
  }
}
