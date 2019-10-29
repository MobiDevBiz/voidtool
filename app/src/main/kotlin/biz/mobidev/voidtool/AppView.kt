package biz.mobidev.voidtool

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.scene.paint.Color
import tornadofx.*

class AppView : View() {
    override val root = BorderPane()
    private val appController: AppController by inject()

    private lateinit var tPanel: Pane
    lateinit var logView: TableView<LogMessage>
    lateinit var keyword: TextField

    private val scroll = SimpleBooleanProperty()
    private val list = mutableListOf<LogMessage>()
    val observableList: ObservableList<LogMessage> = FXCollections.observableList(list)
    val filteredList = FilteredList<LogMessage>(observableList)

    fun informOfAbsentTool(binary: String) {
        runAsync {
            null
        } ui {
            alert(
                    type = Alert.AlertType.ERROR,
                    header = "Binary is missing",
                    content = "Check that $binary is in PATH"
            )
        }
    }

    fun alertProbWithDiskImage() {
        runAsync {
            null
        } ui {
            alert(
                    type = Alert.AlertType.ERROR,
                    header = "Developer image is missing",
                    content = "Check that dev_image directory exists"
            )
        }
    }


    init {
        title = "Void tool"

        with(root) {
            style {
                padding = box(0.px)
                setMinSize(800.0, 600.0)
            }

            top {
                tPanel = hbox(10.0) {
                    padding = tornadofx.insets(10.0)
                    vbox {
                        hbox {
                            keyword = textfield {
                                promptText = "Filter"
                                textProperty().addListener { _, _, newValue: String ->
                                    if (newValue.isEmpty()) {
                                        filteredList.setPredicate(null)
                                    } else {
                                        filteredList.setPredicate { it.matchesFilter(newValue) }
                                    }
                                }
                            }

                            combobox<String> {
                                items = LogLevel.values().asList().observable() as ObservableList<String>

                            }
                        }
                        hbox {
                            button("Run") {
                                isDefaultButton = true
                                maxWidth = Double.MAX_VALUE
                                setOnAction { appController.startCollectingLogs() }
                            }
                            button("Stop") {
                                maxWidth = Double.MAX_VALUE
                                setOnAction { appController.stopCollectingLogs() }
                            }

                            button("To clipboard") {
                                maxWidth = Double.MAX_VALUE
                                setOnAction { appController.copyToClipboard() }

                            }
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
                                setOnAction { appController.makeScreenshot() }
                            }
                            button("Mount dev image") {
                                maxWidth = Double.MAX_VALUE
                                setOnAction { appController.mountDevImage() }
                            }
                            button("Exit") {
                                maxWidth = Double.MAX_VALUE
                                setOnAction { appController.exit() }
                            }
                        }
                    }
                }
            }
            center {
                vbox {
                    padding = tornadofx.insets(10f)
                    maxHeight = Double.MAX_VALUE
                    logView = tableview<LogMessage>(filteredList) {
                        readonlyColumn("Date", LogMessage::logDate).cellFormat(cellFormatter)
                        readonlyColumn("Device Name", LogMessage::deviceName).cellFormat(cellFormatter)
                        readonlyColumn("Parent process", LogMessage::parentProcess).cellFormat(cellFormatter)
                        readonlyColumn("Log level", LogMessage::logLevelString).cellFormat(cellFormatter)
                        readonlyColumn("Message", LogMessage::message).cellFormat(cellFormatter)
                        prefHeightProperty().bind(currentStage?.heightProperty())
                        prefWidthProperty().bind(currentStage?.widthProperty())

                        style {
                            fontFamily = "Monospaced"
                        }

                        columnResizePolicy = SmartResize.POLICY
                        if (scroll.value) {
                            items.addListener { c: ListChangeListener.Change<*> ->
                                c.next()
                                val size = items.size
                                if (size > 0) {
                                    scrollTo(size - 1)
                                }
                            }
                        }
                    }
                }
            }

            Platform.runLater {
                currentStage?.minHeight = tPanel.height + Styles.APP_PADDING * 2
                currentStage?.minWidth = tPanel.height * 2 + Styles.APP_PADDING * 2
            }
        }
    }
}
//}    init {
//        title = "Void tool"
//
//        with(root) {
//            style {
//                padding = box(0.px)
//                setMinSize(800.0, 600.0)
//            }
//
//            right {
//                rPanel = vbox(10.0) {
//                    padding = tornadofx.insets(10.0)
//                    alignment = Pos.CENTER_RIGHT
//                    keyword = textfield {
//                        promptText = "Filter"
//                        textProperty().addListener { _, _, newValue: String ->
//                            if (newValue.isEmpty()) {
//                                filteredList.setPredicate(null)
//                            } else {
//                                filteredList.setPredicate { it.matchesFilter(newValue) }
//                            }
//                        }
//                    }
//
//                    button("Run") {
//                        isDefaultButton = true
//                        maxWidth = Double.MAX_VALUE
//                        setOnAction { appController.startCollectingLogs() }
//                    }
//                    button("Stop") {
//                        maxWidth = Double.MAX_VALUE
//                        setOnAction { appController.stopCollectingLogs() }
//                    }
//
//                    button("To clipboard") {
//                        maxWidth = Double.MAX_VALUE
//                        setOnAction { appController.copyToClipboard() }
//
//                    }
//                    button("To File") {
//                        maxWidth = Double.MAX_VALUE
//                        setOnAction { appController.copyToFile() }
//                    }
//                    button("Open working dir") {
//                        maxWidth = Double.MAX_VALUE
//                        setOnAction { appController.openWorkingDir() }
//                    }
//                    button("Make screenshot") {
//                        maxWidth = Double.MAX_VALUE
//                        setOnAction { appController.makeScreenshot() }
//                    }
//                    button("Mount dev image") {
//                        maxWidth = Double.MAX_VALUE
//                        setOnAction { appController.mountDevImage() }
//                    }
//                    button("Exit") {
//                        maxWidth = Double.MAX_VALUE
//                        setOnAction { appController.exit() }
//                    }
//                }
//            }
//            center {
//                vbox {
//                    padding = tornadofx.insets(10f)
//                    maxHeight = Double.MAX_VALUE
//                    logView = tableview<LogMessage>(filteredList) {
//                        readonlyColumn("Date", LogMessage::logDate).cellFormat(cellFormatter)
//                        readonlyColumn("Device Name", LogMessage::deviceName).cellFormat(cellFormatter)
//                        readonlyColumn("Parent process", LogMessage::parentProcess).cellFormat(cellFormatter)
//                        readonlyColumn("Log level", LogMessage::logLevelString).cellFormat(cellFormatter)
//                        readonlyColumn("Message", LogMessage::message).cellFormat(cellFormatter)
//                        prefHeightProperty().bind(currentStage?.heightProperty())
//                        prefWidthProperty().bind(currentStage?.widthProperty())
//
//                        style {
//                            fontFamily = "Monospaced"
//                        }
//
//                        columnResizePolicy = SmartResize.POLICY
//                        if (scroll.value) {
//                            items.addListener { c: ListChangeListener.Change<*> ->
//                                c.next()
//                                val size = items.size
//                                if (size > 0) {
//                                    scrollTo(size - 1)
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            Platform.runLater {
//                currentStage?.minHeight = rPanel.height + Styles.APP_PADDING * 2
//                currentStage?.minWidth = rPanel.height * 2 + Styles.APP_PADDING * 2
//            }
//        }
//    }
//}

private val cellFormatter: TableCell<LogMessage, String>.(String) -> Unit = {
    style(append = true) {
        focusColor = Color.RED
        accentColor = Color.RED
        backgroundColor += when (rowItem.logLevel) {
            LogLevel.EMERGENCY -> Color.RED
            LogLevel.ALERT -> Color.ORANGERED
            LogLevel.CRITICAL -> Color.SALMON
            LogLevel.ERROR -> Color.PINK
            LogLevel.WARNING -> Color.PALEGOLDENROD
            LogLevel.NOTICE -> Color.WHITESMOKE
            LogLevel.INFO -> Color.WHITESMOKE
            LogLevel.DEBUG -> Color.WHITESMOKE
            LogLevel.UNKNOWN -> Color.TRANSPARENT
        }

        textFill = Color.BLACK
        text = item
        highlightTextFill = Color.WHITE
    }
}
