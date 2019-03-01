package process

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Modules {

  val create = exec(http("createWorkingcopyModule")
    .post("/modules")
    .body(StringBody("""{
      "name": "${moduleName}",
      "version": "${moduleVersion}",
      "working_copy": true,
      "technos": [],
      "version_id": 0
    }""")).asJSON
    .check(status.is(201)))

  val addTemplate = exec(http("addTemplateToModule")
    .post("/modules/${moduleName}/${moduleVersion}/workingcopy/templates")
    .body(StringBody("""{
      "name": "${templateName}",
      "filename": "${templateName}",
      "location": "${templateDir}",
      "content": "${templateContent}{{${propertyName}}}${templateContent}",
      "rights": {},
      "version_id": 0
    }""")).asJSON
    .check(status.is(201)))

  val get = exec(http("getWorkingcopyModule")
    .get("/modules/${moduleName}/${moduleVersion}/workingcopy")
    .check(status.is(200)))

  val getNotFound = exec(http("getWorkingcopyModule")
    .get("/modules/${moduleName}/${moduleVersion}/workingcopy")
    .check(status.is(404)))

  val getModel = exec(http("getWorkingcopyModuleModel")
    .get("/modules/${moduleName}/${moduleVersion}/workingcopy/model")
    .check(status.is(200)))

  val delete = exec(http("deleteWorkingcopyModule")
    .delete("/modules/${moduleName}/${moduleVersion}/workingcopy")
    .check(status.is(200)))
}
