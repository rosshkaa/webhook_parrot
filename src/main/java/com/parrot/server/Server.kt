package com.parrot.server

import com.parrot.Main
import com.parrot.dao.UsersProvider
import org.jetbrains.exposed.sql.Database
import spark.Service
import java.util.logging.Level
import java.util.logging.Logger.getLogger

class Server private constructor() {
    companion object {
        private var instance: Server? = null

        fun getInstance(): Server {
            if (instance == null) {
                instance = Server()
            }
            return instance as Server
        }
    }

    private lateinit var httpService: Service
    private lateinit var bot: TelegramBot

    fun start(token: String, port: Int, path: String) {
        try {
            Database.connect(
                "jdbc:postgresql://127.0.0.1:5432/${Main.dbName}",
                driver = "org.postgresql.Driver",
                user = "postgres",
                password = "admin"
            )

            UsersProvider.init()
            bot = TelegramBot.getInstance()
            bot.start(token)

            httpService = Service.ignite()
            httpService.port(port)
            httpService.threadPool(350)
            httpService.internalServerError("Error: 500 internal error")

            httpService.post(path, HttpRoute(bot))
            println("Http server running on port: $port")
        } catch (e: Exception) {
            getLogger(this::class.java.name).log(Level.INFO, "Server start error", e)
        }
    }

    fun stop() {
        try {
            httpService.stop()
            bot.close()
        } catch (e: Exception) {
            getLogger(this::class.java.name).log(Level.INFO, "Server start error", e)
        }
    }
}
