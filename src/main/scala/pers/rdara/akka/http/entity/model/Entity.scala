package pers.rdara.akka.http.entity.model

import com.fasterxml.jackson.annotation.JsonTypeInfo
import pers.rdara.akka.http.entity.model.entities.{AnimalEntity, BooleanEntity, NumberEntity}

import scala.util.Try

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "_type")
trait Entity

//For every new Entity has been added, we need to explicitly handle it here.
object Entity {
  def apply(utterance: String): Entity = {
    Try(AnimalEntity(utterance)).toOption.map(return _)
    Try(BooleanEntity(utterance)).toOption.map(return _)
    Try(NumberEntity(utterance)).toOption.map(return _)

    throw new UnrecognizedEntityException(s"${utterance} is not an recognized entity")
  }
}