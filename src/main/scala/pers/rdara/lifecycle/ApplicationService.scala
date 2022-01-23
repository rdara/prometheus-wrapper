package pers.rdara.lifecycle

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import pers.rdara.akka.http.test.server.common.ApplicationContext
import pers.rdara.akka.http.test.server.model.ServiceRoutes

//text/plain response.
class ApplicationService(appContext: ApplicationContext) extends ServiceRoutes with Directives {
  override val routes: Route = {
    ignoreTrailingSlash {
      pathPrefix("application") {
        (get & path("status")) {
          complete {
            StatusCodes.OK -> "OK"
          }
        }
      }
    }
  }
}
