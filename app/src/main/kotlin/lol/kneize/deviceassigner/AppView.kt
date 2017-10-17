package lol.kneize.deviceassigner

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.text.Font.font
import javafx.util.Duration
import tornadofx.*
import tornadofx.Stylesheet.Companion.label
import lol.kneize.deviceassigner.Styles.Companion
import java.util.*

class AppView : View() {
    override val root = BorderPane()
    val appController: AppController by inject()

    lateinit var rPanel: Pane
    var logsField : TextArea by singleAssign()

    var runButton: Button by singleAssign()
    var clearLogsButton: Button by singleAssign()
    var copyToClipboardButton: Button by singleAssign()
    var saveToFileButton: Button by singleAssign()
    var stopButton: Button by singleAssign()
    var exitButton: Button by singleAssign()

    init {
        title = "iOS system logs (libimobiledevice-idevicesyslog)"

        with (root) {
            addClass(Companion.appview)

            style {
                padding = box(0.px)
            }
            right {
                rPanel = vbox(10.0) {
                    padding = tornadofx.insets(10.0)
                    alignment = Pos.CENTER_RIGHT
                    button("Run") {
                        isDefaultButton = true
                        maxWidth = Double.MAX_VALUE
                        setOnAction {
                            appController.startCollectingLogs()
                        }
                    }
                    button("Stop") {
                        maxWidth = Double.MAX_VALUE
                        setOnAction {
                            appController.stopCollectingLogs()
                        }
                    }
                    button ( "Clear logs" ) {
                        maxWidth = Double.MAX_VALUE
                        setOnAction { appController.clearLogs() }

                    }
                    button ( "To clipboard" ) {
                        maxWidth = Double.MAX_VALUE
                        setOnAction { appController.copyToClipboard() }
                    }
                    button ( "To File" ) {
                        maxWidth = Double.MAX_VALUE
                        setOnAction { appController.copyToFile() }
                    }
                    button ( "Exit" ) {
                        maxWidth = Double.MAX_VALUE
                        setOnAction {appController.exit()}
                    }

                }
            }
            center {
                vbox {
                    padding = tornadofx.insets(10f)
                    logsField = textarea {
                        hgrow = Priority.ALWAYS
                        vgrow = Priority.ALWAYS
                        isWrapText = false
                        isEditable = false
                        font = font("Monospaced")

                    }
                }
            }

            Platform.runLater {
                currentStage?.minHeight = rPanel.height + Styles.APP_PADDING * 2
            }
        }
    }

    fun clear() {
//
    }

    fun appendLogs(logs: String) {
        logsField.appendText(logs)
    }

    fun shakeStage() {
        val rand = Random()
        var dir = false
        var alter = false
        val cycleCount = 12
        var diff = 0
        val amp = 5
        val keyframeDuration = Duration.seconds(0.04)

        val stage = FX.primaryStage

        val timeline = Timeline(KeyFrame(keyframeDuration, EventHandler {
            val move = if(dir) {
                diff
            } else {
                diff = rand.nextInt() % (amp * 2) - amp
                alter = !alter
                diff
            }
            stage.x += move * if(dir) -1 else 1
            stage.y += move * if(dir xor alter) -1 else 1
            dir = !dir
        }))
        timeline.cycleCount = cycleCount
        timeline.play()
    }
}
private operator fun CssRule.invoke(s: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}
