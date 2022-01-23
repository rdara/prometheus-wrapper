package pers.rdara.akka.http.jackson

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.{ContentTypeRange, ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{MalformedRequestContentRejection, RejectionHandler}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.annotation.{JsonSetter, Nulls}
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, PropertyNamingStrategy}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect.runtime.universe._
import java.lang.reflect.{ParameterizedType, Type => JavaType}
import scala.io.{Codec, Source}
import scala.reflect.ClassTag
import scala.util.Try

/**
 * @author Ramesh Dara
 */

object JacksonUtil {

  val objectMapper: ObjectMapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .setSerializationInclusion(Include.NON_NULL)
    .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
    .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY))

  def readFromResource[T](resource: String, classType: Class[T]): Try[T] = {
    implicit val codec = Codec.UTF8.charSet
    Try(objectMapper.readValue(Source.fromResource(resource).bufferedReader(), classType))
  }

  def fromJson[T](json: String)(implicit classTag: ClassTag[T]): T = {
    objectMapper.readValue(json, classTag.runtimeClass.asInstanceOf[Class[T]])
  }

  object AkkaHttpSupport {

    val rejectionHandler: RejectionHandler = RejectionHandler
      .newBuilder()
      .handle {
        case s: MalformedRequestContentRejection ⇒
          complete {
            StatusCodes.BadRequest → s.message
          }
      }
      .result()
  }

  // Copy from https://github.com/hseeberger/akka-http-json with minor modification
  trait AkkaHttpSupport {

    def unmarshallerContentTypes: Seq[ContentTypeRange] = List(ContentTypes.`application/json`)

    private val jsonStringUnmarshaller =
      Unmarshaller.byteStringUnmarshaller
        .forContentTypes(unmarshallerContentTypes: _*)
        .mapWithCharset {
          case (ByteString.empty, _) ⇒ throw Unmarshaller.NoContentException
          case (data, charset) ⇒ data.decodeString(charset.nioCharset.name)
        }

    private def typeReference[T: TypeTag]: TypeReference[T] = {
      val t = typeTag[T]
      val mirror = t.mirror

      def mapType(t: Type): JavaType =
        if (t.typeArgs.isEmpty) {
          mirror.runtimeClass(t)
        } else {
          new ParameterizedType {
            def getRawType = mirror.runtimeClass(t)

            def getActualTypeArguments = t.typeArgs.map(mapType).toArray

            def getOwnerType = null
          }
        }

      new TypeReference[T] {
        override def getType = mapType(t.tpe)
      }
    }

    /**
     * HTTP entity => `A`
     */
    implicit def unmarshaller[A](implicit ct: TypeTag[A]): FromEntityUnmarshaller[A] = {
      jsonStringUnmarshaller.map { data ⇒
        JacksonUtil.objectMapper.readValue(data, typeReference[A]).asInstanceOf[A]
      }
    }

    /**
     * `A` => HTTP entity
     */
    implicit def marshaller[Object]: ToEntityMarshaller[Object] = {
      akka.http.javadsl.marshallers.jackson.Jackson.marshaller[Object](JacksonUtil.objectMapper)
    }

  }
}
