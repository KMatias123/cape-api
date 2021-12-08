package me.kmatias.capeapi

import org.shanerx.mojang.Mojang
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CapeRestController {

    @GetMapping("/capestatus")
    fun askCape(@RequestParam name: String): RequestData? {

        val mojang = Mojang().connect()

        CapeApiApplication.logger.info("Checking $name from Mojang")
        if (CapeApiApplication.uuids.contains(mojang.getUUIDOfUsername(name))) {
            CapeApiApplication.logger.info("$name has a cape")
            return RequestData(true)
        }

        CapeApiApplication.logger.info("$name doesn't have a cape")
        return RequestData(false)
    }
}