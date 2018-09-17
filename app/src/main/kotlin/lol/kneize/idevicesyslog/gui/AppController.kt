package lol.kneize.idevicesyslog.gui

import javafx.application.Platform
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import lol.kneize.idevicesyslog.gui.os.OS
import org.intellij.lang.annotations.Language
import tornadofx.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*
import java.util.concurrent.TimeUnit


class AppController : Controller() {
    private val appView: AppView by inject()
    private var proc: Process? = null
    private val lineDivider = System.getProperty("line.separator")


    private val serviceMessageRegex = Regex("""^\[\w+]""")
    private val syslogMessageRegex = Regex("""^(\w{3})\s+\d+\s\d+:\d+:\d+\s+[\w-\d]+\s.*?:\s(.*)$""")
    private val applicationMessageRegex = Regex("""^\[(\w)+:\d+]\s*(.*)""")
    private val devicePropertyRegex = Regex("""^(\w+:\s)(.*)$""")

    @Language("RegExp") private val syslogDate = """\w{3}\s+\d{1,2}\s\d{1,2}:\d{1,2}:\d{1,2}"""
    @Language("RegExp") private val syslogDeviceName = """.*?"""
    @Language("RegExp") private val syslogParentProcess = """.+(?:\(.+\))?\[\d+]"""
    @Language("RegExp") private val syslogLogLevel = """<\w+>"""
    @Language("RegExp") private val syslogMessageText = """.*"""
    private val syslogMessage = Regex(
            "($syslogDate)\\s+" +
                    "($syslogDeviceName)\\s+" +
                    "($syslogParentProcess)\\s+" +
                    "($syslogLogLevel):\\s+" +
                    "($syslogMessageText)")


    private fun parseMessage(input: String): LogMessage? {
        val match = syslogMessage.matchEntire(input)
        return match?.let {
            LogMessage(
                    it.groupValues[1],
                    it.groupValues[2],
                    it.groupValues[3],
                    it.groupValues[4],
                    it.groupValues[5])
        } ?: run {
            null
        }
    }

    private var lastMessage: LogMessage? = null
    private fun appendMessage(log: LogMessage? = null) {
        lastMessage?.let { it -> runLater { appView.observableList.add(it) } }
        lastMessage = log
    }
    private fun appendMessage(continuation: String) {
        val previous = lastMessage
       if(previous != null) {
            lastMessage = previous.copy(message = "${previous.message}\n$continuation")
        } else {
            runLater { appView.observableList.add(LogMessage("", "", "", "", continuation)) }
       }
    }

    fun startAppendingRows() {
        runAsync {
            fun parseAndAppend(line: String) = runLater {
                val parsed = parseMessage(line)
                if(parsed != null) {
                    appendMessage(parsed)
                } else {
                    appendMessage(line)
                }
            }

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
                    parseAndAppend(line)
                } else if (line.matches(syslogMessageRegex)) {
                    switch = !switch
                    parseAndAppend(line)
                } else if (!line.matches(applicationMessageRegex) && switch) {
                    parseAndAppend(line)
                }
            }
        }
    }

    fun stopCollectingLogs() {
        this.proc?.destroyForcibly()
        this.proc?.waitFor(5000, TimeUnit.MILLISECONDS)
        this.proc = null
        appendMessage()
        appendMessage("[disconnected]")
    }

    fun copyToClipboard() {
        val clipboard = Clipboard.getSystemClipboard()
        val clipboardContent = ClipboardContent()
        val selection = appView.logView.selectedItem?.toDetailedString()
        if (!selection.isNullOrBlank()) {
            clipboardContent.putString(selection)
            clipboard.setContent(clipboardContent)
            appendMessage("[idevicesyslog] Selection is copied to clipboard")
        }
    }

    fun copyToFile() {
        val targetDir = System.getProperty("user.dir")
        val path = String.format("$targetDir${File.separator}logs")
        val root = File(path)
        root.mkdirs()
        val fileName = "syslog-" + SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(Date()) + ".txt"
        val syslog = File(root, fileName)
        val iPad = getDeviceInfo("ProductType:")
        val iOSVersion = getDeviceInfo("ProductVersion:")
        val uniqueDeviceID = getDeviceInfo("UniqueDeviceID:")
        try {
            syslog.bufferedWriter().use {
                it.append("===============================================================================$lineDivider")
                it.append("Apple Device Model: $iPad$lineDivider")
                it.append("iOS Version: $iOSVersion$lineDivider")
                it.append("UDID: $uniqueDeviceID$lineDivider")
                it.append("Search term was: " + appView.keyword.text + lineDivider)
                it.append("================================================================================$lineDivider")
                for (i in appView.observableList) {
                    it.append(i.toString())
                    it.append(lineDivider)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        appendMessage("[idevicesyslog] Stack is saved to: $syslog")
    }

    fun openWorkingDir(){
        val targetDir = System.getProperty("user.dir")
        val root = File(targetDir)
        OS.getActions().openDirectoryViewer(root)
        appendMessage("[idevicesyslog] Opened folder: $targetDir")
    }

    private fun getDeviceInfo(property: String): String {
        val proc = ProcessBuilder(OS.getActions().executable("ideviceinfo"))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start() as Process
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
        val str = "$targetDir${File.separator}dev_image${File.separator}$productVersion${File.separator}DeveloperDiskImage.dmg $targetDir${File.separator}dev_image${File.separator}$productVersion${File.separator}DeveloperDiskImage.dmg.signature"
        val proc = ProcessBuilder(OS.getActions().executable("ideviceimagemounter"), str)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start() as Process
        this@AppController.proc = proc
        val reader = proc.inputStream?.bufferedReader()
        while (true) {
            val line = reader!!.readLine() ?: break
            appendMessage("[ideviceimagemounter] $line")
        }
    }

    fun makeScreenshot(){
        mountDevImage()
        val timestamp = LocalDateTime.now().format(ofPattern("yyyy-MM-dd-HH-mm-ss"))
        val targetDir = System.getProperty("user.dir")
        val path = String.format("$targetDir${File.separator}screenshots")
        val root = File(path)
        root.mkdirs()
        val pathToSaveScreenshot = "$targetDir${File.separator}screenshots${File.separator}screenshot-$timestamp.tiff"
        val proc = ProcessBuilder(OS.getActions().executable("idevicescreenshot"), pathToSaveScreenshot)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start() as Process
        val reader = proc.inputStream?.bufferedReader()
        val line = reader?.readLine()
        if (line != null) {
            appendMessage("[idevicescreenshot] $line")
        }
    }


    fun exit() {
        stopCollectingLogs()
        Platform.exit()
    }
}


