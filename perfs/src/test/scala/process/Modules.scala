package process

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Modules {

  val create = exec(http("createWorkingcopyModule")
      .post("/modules").body(StringBody("""{"name": "${moduleName}",
                                            "version": "${moduleVersion}",
                                            "working_copy": true,
                                            "technos": [],
                                            "version_id": 0
                                           }""")).asJSON
      .check(status.is(201)))

  val update = exec(http("updateModule")
    .put("/modules").body(StringBody("""{"name": "${moduleName}",
                                         "version": "${moduleVersion}",
                                         "working_copy": true,
                                         "technos": [],
                                         "version_id": 1
                                        }""")).asJSON
    .check(status.is(200)))

  val get = exec(http("getWorkingcopyModule")
    .get("/modules/${moduleName}/${moduleVersion}/workingcopy")
    .check(status.is(200)))

  val getNotFound = exec(http("getWorkingcopyModule")
    .get("/modules/${moduleName}/${moduleVersion}/workingcopy")
    .check(status.is(404)))

  val delete = exec(http("deleteWorkingcopyModule")
    .delete("/modules/${moduleName}/${moduleVersion}/workingcopy")
    .check(status.is(200)))
}
