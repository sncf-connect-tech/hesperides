

class HesperidesApi extends Simulation {
    setUp(
        Generic.modules.inject(constantUsersPerSec(Config.defaultUserPerSeconds) during Config.duration).protocols(Config.httpConf),
        Generic.platforms.inject(constantUsersPerSec(Config.defaultUserPerSeconds) during Config.duration).protocols(Config.httpConf)
    ).assertions(
        global.successfulRequests.percent.gt(Config.percentOkMin),
        global.responseTime.percentile4.lt(Config.percentile99ResponseTimeMax)
    )  
}
