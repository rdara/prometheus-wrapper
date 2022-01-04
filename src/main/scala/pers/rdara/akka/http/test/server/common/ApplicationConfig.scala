package pers.rdara.akka.http.test.server.common

import com.typesafe.config.{Config, ConfigFactory}

/**
 * @author Ramesh Dara
*/

//A glimpse of typesafe:config usage
// https://github.com/lightbend/config
class ApplicationConfig(val underlying: Config) {
  private val prefix = "akka.http.test"
  object server {
    private val server = s"$prefix.server"
    val scheme: String = underlying.getString(s"$server.scheme")
    val host: String = underlying.getString(s"$server.host")
    val port: Int = underlying.getInt(s"$server.port")
  }
}

// test.conf wins over system properties.
object ApplicationConfig {
  val configPath = "test.conf"
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
