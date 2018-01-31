package lol.kneize.idevicesyslog.gui

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.paint.Paint.valueOf
import javafx.scene.text.Font.font
import javafx.util.Duration
import tornadofx.*
import lol.kneize.idevicesyslog.gui.Styles.Companion
import sun.management.Agent.getText
import java.util.*




class AppView : View() {
    override val root = BorderPane()
    val appController: AppController by inject()

    lateinit var rPanel: Pane
    lateinit var controls: Pane
    var logsField : TextArea by singleAssign()

    var runButton: Button by singleAssign()
    var clearLogsButton: Button by singleAssign()
    var copyToClipboardButton: Button by singleAssign()
    var saveToFileButton: Button by singleAssign()
    var stopButton: Button by singleAssign()
    var exitButton: Button by singleAssign()
    var keyField: TextField by singleAssign()
    var keyword = SimpleStringProperty()



    init {
        title = "iOS system logs (libimobiledevice-idevicesyslog)"

        with (root) {
            addClass(Companion.appview)

            style {
                padding = box(0.px)
            }
//            top {
//                controls = hbox (10.0, Pos.CENTER){
//                    padding = tornadofx.insets(10.0)
//
//
//                    togglebutton ("Show Timestamp") {
//                        //LogView.showTimeStampProperty().bind(showTimestamp.selectedProperty())
//                    }
//                    togglebutton ("Tail") {
//                        //
//                    }
//                    togglebutton ("Pause") {
//                        //
//                    }
//                    slider(0.1, 60.0, 60.0) {
//                        //
//                    }
//                }
//            }
            right {
                rPanel = vbox(10.0) {
                    padding = tornadofx.insets(10.0)
                    alignment = Pos.CENTER_RIGHT

//                    textfield ("justenergy"){
//                        maxWidth = Double.MAX_VALUE
//                        bind(keyword)
//                    }

                    button("Run") {
                        isDefaultButton = true
                        maxWidth = Double.MAX_VALUE
                        setOnAction {
                            appController.startCollectingLogs()
                        }
                    }
//                    button("GC") {
//                        maxWidth = Double.MAX_VALUE
//                        setOnAction {
//                            System.gc()
//                        }
//                   }
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
                        //prefRowCount = 500000
//                        style {
//                            backgroundColor += Color.DARKSLATEGRAY
//                        }
                        font = font("Monospaced", 16.0)
//                        font = font(12.0)



                    }
                }
            }

            Platform.runLater {
                currentStage?.minHeight = rPanel.height + Styles.APP_PADDING * 2
            }
        }
    }
//    fun deleteLogs(index: Int) {
//        val LIMIT = 500000
//        logsField.replaceText()
//    }

    fun appendLogs(logs: String) {
//        val pos = logsField.getScrollTop()
//        val anchor = logsField.getAnchor()
//        val caret = logsField.getCaretPosition()
//        if (logs.contains("JustEnergy", ignoreCase = true))
//        {
//            logs.toString()
//        }
        logsField.appendText(logs)
//        logsField.setScrollTop(pos)
//        logsField.selectRange(anchor, caret)
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

