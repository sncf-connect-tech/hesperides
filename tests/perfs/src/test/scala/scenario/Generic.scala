package scenario

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import feeder.Feeders
import process.Modules

object Generic {
    
  val modules = scenario("Creation / modification / consultation / suppression de modules")
      .feed(Feeders.moduleName)
      .feed(Feeders.moduleVersion)
      .exec(Modules.create)
      .exec(Modules.get)
      .exec(Modules.update)
      .exec(Modules.get)
      .exec(Modules.delete)
      .exec(Modules.getNotFound)

}
