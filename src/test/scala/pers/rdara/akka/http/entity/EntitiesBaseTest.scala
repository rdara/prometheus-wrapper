package pers.rdara.akka.http.entity

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpMethod, HttpRequest, StatusCodes, Uri}
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.HttpProtocols.`HTTP/1.1`
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.scalatest.time.{Minutes, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers}
import pers.rdara.akka.http.entity.model.{TestEntities, TestEntity, TestResponse}
import pers.rdara.akka.http.entity.service.InputMessage
import pers.rdara.akka.http.jackson.JacksonUtil
import pers.rdara.akka.http.test.server.common.{ApplicationConfig, ApplicationContext}

import java.text.SimpleDateFormat
import java.util.Calendar
import scala.concurrent.Future
import scala.reflect.ClassTag
import EntitiesBaseTest._
import pers.rdara.akka.http.test.server.common.Utilities.{getBaseUrl, getUniqueRequestId}

import scala.io.Source
import scala.util.Try

//Base test class
abstract class EntitiesBaseTest extends FlatSpec with TestExecutionContext with Matchers with BeforeAndAfterEach with BeforeAndAfterAll with JacksonUtil.AkkaHttpSupport {

  override def beforeAll(): Unit = {
    assume(isApplicationReady, s"The application that exposes entities endpoint, ${entitiesUri}, is not ready and hence ignoring the test")
  }


  //The derived test can add that test specific headers
  def defaultHeaders: List[HttpHeader] = List[HttpHeader](
      //Authorization(GenericHttpCredentials("ID", "id token"))
  )

  //The count number of tests will be run sequentially
  def testSynchronousCalls[T: ClassTag](testEntities: TestEntities,
                                        count: Int,
                                        headers: List[HttpHeader] = this.defaultHeaders): Boolean = {
    val startTime = System.currentTimeMillis
    val results = (1 to count).map { _ ⇒
      val (utterance, responses) = testEntities.next()
      val userMessage               = InputMessage(utterance)
        whenReady(sendRequest[T](POST, entitiesUri, headers, userMessage, responses)) { response ⇒
          response
        }
    }
    displayResults(results, startTime)
  }

  //The count number of runs will be run concurrently
  def testAsynchronousCalls[T: ClassTag](testEntities: TestEntities,
                                         count: Int,
                                         headers: List[HttpHeader] = this.defaultHeaders): Boolean = {
    val startTime = System.currentTimeMillis
    val fResponses = (1 to count).map { _ ⇒
      val (utterance, responses) = testEntities.next()
      val userMessage               = InputMessage(utterance)
      sendRequest[T](POST, entitiesUri, headers, userMessage, responses)
    }
    whenReady(Future.sequence(fResponses), timeout(Span(10, Minutes)), interval(Span(2, Seconds))) { results ⇒
      displayResults(results, startTime)
    }
  }

  def sendRequest[T: ClassTag](method: HttpMethod,
                               uri: Uri,
                               headers: List[HttpHeader],
                               userMessage: InputMessage,
                               responses: List[TestEntity]): Future[TestResponse] = {
    val uniqueRequestId   = getUniqueRequestId(userMessage.utterance.take(10))
    val requestHeaders   = headers :+ RawHeader("UNIQUE-REQUEST-ID", uniqueRequestId)
    val entity        = HttpEntity(ContentTypes.`application/json`, JacksonUtil.objectMapper.writeValueAsString(userMessage))
    val request       = new HttpRequest(method, uri, requestHeaders, entity, `HTTP/1.1`)
    http.singleRequest(request).flatMap { res ⇒
      Unmarshal(res.entity).to[TestEntity].map { testEntity ⇒
        TestResponse(res.status, testEntity, userMessage, responses, uniqueRequestId)
      }
    }
  }

  def displayResults(results: IndexedSeq[TestResponse], startTime: Long): Boolean = {
    val duration = (System.currentTimeMillis - startTime) / 1000.0
    val times    = results.size
    val goodResults = if (results.nonEmpty) {
      val good = results.map { result ⇒
        result.verify
      }.count(x ⇒ x)
      val ises = results.map { result ⇒
        result.statusCode != StatusCodes.OK
      }.count(x ⇒ x)
      println(s"Errors that aren't StatusCodes.OK = $ises")
      println(s"Success Percentage = ${(100 * good) / times}. Successful Calls = $good and Failed Calls = ${times - good}.")
      good
    } else {
      0
    }
    println(s"Took $duration seconds to complete $times  calls in the test.")
    times == goodResults //Are they any bad responses?
  }

}

object EntitiesBaseTest {
  val baseUri = getBaseUrl(ApplicationContext.Default)
  val entitiesUri = Uri(s"$baseUri/entities/resolve")
  val applicationStatusUri = Uri(s"$baseUri/application/status")

  val isApplicationReady =  Try(Source.fromURL(s"${applicationStatusUri}").mkString.trim.equals("OK")).getOrElse(false)

  sys addShutdownHook {
    //Add any cleanup wrt entities testing here
  }
}
