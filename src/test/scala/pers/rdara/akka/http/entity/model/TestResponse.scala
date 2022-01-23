package pers.rdara.akka.http.entity.model

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import pers.rdara.akka.http.entity.service.InputMessage

// Consolidated Test Response that has Http Status Code, received response, unique request id to differentiate concurrently happening calls.
// Also has "verify" which verifies whether the receved response matches the expected response.
case class TestResponse(statusCode: StatusCode,
                        actualResponse: TestEntity,
                        inputMessage: InputMessage,
                        expectedResponses: List[TestEntity],
                        uniqueRequestId: String) {
  def verify: Boolean = {
    statusCode == StatusCodes.OK &&
      expectedResponses.forall { expRes ⇒
          expRes._type == actualResponse._type && expRes.value == actualResponse.value
      }
  }
}
