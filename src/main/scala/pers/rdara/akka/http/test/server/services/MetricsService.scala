package pers.rdara.akka.http.test.server.services

import akka.http.scaladsl.server.Route
import pers.rdara.akka.http.test.server.common.ApplicationContext
import pers.rdara.akka.http.test.server.model.ServiceRoutes

/**
  * @author Ramesh Dara
  * @since Jun-2019
  */
class MetricsService(appContext: ApplicationContext) extends ServiceRoutes {

  override val routes: Route = {
    Metrics.metricsRoute(appContext.ec)
  }

}
