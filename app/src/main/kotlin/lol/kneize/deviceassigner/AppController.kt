package lol.kneize.deviceassigner

import javafx.application.Platform
import tornadofx.Controller
import tornadofx.FX
import java.io.IOException
import java.util.concurrent.TimeUnit

class AppController : Controller() {
    val appView: AppView by inject()

    fun init() {
        with (config) {
                showLoginScreen("!")
        }
    }

    fun showLoginScreen(message: String, shake: Boolean = false) {
        if (FX.primaryStage.scene.root != appView.root) {
            FX.primaryStage.scene.root = appView.root
            FX.primaryStage.sizeToScene()
            FX.primaryStage.centerOnScreen()
        }

        appView.title = message

        Platform.runLater {
            if (shake) appView.shakeStage()
        }
    }


    fun collectLogs(): String {
        val proc = ProcessBuilder("idevicesyslog.exe")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText()
    }


    fun exit() {
        Platform.exit()
    }

    companion object {
        val USERNAME = "username"
        val PASSWORD = "password"
    }

}