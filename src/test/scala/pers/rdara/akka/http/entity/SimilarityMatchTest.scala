package pers.rdara.akka.http.entity

import org.scalatest.{FlatSpec, Matchers}

class SimilarityMatchTest extends FlatSpec with Matchers {

  "Similarity Match" should "sanity check" in {
    val text = "frog"
    val shouldMatch = List("frg", "fro", "rog", "fog")
    val shouldnotMatch = List("f", "fr", "xyz")
    shouldMatch.map { choice =>
       SimilarityMatch.matchesAny(text, List(choice)) shouldBe true
    }
    shouldnotMatch.map { choice =>
      SimilarityMatch.matchesAny(text, List(choice)) shouldBe false
    }
  }
}
