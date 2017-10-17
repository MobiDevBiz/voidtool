package lol.kneize.deviceassigner

import javafx.application.Platform
import javafx.scene.input.Clipboard
import tornadofx.Controller
import tornadofx.FX
import java.io.IOException
import java.util.concurrent.TimeUnit
import sun.text.normalizer.UTF16.append
import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
import tornadofx.runLater
import javafx.scene.input.ClipboardContent
import tornadofx.FileChooserMode
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class AppController : Controller() {
    val appView: AppView by inject()
    var proc: Process? = null

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


    fun startCollectingLogs() {
        runAsync {
            val proc = ProcessBuilder("idevicesyslog.exe")
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start() as Process
            this@AppController.proc = proc

            val reader = proc.inputStream.bufferedReader()
            while(true) {
                val line = reader.readLine() ?: break
                runLater {
                    appView.appendLogs(line + '\n')
                }
            }
        }
    }

    fun stopCollectingLogs() {
        val proc = this.proc
        this.proc = null
        if(proc != null) {
            proc.destroy()
        }
    }

    fun copyToClipboard() {
        val clipboard = Clipboard.getSystemClipboard()
        val content = ClipboardContent()
        content.putString(appView.logsField.selectedText.toString())
        clipboard.setContent(content)
        appView.appendLogs("[idevicesyslog] Selection is saved to clipboard" + '\n')
    }
    fun copyToFile() {
        val fileName = "syslog-" + SimpleDateFormat("yyyy-MM-dd-hh-mm").format(Date())
        val syslog = File(fileName)
        syslog.writeText(appView.logsField.selectedText.toString())
        appView.appendLogs("[idevicesyslog] Selection is saved to: " + fileName + '\n')
    }

    fun clearLogs() {
        appView.logsField.clear()
        appView.appendLogs("[idevicesyslog] view is cleared" + '\n')
    }

    fun exit() {
        stopCollectingLogs()
        Platform.exit()
    }





}