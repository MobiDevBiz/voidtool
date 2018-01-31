package lol.kneize.idevicesyslog.gui

import javafx.application.Platform
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import tornadofx.*
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

//    private val serviceMessageRegex = Regex("""^\[\w+]""")
//    private val syslogMessageRegex = Regex("""^\w{3}\s+\d+\s\d+:\d+:\d+\s+[\w-\d]+\s.*?:\s(.*)$""")
//
//    private val applicationMessageRegex = Regex("""^\[(\w)+:\d+]\s*(.*)""")


    fun startCollectingLogs() {
        runAsync {
            val proc = ProcessBuilder("idevicesyslog.exe")
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start() as Process
            this@AppController.proc = proc

            val reader = proc.inputStream.bufferedReader()
            var index = 0
            var start = 0
            var end = 0
            while(true) {
                index++
                val line = reader.readLine() ?: break

//                val serviceMessageMatch = serviceMessageRegex.find(line)
//                if(serviceMessageMatch != null) {
                    runLater { appView.appendLogs(line + '\n')}//s()"Keyword is: ${serviceMessageMatch.groupValues[1]}\n") }}
//                } else {
//                    if(syslog) {
//                        buffer = matches[1]
//                    } else {
//                        buffer += line
//                        continue
//                    }
//                }
//
//                process buffer


//                when {
//                    applicationMessageRegex.matches(line) -> runLater {
//                        appView.appendLogs(line + '\n')
//                        println(line)
//                    }
//                    line.contains(appView.keyword.value, ignoreCase = true) -> runLater { appView.appendLogs("Keyword is: ${appView.keyword} $line\n") }
//                    else -> runLater { println(line) }//stopCollectingLogs()
//                }
//                if (line.contains(appView.keyword.toString(), ignoreCase = true)) {
//                    runLater {
//                        appView.appendLogs(line + '\n')
//                    }
//                } else {stopCollectingLogs()}
            }
        }
    }

    fun stopCollectingLogs() {
        val proc = this.proc
        this.proc = null
        if(proc != null) {
            proc.destroy()
        }
        runLater { appView.appendLogs("[disconnected]" + '\n') }

    }

    fun copyToClipboard() {
        val clipboard = Clipboard.getSystemClipboard()
        val content = ClipboardContent()
        val selection = appView.logsField.selectedText
        if (selection.isNotEmpty()) {
            content.putString(selection.toString())
            clipboard.setContent(content)
            appView.appendLogs("[idevicesyslog] Selection is saved to clipboard" + '\n')
        }
    }
    fun copyToFile() {
        val targetDir = System.getProperty("user.dir")
        val path = String.format("%s/logs", targetDir)
        val root = File(path)
        root.mkdirs()
        val fileName = "syslog-" + SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(Date()) + ".txt"
        val syslog = File(root, fileName)
        val fullPath = syslog.absolutePath
        syslog.writeText(appView.logsField.getText(0,appView.logsField.length).toString())
        appView.appendLogs("[idevicesyslog] Stack is saved to: " + fullPath + '\n')
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