package feeder

import io.gatling.core.Predef._
import com.github.nscala_time.time.Imports._

import scala.util.Random

object Feeders {
  val moduleName = Iterator.continually(Map("moduleName" -> Random.alphanumeric.take(20).mkString))
  val moduleVersion = Iterator.continually(Map("moduleVersion" -> s"${Random.nextInt(10)}.${Random.nextInt(10)}.${Random.nextInt(10)}"))
}
