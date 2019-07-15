package conf

import java.lang.System._

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.{Duration, FiniteDuration}

object Config {

  val baseUrl = getProperty("baseUrl", "http://localhost:8080/rest")
  val defaultUserPerSeconds = Integer.getInteger("usersPerSecond", 4).toInt
  val duration = Duration.create(getProperty("duration", "20 second")).asInstanceOf[FiniteDuration]
  val percentOkMin = Integer.getInteger("percentOkMin", 99).toInt
  val percentile99ResponseTimeMax = Integer.getInteger("percentile99ResponseTimeMax", 12000).toInt
  val auth = getProperty("auth", "tech:password").split(":")

  val httpConf = http
    .baseUrl(baseUrl)
    .userAgentHeader("Gatling")
    .basicAuth(auth(0), auth(1))
    .disableCaching

}
