package pers.rdara.akka.http.entity.model

// For all of the utterances, expected to receive any of specified response
case class TestUtterance(utterances: List[String], responses: List[TestEntity])
