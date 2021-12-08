package me.kmatias.capeapi

import org.shanerx.mojang.Mojang
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

@SpringBootApplication
class CapeApiApplication {

    companion object Public {

        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger("CapeAPI")

        @Volatile
        @JvmStatic
        var uuids = ArrayList<String>()

        @JvmStatic
        val capeUserFile = File("./cape-users")
    }


}

fun main(args: Array<String>) {

    if (CapeApiApplication.capeUserFile.exists()) {
        val reader = FileReader(CapeApiApplication.capeUserFile)
        reader.readLines().forEach { line ->
            CapeApiApplication.uuids.add(line)
        }
        reader.close()
    }


    runApplication<CapeApiApplication>(*args)
    startInputThread()
    Runtime.getRuntime().addShutdownHook(Thread { exit() })
}

fun exit() {


    val fileWriter = FileWriter(CapeApiApplication.capeUserFile)

    CapeApiApplication.uuids.forEach { uuid ->
        fileWriter.write(uuid)
        fileWriter.write("\n")
    }

    fileWriter.flush()
    fileWriter.close()
}

fun startInputThread() {
    val inputThread = Thread {

        val strReader = Scanner(System.`in`)

        while (strReader.hasNext()) {
            executeCommand(strReader.nextLine())
        }

        Thread.sleep(50)
    }
    inputThread.isDaemon = true
    inputThread.start()
}

// todo: command manager and separate classes for them
fun executeCommand(string: String) {
    val args = string.split(" ")


    if (args[0].equals("help", true)) {
        CapeApiApplication.logger.info(
            "\n" +
                    "commands:\n" +
                    ":: add  [name]    -> adds a player to the uuid list\n" +
                    ":: rm   [name]    -> removes a player from the uuid list\n" +
                    ":: help           -> this\n" +
                    ":: list [convert] -> lists all the cape users, convert is a boolean and converts the uuids to names, which takes a while because of using the Mojang api"
        )
    } else if (args[0].equals("add", true)) {
        val name = args[1]
        if (name.isEmpty()) {
            CapeApiApplication.logger.warn("Syntax error! args[1] is empty")
            return
        }
        val mojangAPI = Mojang().connect()
        val uuid = mojangAPI.getUUIDOfUsername(name)

        if (uuid.isEmpty()) {
            CapeApiApplication.logger.warn("Syntax error! uuid is empty which means that the player name is invalid")
            return
        }

        CapeApiApplication.uuids.add(uuid)

        CapeApiApplication.logger.info("Added $name to the cape user list")

    } else if (args[0].equals("rm", true)) {

        val name = args[1]
        if (name.isEmpty()) {
            CapeApiApplication.logger.warn("Syntax error! args[1] is empty")
            return
        }
        val mojangAPI = Mojang().connect()
        val uuid = mojangAPI.getUUIDOfUsername(name)

        if (uuid.isEmpty()) {
            CapeApiApplication.logger.warn("Syntax error! uuid is empty which means that the player name is invalid")
            return
        }

        if (!CapeApiApplication.uuids.contains(uuid)) {
            CapeApiApplication.logger.warn("That user was never a cape user!")
            return
        }

        CapeApiApplication.uuids.remove(uuid)

        CapeApiApplication.logger.info("Removed $name from the cape user list")

    } else if (args[0].equals("list", true)) {

        if (args.size > 1 && args[1].equals("true")) {
            val names = ArrayList<String>()
            val mojangAPI = Mojang().connect()


            CapeApiApplication.uuids.forEach { uuid ->
                names.add(mojangAPI.getPlayerProfile(uuid).username)
            }

            CapeApiApplication.logger.info("cape user name list:")

            names.forEach { name ->
                CapeApiApplication.logger.info(name)
            }
        } else {
            CapeApiApplication.logger.info("cape user uuid list:")

            CapeApiApplication.uuids.forEach { uuid ->
                CapeApiApplication.logger.info(uuid)
            }
        }
    } else if (args[0].equals("clear", true)) {
        if (args.size > 1 && args[1].equals("confirm")) {
            CapeApiApplication.uuids.clear();
            CapeApiApplication.logger.warn("Cleared the cape user list!")
        } else {
            CapeApiApplication.logger.warn("Do you really want to clear all the cape users? If you really want to do it, type \"clear confirm\"")
        }
    }
}