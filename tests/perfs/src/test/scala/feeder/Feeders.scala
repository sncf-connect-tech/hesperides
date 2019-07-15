package feeder

import scala.util.Random

object Feeders {
  val moduleName = Iterator.continually(Map("moduleName" -> Random.alphanumeric.take(20).mkString))
  val moduleVersion = Iterator.continually(Map("moduleVersion" -> s"${Random.nextInt(10)}.${Random.nextInt(10)}.${Random.nextInt(10)}"))
  val modulePath = Iterator.continually(Map("modulePath" -> s"${Random.alphanumeric.take(3).mkString.toUpperCase()}"))
  val templateName = Iterator.continually(Map("templateName" -> s"${Random.alphanumeric.take(10).mkString}.${Random.alphanumeric.take(3).mkString}"))
  val templateDir = Iterator.continually(Map("templateDir" -> s"${Random.alphanumeric.take(3).mkString}/${Random.alphanumeric.take(3).mkString}/${Random.alphanumeric.take(6).mkString}"))
  val templateContent = Iterator.continually(Map("templateContent" -> s"${Random.alphanumeric.take(100).mkString}"))
  val applicationName = Iterator.continually(Map("applicationName" -> Random.alphanumeric.take(3).mkString.toUpperCase()))
  val applicationVersion = Iterator.continually(Map("applicationVersion" -> s"${Random.nextInt(10)}.${Random.nextInt(10)}.${Random.nextInt(10)}"))
  val platformName = Iterator.continually(Map("platformName" -> s"${Random.alphanumeric.take(3).mkString.toUpperCase()}"))
  val propertyName = Iterator.continually(Map("propertyName" -> s"${Random.alphanumeric.take(10).mkString}"))
  val propertyValue = Iterator.continually(Map("propertyValue" -> s"${Random.alphanumeric.take(4).mkString}"))
}
