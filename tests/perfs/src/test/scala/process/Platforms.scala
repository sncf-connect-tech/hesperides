package process

object Platforms {

  val create = exec(http("createPlatform")
    .post("/applications/${applicationName}/platforms")
    .body(StringBody(
      """{
        "application_name": "${applicationName}",
        "application_version": "${applicationVersion}",
        "modules": [],
        "platform_name": "${platformName}",
        "production": false,
        "version_id": 0
      }""")).asJson
    .check(status.is(200)))

  val addModule = exec(http("addModuleToPlatform")
    .put("/applications/${applicationName}/platforms")
    .body(StringBody(
      """{
        "application_name": "${applicationName}",
        "application_version": "${applicationVersion}",
        "modules": [
          {
            "id": 0,
            "instances": [],
            "name": "${moduleName}",
            "path": "${modulePath}",
            "version": "${moduleVersion}",
            "working_copy": true
          }
        ],
        "platform_name": "${platformName}",
        "production": false,
        "version_id": 1
      }""")).asJson
    .check(status.is(200)))

  val addGlobalProperty = exec(http("addGlobalropertyToPlatform")
    .post("/applications/${applicationName}/platforms/${platformName}/properties")
    .queryParam("platform_vid", 2)
    .queryParam("path", "#")
    .body(StringBody(
      """{
        "iterable_properties": [],
        "key_value_properties": [
          {
            "name": "${propertyName}",
            "value": "${propertyValue}"
          }
        ]
      }""")).asJson
    .check(status.is(200)))

  val addModuleProperty = exec(http("addModulePropertyToPlatform")
    .post("/applications/${applicationName}/platforms/${platformName}/properties")
    .queryParam("platform_vid", 3)
    .queryParam("path", "${modulePath}#${moduleName}#${moduleVersion}#WORKINGCOPY")
    .body(StringBody(
      """{
        "iterable_properties": [],
        "key_value_properties": [
          {
            "name": "${propertyName}",
            "value": "${propertyValue}"
          }
        ]
      }""")).asJson
    .check(status.is(200)))

  val get = exec(http("getPlatform")
    .get("/applications/${applicationName}/platforms/${platformName}")
    .check(status.is(200)))

  val getNotFound = exec(http("getPlatformNotFound")
    .get("/applications/${applicationName}/platforms/${platformName}")
    .check(status.is(404)))

  val getFiles = exec(http("getPlatformFiles")
    .get("/files/applications/${applicationName}/platforms/${platformName}/${modulePath}/${moduleName}/${moduleVersion}/instances/default")
    .queryParam("isWorkingCopy", "true")
    .queryParam("simulate", "true")
    .check(status.is(200)))

  val getFile = exec(http("getPlatformFile")
    .get("/files/applications/${applicationName}/platforms/${platformName}/${modulePath}/${moduleName}/${moduleVersion}/instances/default/${templateName}")
    .queryParam("isWorkingCopy", "true")
    .queryParam("simulate", "true")
    .queryParam("template_namespace", "modules#${moduleName}#${moduleVersion}#WORKINGCOPY")
    .check(status.is(200)))

  val getGlobalProperties = exec(http("getPlatformProperties")
    .get("/applications/${applicationName}/platforms/${platformName}/properties")
    .queryParam("path", "#")
    .check(status.is(200)))

  val getModuleProperties = exec(http("getPlatformProperties")
    .get("/applications/${applicationName}/platforms/${platformName}/properties")
    .queryParam("path", "${modulePath}#${moduleName}#${moduleVersion}#WORKINGCOPY")
    .check(status.is(200)))

  val getGlobalPropertiesUsage = exec(http("getPlatformGlobalPropertiesUsage")
    .get("/applications/${applicationName}/platforms/${platformName}/global_properties_usage")
    .check(status.is(200)))

  val getInstancePropertiesModel = exec(http("getPlatformInstancePropertiesModel")
    .get("/applications/${applicationName}/platforms/${platformName}/properties/instance_model")
    .queryParam("path", "${modulePath}#${moduleName}#${moduleVersion}#WORKINGCOPY")
    .check(status.is(200)))

  val delete = exec(http("deletePlatform")
    .delete("/applications/${applicationName}/platforms/${platformName}")
    .check(status.is(200)))
}
