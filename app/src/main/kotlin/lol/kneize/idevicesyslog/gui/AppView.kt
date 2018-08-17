package lol.kneize.idevicesyslog.gui

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleSetProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.text.Font.font
import javafx.util.Duration
import tornadofx.*
import java.util.*
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY
import java.util.ArrayList

class AppView : View() {
    override val root = BorderPane()
    val appController: AppController by inject()

    lateinit var rPanel: Pane
    lateinit var controls: Pane
    lateinit var logView: TableView<*>
    lateinit var keyword: TextField

/*
val fooProperty = SimpleStringProperty()  // Or any other Property
var foo by fooProperty
 */
    val scroll = SimpleBooleanProperty()
    val list: List<LogMessage> = mutableListOf()
    val observableList: ObservableList<LogMessage> = FXCollections.observableList(list)
    val filteredList = FilteredList<LogMessage>(observableList)



    init {
        title = "iOS system logs reader(idevicesyslog-gui)"

        with (root) {
            style {
                padding = box(0.px)
                setMinSize(800.0,600.0)

            }
            right {
                rPanel = vbox(10.0) {
                    padding = tornadofx.insets(10.0)
                    alignment = Pos.CENTER_RIGHT
                    keyword = textfield {
                        promptText = "Filter"
                        textProperty().addListener { _: ObservableValue<*>, _: String, newValue: String ->
                            if(newValue.isEmpty()) {
                                filteredList.setPredicate(null)
                            } else {
                                filteredList.setPredicate { it.matchesFilter(newValue) }
                            }
                        }
                    }


                    button("Run") {
                        isDefaultButton = true
                        maxWidth = Double.MAX_VALUE
                        setOnAction {
                            appController.startAppendingRows()
                        }
                    }
                    button("Stop") {
                        maxWidth = Double.MAX_VALUE
                        setOnAction {appController.stopCollectingLogs()}
                    }
//                    button("Clear logs") {
//                        maxWidth = Double.MAX_VALUE
//                        setOnAction { appController.clearLogs() }
//                    }
//                    button("To clipboard") {
//                        maxWidth = Double.MAX_VALUE
//                        setOnAction { appController.copyToClipboard() }
//                    }
                    button("To File") {
                        maxWidth = Double.MAX_VALUE
                        setOnAction { appController.copyToFile() }
                    }
                    button("Open working dir") {
                        maxWidth = Double.MAX_VALUE
                        setOnAction { appController.openWorkingDir() }
                    }
                    button("Make screenshot") {
                        maxWidth = Double.MAX_VALUE
                        setOnAction {appController.makeScreenshot()}
                    }
                    button("Mount dev image") {
                        maxWidth = Double.MAX_VALUE
                        setOnAction {appController.mountDevImage()}
                    }
                    button("Exit") {
                        maxWidth = Double.MAX_VALUE
                        setOnAction {appController.exit()}
                    }
                }
            }
            center {
                vbox {
                    padding = tornadofx.insets(10f)
                    maxHeight = Double.MAX_VALUE

                    logView = tableview<LogMessage>(filteredList) {
                        readonlyColumn("Date", LogMessage::logdate)
                        readonlyColumn("Device Name", LogMessage::deviceName)
                        readonlyColumn("Parent process", LogMessage::parentProcess)
                        readonlyColumn("Log level", LogMessage::logLevel)
                        readonlyColumn("Message", LogMessage::message)
                        prefHeightProperty().bind(currentStage?.heightProperty())
                        prefWidthProperty().bind(currentStage?.widthProperty())

                        columnResizePolicy = CONSTRAINED_RESIZE_POLICY
                        font("Monospaced", 16.0)
                        if (scroll.value) {
                            items.addListener { c: ListChangeListener.Change<*> ->
                                c.next()
                                val size = items.size
                                if (size > 0) {
                                    scrollTo(size - 1)
                                } else { items.removeListener{ c: ListChangeListener.Change<*> ->
                                    c.next()}}
                            }
                        }
                    }
                }
            }

            Platform.runLater {
                currentStage?.minHeight = rPanel.height + Styles.APP_PADDING * 2
                currentStage?.minWidth = rPanel.height * 2 + Styles.APP_PADDING * 2
            }
        }
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

