package pers.rdara.akka.http.entity

import pers.rdara.akka.http.entity.model.{TestEntities, TestEntity, TestResponse, TestUtterance}

class SynchronousEntitiesTest extends EntitiesBaseTest {
  val testUtterance1 = TestUtterance(List("frog"), List(TestEntity("AnimalEntity", "Amphibian")))
  val testUtterance2 = TestUtterance(List("yep"), List(TestEntity("BooleanEntity", true)))
  val testUtterances = TestEntities(List(testUtterance1, testUtterance2))

  "Entities resolve" should "check entities in sequence" in {
    assert(testSynchronousCalls[TestResponse](testUtterances, 2))
  }
}
