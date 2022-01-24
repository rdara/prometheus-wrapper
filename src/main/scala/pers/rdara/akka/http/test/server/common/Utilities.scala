package pers.rdara.akka.http.test.server.common

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader

import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * @author Ramesh Dara
 * @since Jan-2022
 */
object Utilities {
  val startTimeHeaderName = "PROMETHEUS-START-TIME-HEADER"
  val uniqueIdHeaderName = "UNIQUE-REQUEST-ID"

  def getDuration(request: HttpRequest): Long = {
    val startTimeHeader = request.getHeader(startTimeHeaderName).orElse(RawHeader(startTimeHeaderName, System.currentTimeMillis.toString))
    val startTimeInMillis = startTimeHeader.value.toLong
    System.currentTimeMillis - startTimeInMillis
  }

  def getBaseUrl(appContext: ApplicationContext) = {
    s"${appContext.appConfig.server.scheme}://${appContext.appConfig.server.host}:${appContext.appConfig.server.port}"
  }

  def getUniqueRequestId(prefix: String = "", suffix: String = ""): String = {
    val randomNumber: Long = (0xffffffffL * Math.random()).toLong
    val time               = new SimpleDateFormat("hh:mm:ss.SSS").format(Calendar.getInstance().getTime())
    f"${prefix}|${randomNumber}%08X|$time|${suffix}"
  }

}
