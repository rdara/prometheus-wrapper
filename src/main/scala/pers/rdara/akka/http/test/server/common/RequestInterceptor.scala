package pers.rdara.akka.http.test.server.common

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.mapRequest
import akka.stream.Materializer
import pers.rdara.akka.http.test.server.TestServer
import pers.rdara.akka.http.test.server.common.Utilities.{uniqueIdHeaderName, getUniqueRequestId}


trait RequestInterceptor {
  this: TestServer =>

  def injectUniqueIDHeader(implicit materializer: Materializer): Directive0 = {
    mapRequest(request => {
      if(request.getHeader(uniqueIdHeaderName).isEmpty()) {
        request.addHeader(RawHeader(uniqueIdHeaderName, getUniqueRequestId("PROM-WRAP")))
      } else {
        request
      }
    })
  }

  def logRequestUniqueId(implicit materializer: Materializer): Directive0 = {
    mapRequest(request => {
      logger.info(s"Received ${request.uri.path} request with ${request.getHeader(uniqueIdHeaderName).get()}")
      request
    })
  }
}