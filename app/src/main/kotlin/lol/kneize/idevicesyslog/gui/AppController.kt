package lol.kneize.idevicesyslog.gui

import javafx.application.Platform
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import lol.kneize.idevicesyslog.gui.OS.OS
import tornadofx.Controller
import tornadofx.FX
import tornadofx.runLater
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*


class AppController : Controller() {
    val appView: AppView by inject()
    var proc: Process? = null
    private val lineDivider = System.getProperty("line.separator")


    

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

    private val serviceMessageRegex = Regex("""^\[\w+]""")
    private val syslogMessageRegex = Regex("""^(\w{3})\s+\d+\s\d+:\d+:\d+\s+[\w-\d]+\s.*?:\s(.*)$""")
    private val applicationMessageRegex = Regex("""^\[(\w)+:\d+]\s*(.*)""")
    private val devicePropertyRegex = Regex("""^(\w+:\s)(.*)$""")

    fun startCollectingLogs() {
        runAsync {
            val proc = ProcessBuilder(OS.getActions().executable("idevicesyslog"))
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start() as Process
            this@AppController.proc = proc
            val reader = proc.inputStream.bufferedReader()
            var switch = false
            while (true) {
                val line = reader.readLine() ?: break
                if (line.matches(serviceMessageRegex) || line.matches(applicationMessageRegex)) {
                    runLater { appView.appendLogs(line + '\n') }
                } else if (line.matches(syslogMessageRegex)) {
                    if (appView.keyWord.isEmpty()) {
                        runLater { appView.appendLogs(line + '\n') }
                        runLater { System.gc() }
                    } else if (appView.keyWord.isNotEmpty() && line.contains(appView.keyWord, ignoreCase = true)) {
                        switch = !switch

                        runLater { appView.appendLogs(line + '\n') }
                        runLater { System.gc() }
                    }
                } else if (!line.matches(applicationMessageRegex) && switch) {
                    runLater { appView.appendLogs(line + '\n') }
                }
            }
        }
    }

    fun startAppendingRows() {
        runAsync {
            val proc = ProcessBuilder(OS.getActions().executable(OS.getActions().executable("idevicesyslog")))
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start() as Process
            this@AppController.proc = proc
            val reader = proc.inputStream.bufferedReader()
            var switch = false
            while (true) {
                val line = reader.readLine() ?: break
                if (line.matches(serviceMessageRegex) || line.matches(applicationMessageRegex)) {
                    runLater { appView.logMessage.add(line+'\n') }
                } else if (line.matches(syslogMessageRegex)) {
                    if (appView.keyWord.isEmpty()) {
                        runLater { appView.logMessage.add(line+'\n') }
                        runLater { System.gc() }
                    } else if (appView.keyWord.isNotEmpty() && line.contains(appView.keyWord, ignoreCase = true)) {
                        switch = !switch
                        runLater { appView.logMessage.add(line+'\n') }
                        runLater { System.gc() }
                    }
                } else if (!line.matches(applicationMessageRegex) && switch) {
                    runLater { appView.appendLogs(line + '\n') }
                }
            }
        }
    }

    fun stopCollectingLogs() {
        this.proc?.destroy()
        this.proc = null
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
        val iPad = getDeviceInfo("ProductType:")
        val iOSVersion = getDeviceInfo("ProductVersion:")
        try {
            syslog.writeText("============================================$lineDivider")
            syslog.appendText("iPad Model: $iPad$lineDivider")
            syslog.appendText("iOS Version: $iOSVersion$lineDivider")
            syslog.appendText("============================================$lineDivider")
            syslog.appendText(appView.logsField.getText(0,appView.logsField.length).toString())
        } catch (e: IOException) {e.printStackTrace()}
        appView.appendLogs("[idevicesyslog] Stack is saved to: $syslog\n")
    }

    fun openWorkingDir(){
        val targetDir = System.getProperty("user.dir")
        val root = File(targetDir)
        OS.getActions().openDirectoryViewer(root)
        appView.appendLogs("[idevicesyslog] Opened folder: $targetDir\n")
    }

    private fun getDeviceInfo(property: String): String {
        val proc = ProcessBuilder(OS.getActions().executable("ideviceinfo"))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start() as Process
        //this@AppController.proc = proc
        return proc.inputStream
                .bufferedReader()
                .readLines()
                .firstOrNull {
                    it.contains(property, ignoreCase = true)
                }?.let { devicePropertyRegex.matchEntire(it)?.groupValues?.get(2)}!!
    }

    fun mountDevImage() {
        val productVersion = getDeviceInfo("ProductVersion")
        val targetDir = System.getProperty("user.dir")
        val str = "$targetDir\\dev_image\\$productVersion\\DeveloperDiskImage.dmg $targetDir\\dev_image\\$productVersion\\DeveloperDiskImage.dmg.signature"
        val proc = ProcessBuilder(OS.getActions().executable("ideviceimagemounter"), str)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start() as Process
        this@AppController.proc = proc
        val reader = proc.inputStream?.bufferedReader()
        while (true) {
            val line = reader!!.readLine() ?: break
            runLater {appView.appendLogs("[ideviceimagemounter] $line")}
        }
    }

    fun makeScreenshot(){
        mountDevImage()
        val timestamp = LocalDateTime.now().format(ofPattern("yyyy-MM-dd-HH-mm-ss"))
        val targetDir = System.getProperty("user.dir")
        val path = "$targetDir\\screenshots\\screenshot-$timestamp.tiff"
        val proc = ProcessBuilder(OS.getActions().executable("idevicescreenshot"), path)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start() as Process
        //this@AppController.proc = proc
        val reader = proc.inputStream?.bufferedReader()
        val line = reader?.readLine()
        if (line != null) {
            appView.appendLogs("[idevicescreenshot] $line\n")
        }
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

