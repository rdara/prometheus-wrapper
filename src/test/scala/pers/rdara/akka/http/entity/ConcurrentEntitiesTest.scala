package pers.rdara.akka.http.entity

import pers.rdara.akka.http.entity.model.{TestEntities, TestResponse}
import pers.rdara.akka.http.jackson.JacksonUtil

class ConcurrentEntitiesTest extends EntitiesBaseTest {
  lazy val testdata: TestEntities = JacksonUtil.readFromResource[TestEntities]("EntitiesUtterances.json", classOf[TestEntities]).get

  "Entities resolve" should "check several entities aas in EntityUtterances.json" in {
    assert(testAsynchronousCalls[TestResponse](testdata, 256))
  }
}
