package lol.kneize.deviceassigner

import javafx.application.Platform
import tornadofx.Controller
import tornadofx.FX

class AppController : Controller() {
    val appView: AppView by inject()

    fun init() {
        with (config) {
            if (containsKey(USERNAME) && containsKey(PASSWORD))
                tryLogin(string(USERNAME), string(PASSWORD), true)
            else
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

    fun tryLogin(username: String, password: String, remember: Boolean) {
        runAsync {
            username == "admin" && password == "secret"
        } ui { successfulLogin ->

            if (successfulLogin) {
                appView.clear()

                if (remember) {
                    with (config) {
                        set(USERNAME to username)
                        set(PASSWORD to password)
                        save()
                    }
                }

            } else {
                showLoginScreen("Login failed. Please try again.", true)
            }
        }
    }



    fun exit() {
        Platform.exit()
    }

    companion object {
        val USERNAME = "username"
        val PASSWORD = "password"
    }

}