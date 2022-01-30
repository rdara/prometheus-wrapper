package pers.rdara.akka.http.test.server.common

import com.typesafe.config.{Config, ConfigFactory}
import scala.collection.JavaConverters._

/**
 * @author Ramesh Dara
*/

//A glimpse of typesafe:config usage
// https://github.com/lightbend/config
class ApplicationConfig(val underlying: Config) {
  private val prefix = "application"
  object server {
    private val server = s"$prefix.server"
    val scheme: String = underlying.getString(s"$server.scheme")
    val host: String = underlying.getString(s"$server.host")
    val port: Int = underlying.getInt(s"$server.port")
  }

  object metrics {
    private val metrics =  s"$prefix.metrics"
    val no_of_labels: Int = underlying.getInt(s"$metrics.no_of_labels")
//    val label_names: Seq[String] = underlying.getStringList(s"$metrics.label_names").asScala
    val label_names: Seq[String] = underlying.getString(s"$metrics.label_names").split(",").map(_.trim)
    val labelled_keys: Seq[String] = underlying.getString(s"$metrics.labelled_keys").split(",").map(_.trim)
    val default_keys: Seq[String] = underlying.getString(s"$metrics.default_keys").split(",").map(_.trim)
    if(no_of_labels < 0) {
      throw new RuntimeException(s"metrics.no_of_levels (${no_of_labels}) can't be less than zero.)")
    }
    if(label_names.length < no_of_labels) {
      throw new RuntimeException(s"metrics.label_names should have at least metrics.no_of_levels (${no_of_labels}) entries")
    }
    if(labelled_keys == default_keys) {
      throw new RuntimeException(s"metrics.labelled_keys and metrics.default_keys should be different.")
    }
  }
}

// metrics.conf wins over system properties.
object ApplicationConfig {
  val configPath = "metrics.conf"
  lazy val Default: ApplicationConfig = {
    val defaultConfig = ConfigFactory.parseResources(configPath)
    val systemConfig = ConfigFactory.systemProperties()
    val underlyingConfig = systemConfig.withFallback(defaultConfig)
    new ApplicationConfig(underlyingConfig.resolve())
  }

  def apply(): ApplicationConfig = Default

  def apply(config: Config): ApplicationConfig = {
    new ApplicationConfig(config.withFallback(Default.underlying))
  }
}
