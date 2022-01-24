package pers.rdara.akka.http.entity

import org.apache.commons.text.similarity.{JaroWinklerSimilarity, LevenshteinDistance}

object SimilarityMatch {
  //Ref:   http://en.wikipedia.org/wiki/Jaro%E2%80%93Winkler_distance
  private val jaroWinkler = new JaroWinklerSimilarity()
  private val levenshtein = LevenshteinDistance.getDefaultInstance()
  private val ACCEPTABLE_SCORE = 90

  // Try bot the JaroWrinkler and Levenshtein Distances to identify similarity score between 2 words.
  private def similarityScore(source: String, target: String): Int = {
    val modifiedSource = source.toLowerCase().trim
    val modifiedTarget = target.toLowerCase().trim

    val jaroScore = (jaroWinkler.apply(modifiedSource, modifiedTarget) * 100).toInt
    val distance = levenshtein.apply(modifiedSource, modifiedTarget)
    val levenshteinScore = ((source.length - distance) * 100) / source.length
    Math.max(jaroScore, levenshteinScore)
  }

  //If source is similar to any of the given choices, then returns true. Otherwise false.
  def matchesAny(source: String, choices: Seq[String]): Boolean = {
    choices.map { choice =>
      if (similarityScore(source, choice) > ACCEPTABLE_SCORE) {
        return true
      }
    }
    false
  }

}
