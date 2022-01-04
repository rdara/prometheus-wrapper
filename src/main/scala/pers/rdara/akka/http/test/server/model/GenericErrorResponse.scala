package pers.rdara.akka.http.test.server.model

case class GenericErrorResponse(message: String)

/**
 * @author Ramesh Dara
*/

object GenericErrorResponse {
  def apply(e: Throwable): GenericErrorResponse = GenericErrorResponse(e.getMessage)
}
