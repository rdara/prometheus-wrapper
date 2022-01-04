package pers.rdara.akka.http.test.server.model

import akka.http.scaladsl.server

/**
 * @author Ramesh Dara
 * @since Jan-2022
 */
trait ServiceRoutes {

  val routes: server.Route

}
