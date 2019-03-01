package scenario

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import feeder.Feeders
import process.Events
import process.Modules
import process.Platforms

object Generic {

  val modules = scenario("Creation / modification / consultation / suppression de modules")
      .feed(Feeders.moduleName)
      .feed(Feeders.moduleVersion)
      .feed(Feeders.templateName)
      .feed(Feeders.templateDir)
      .feed(Feeders.templateContent)
      .feed(Feeders.propertyName)
      .exec(Modules.create)
      .exec(Modules.get)
      .exec(Modules.addTemplate)
      .exec(Modules.get)
      .exec(Modules.getModel)
      .exec(Modules.delete)
      .exec(Modules.getNotFound)

  val platforms = scenario("Creation / modification / consultation / suppression de plateformes")
      .feed(Feeders.moduleName)
      .feed(Feeders.moduleVersion)
      .feed(Feeders.modulePath)
      .feed(Feeders.templateName)
      .feed(Feeders.templateDir)
      .feed(Feeders.templateContent)
      .feed(Feeders.applicationName)
      .feed(Feeders.applicationVersion)
      .feed(Feeders.platformName)
      .feed(Feeders.propertyName)
      .feed(Feeders.propertyValue)
      .exec(Platforms.create)
      .exec(Modules.create)
      .exec(Modules.addTemplate)
      .exec(Platforms.addModule)
      .exec(Platforms.addGlobalProperty)
      .exec(Platforms.addModuleProperty)
      .exec(Platforms.get)
      .exec(Platforms.getFiles)
      .exec(Platforms.getFile)
      .exec(Platforms.getGlobalProperties)
      .exec(Platforms.getModuleProperties)
      .exec(Platforms.getInstancePropertiesModel)
      .exec(Platforms.getGlobalPropertiesUsage)
      .exec(Platforms.delete)
      .exec(Modules.delete)
      .exec(Platforms.getNotFound)
      .exec(Events.getModules)
      .exec(Events.getLegacyModules)
      .exec(Events.getPlatforms)
      .exec(Events.getLegacyPlatforms)
}
