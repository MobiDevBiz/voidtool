package biz.mobidev.voidtool

import javafx.application.Platform
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import biz.mobidev.voidtool.os.OS
import biz.mobidev.voidtool.utils.appendln
import biz.mobidev.voidtool.utils.runBinary
import javafx.concurrent.Task
import tornadofx.*
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*
import java.util.concurrent.TimeUnit

private val fileSep: String = File.separator

class AppController : Controller() {
    private val appView: AppView by inject()
    private var syslogTask: Task<*>? = null

    private var lastMessage: LogMessage? = null
    private fun appendLogMessage(log: LogMessage? = null) {
        println(log)
        lastMessage?.let { it -> runLater { appView.observableList.add(it) } }
        lastMessage = log
    }
    private fun appendStringMessage(continuation: String) {
        println(continuation)
        val previous = lastMessage
        if(previous != null) {
            lastMessage = previous.copy(message = "${previous.message}\n$continuation")
        } else {
            runLater { appView.observableList.add(LogMessage("", "", "", "", continuation)) }
        }
    }

    private fun runBinaryIfPresent(binary: String, vararg params: String): Process? =
            runBinary(binary, *params) ?: run {
                appView.informOfAbsentTool("idevicesyslog")
                null
            }

    private val devicePropertyRegex = Regex("""^(\w+:\s)(.*)$""")
    private fun getDeviceInfo(property: String) =
            runBinaryIfPresent("ideviceinfo")?.inputStream
                    ?.bufferedReader()
                    ?.readLines()
                    ?.firstOrNull { it.contains(property, ignoreCase = true) }
                    ?.let { devicePropertyRegex.matchEntire(it)?.groupValues?.get(2) }

    fun startCollectingLogs() {
        syslogTask = runAsync {
            runBinaryIfPresent("idevicesyslog")?.apply {
                try {
                    inputStream.reader().useLines {
                        it.forEach { line ->
                            val parsed = line.parseLogMessage()
                            if (parsed != null) {
                                appendLogMessage(parsed)
                            } else {
                                appendStringMessage(line)
                            }
                            if (Thread.currentThread().isInterrupted) {
                                return@useLines
                            }
                        }
                    }
                } finally {
                    destroyForcibly()
                    waitFor(2000, TimeUnit.MILLISECONDS)
                }
            }
        }
    }

    fun stopCollectingLogs() {
        syslogTask?.cancel()
        syslogTask = null
        appendLogMessage()
        appendStringMessage("[disconnected]")
    }

    fun copyToClipboard() {
        val clipboard = Clipboard.getSystemClipboard()
        val clipboardContent = ClipboardContent()
        val selection = appView.logView.selectedItem?.toDetailedString()
        if (!selection.isNullOrBlank()) {
            clipboardContent.putString(selection)
            clipboard.setContent(clipboardContent)
            appendStringMessage("[idevicesyslog] Selection is copied to clipboard")
        }
    }

    fun copyToFile() {
        val targetDir = System.getProperty("user.dir")
        val path = String.format("$targetDir${fileSep}logs")
        val root = File(path)
        root.mkdirs()
        val fileName = "syslog-" + SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(Date()) + ".txt"
        val syslog = File(root, fileName)
        val iDevice = getDeviceInfo("ProductType:")
        val iOSVersion = getDeviceInfo("ProductVersion:")
        val uniqueDeviceID = getDeviceInfo("UniqueDeviceID:")
        try {
            syslog.bufferedWriter().use {
                it.appendln("===============================================================================")
                it.appendln("Apple Device Model: $iDevice")
                it.appendln("iOS Version: $iOSVersion")
                it.appendln("UDID: $uniqueDeviceID")
                it.appendln("Search term was: ${appView.keyword.text}")
                it.appendln("================================================================================")
                appView.filteredList.forEach { message ->
                    it.appendln("$message")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        appendStringMessage("[idevicesyslog] Stack is saved to: $syslog")
    }

    fun openWorkingDir() {
        val targetDir = System.getProperty("user.dir")
        val root = File(targetDir)
        OS.actions.openDirectoryViewer(root)
        appendStringMessage("[idevicesyslog] Opened folder: $targetDir")
    }

    fun mountDevImage() {
        val targetDir = System.getProperty("user.dir")
        val diskImageLocation = Paths.get(targetDir, "dev_image", getDeviceInfo("ProductVersion"))
        val diskImagePath = diskImageLocation.resolve("DeveloperDiskImage.dmg")
        val diskImageSignaturePath = diskImageLocation.resolve("DeveloperDiskImage.dmg.signature")

        if(!Files.isRegularFile(diskImagePath) || !Files.isRegularFile(diskImageSignaturePath)) {
            appView.alertProbWithDiskImage()
        } else {
            runBinaryIfPresent(
                    "ideviceimagemounter",
                    diskImagePath.toString(),
                    diskImageSignaturePath.toString())?.let {
                val reader = it.inputStream.bufferedReader()
                while (true) {
                    val line = reader.readLine() ?: break
                    appendStringMessage("[ideviceimagemounter] $line")
                }
            }
        }
    }

    fun makeScreenshot() {
        mountDevImage()
        val timestamp = LocalDateTime.now().format(ofPattern("yyyy-MM-dd-HH-mm-ss"))
        val targetDir = System.getProperty("user.dir")
        val screenshotsPath =  Files.createDirectories(Paths.get(targetDir, "screenshots"))
        val pathToSaveScreenshot = screenshotsPath.resolve("screenshot-$timestamp.tiff")
        runBinaryIfPresent("idevicescreenshot", pathToSaveScreenshot.toString())?.apply {
            inputStream.bufferedReader().readLine()?.let {
                appendStringMessage("[idevicescreenshot] $it")
            }
        }
    }

    fun exit() {
        stopCollectingLogs()
        Platform.exit()
    }
}
