package pers.rdara.akka.http.entity.model.entities

import pers.rdara.akka.http.entity.SimilarityMatch
import pers.rdara.akka.http.entity.model.{Entity, EntityDefinition, EntityValue, UnrecognizedEntityException}

case class AnimalEntity(value: String) extends Entity {
  override def toString: String = value
}

object AnimalEntity {

  //Credit: https://examples.yourdictionary.com/basic-types-of-animals-and-their-characteristics.html
  private val animalEntityDefinition = EntityDefinition(
    "Animal",
    List(
      EntityValue("Amphibian", List("frog", "newt", "salamander", "toad")),
      EntityValue("Bird", List("albatrosses", "chicken", "hummingbird", "falcon", "flamingo", "ostrich", "owl", "parrot", "penguin", "pigeon")),
      EntityValue("Fish", List("eel", "hagfish", "lamprey", "minnow", "ray", "salmon", "seahorse", "shark")),
      EntityValue("Mammal", List("aardvark", "bat", "elephant", "hamster", "human", "rabbit", "rhinocerose", "whale")),
      EntityValue("Reptile", List("crocodile", "gecko", "lizard", "sea turtle", "snake", "tortoise", "turtle"))
    ))

  def apply(text: String): AnimalEntity = {
    animalEntityDefinition.values.foreach(entityValue => {
      if (SimilarityMatch.matchesAny(text, entityValue.synonyms)) {
        return new AnimalEntity(entityValue.value)
      }
    })
    throw new UnrecognizedEntityException(s"$text is not matched to any of Animal Entity Classifications")
  }
}