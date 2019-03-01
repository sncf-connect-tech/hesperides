package process

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Events {

  val getModules = exec(http("getModuleEvents")
    .get("/events/modules/${moduleName}/${moduleVersion}/workingcopy")
    .check(status.is(200)))

  val getLegacyModules= exec(http("getLegacyModuleEvents")
    .get("/events/module-${moduleName}-${moduleVersion}-wc")
    .check(status.is(200)))

  val getPlatforms = exec(http("getPlatformEvents")
    .get("/events/platforms/${applicationName}/${platformName}")
    .check(status.is(200)))

  val getLegacyPlatforms= exec(http("getLegacyPlatformEvents")
    .get("/events/platform-${applicationName}-${platformName}")
    .check(status.is(200)))
}
