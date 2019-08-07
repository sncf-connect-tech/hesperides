package conf

import java.lang.System._

object Config {

  val baseUrl = getProperty("baseUrl", "http://localhost:8080/rest")
  val defaultUserPerSeconds = Integer.getInteger("usersPerSecond", 5).toInt
  val duration = Duration.create(getProperty("duration", "60seconds")).asInstanceOf[FiniteDuration]
  val percentOkMin = Integer.getInteger("percentOkMin", 99).toInt
  val percentile99ResponseTimeMax = Integer.getInteger("percentile99ResponseTimeMax", 5000).toInt
  val auth = getProperty("auth", "tech:password").split(":")

  val httpConf = http
    .baseUrl(baseUrl)
    .userAgentHeader("Gatling")
    .basicAuth(auth(0), auth(1))
    .disableCaching

}
