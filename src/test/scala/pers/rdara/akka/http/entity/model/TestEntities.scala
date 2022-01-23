package pers.rdara.akka.http.entity.model

// Acts as an indefinite cyclical iterator that leverages continually of scala Iterator.
//Each TestEntity denotes for each utterance, whats the expected response
case class TestEntities(entities: List[TestUtterance]) {

  private val testEntitiesInfiniteList = entities.map { testEntity ⇒
    (Iterator.continually(testEntity.utterances).flatten, testEntity.responses)
  }

  private val interleavedInfiniteList = Iterator.continually(testEntitiesInfiniteList).flatten

  def next(): (String, List[TestEntity]) = {
    val (testEntityIter, responses) = interleavedInfiniteList.next
    (testEntityIter.next, responses)
  }
}