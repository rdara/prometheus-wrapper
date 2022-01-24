package pers.rdara.akka.http.entity.model.entities

import pers.rdara.akka.http.entity.SimilarityMatch
import pers.rdara.akka.http.entity.model.{Entity, EntityDefinition, EntityValue, UnrecognizedEntityException}

case class BooleanEntity(value: Boolean) extends Entity {
  override def toString: String = value.toString
}

object BooleanEntity {
  private val animalEntityDefinition = EntityDefinition(
    "Boolean",
    List(
      EntityValue("true", List("yes", "s", "yep")),
      EntityValue("false", List("no", "nope"))
    ))

  def apply(text: String): BooleanEntity = {
    animalEntityDefinition.values.map { entityValue =>
      if(SimilarityMatch.matchesAny(text, entityValue.value :: entityValue.synonyms)) {
        return new BooleanEntity(entityValue.value.toBoolean)
      }
    }
    throw new UnrecognizedEntityException(s"${text} is not Boolean")
  }
}


