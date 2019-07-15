import _root_.scenario.Generic
import conf.Config
import io.gatling.core.Predef.{Simulation, _}

class HesperidesApi extends Simulation {
  setUp(
    Generic.modules.inject(constantUsersPerSec(Config.defaultUserPerSeconds) during Config.duration).protocols(Config.httpConf),
    Generic.platforms.inject(constantUsersPerSec(Config.defaultUserPerSeconds) during Config.duration).protocols(Config.httpConf)
  ).assertions(
    global.successfulRequests.percent.gt(Config.percentOkMin),
    global.responseTime.percentile4.lt(Config.percentile99ResponseTimeMax)
  )
}
