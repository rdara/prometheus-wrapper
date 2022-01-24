package pers.rdara.akka.http.entity.model.entities

import pers.rdara.akka.http.entity.model.{Entity, UnrecognizedEntityException}

case class NumberEntity(value: Double) extends Entity {
  override def toString: String = value.toString
}

object NumberEntity {
  def apply(text: String): NumberEntity = {
    try {
      new NumberEntity(text.toDouble)
    } catch {
      case _ => throw new UnrecognizedEntityException(s"${text} is not Number")
    }
  }
}



