package lol.kneize.idevicesyslog.gui

import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.beans.binding.Bindings
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.css.PseudoClass
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.Duration

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque

class Log {

    private val log = LinkedBlockingDeque<LogRecord>(MAX_LOG_ENTRIES)

    fun drainTo(collection: MutableCollection<in LogRecord>) {
        log.drainTo(collection)
    }

    fun offer(record: LogRecord) {
        log.offer(record)
    }

    companion object {
        private val MAX_LOG_ENTRIES = 1000000
    }
}

internal class Logger(val log: Log, private val context: String) {

    fun log(record: LogRecord) {
        log.offer(record)
    }

    fun debug(msg: String) {
        log(LogRecord(Level.DEBUG, context, msg))
    }

    fun info(msg: String) {
        log(LogRecord(Level.INFO, context, msg))
    }

    fun warn(msg: String) {
        log(LogRecord(Level.WARN, context, msg))
    }

    fun error(msg: String) {
        log(LogRecord(Level.ERROR, context, msg))
    }
}

internal enum class Level {
    DEBUG, INFO, WARN, ERROR
}

class LogRecord internal constructor(val level: Level, val context: String, val message: String) {
    val timestamp: Date?

    init {
        this.timestamp = Date()
    }
}

class LogView(log: Log) : ListView<LogRecord>() {

    private val showTimestamp = SimpleBooleanProperty(false)
    private val filterLevel = SimpleObjectProperty<Level>(null)
    private val tail = SimpleBooleanProperty(false)
    private val paused = SimpleBooleanProperty(false)
    private val refreshRate = SimpleDoubleProperty(60.0)

    private val logItems = FXCollections.observableArrayList<LogRecord>()

    fun showTimeStampProperty(): BooleanProperty {
        return showTimestamp
    }

    internal fun filterLevelProperty(): ObjectProperty<Level> {
        return filterLevel
    }

    fun tailProperty(): BooleanProperty {
        return tail
    }

    fun pausedProperty(): BooleanProperty {
        return paused
    }

    fun refreshRateProperty(): DoubleProperty {
        return refreshRate
    }

    init {
        styleClass.add("log-view")

        val logTransfer = Timeline(
                KeyFrame(Duration.seconds(1.0), EventHandler<ActionEvent> {
                    log.drainTo(logItems)

                    if (logItems.size > MAX_ENTRIES) {
                        logItems.remove(0, logItems.size - MAX_ENTRIES)
                    }

                    if (tail.get()) {
                        scrollTo(logItems.size)
                    }
                }
            )
        )
        logTransfer.setCycleCount(Timeline.INDEFINITE)
        logTransfer.rateProperty().bind(refreshRateProperty())

        this.pausedProperty().addListener { observable, oldValue, newValue ->
            if (newValue!! && logTransfer.getStatus() == Animation.Status.RUNNING) {
                logTransfer.pause()
            }

            if (!newValue && logTransfer.getStatus() == Animation.Status.PAUSED && parent != null) {
                logTransfer.play()
            }
        }

        this.parentProperty().addListener { observable, oldValue, newValue ->
            if (newValue == null) {
                logTransfer.pause()
            } else {
                if (!paused.get()) {
                    logTransfer.play()
                }
            }
        }

        filterLevel.addListener { observable, oldValue, newValue ->
            items = FilteredList(
                    logItems
            ) { logRecord -> logRecord.level.ordinal >= filterLevel.get().ordinal }
        }
        filterLevel.set(Level.DEBUG)

        setCellFactory { param ->
            object : ListCell<LogRecord>() {
                init {
                    showTimestamp.addListener { observable -> updateItem(this.item, this.isEmpty) }
                }

                override fun updateItem(item: LogRecord?, empty: Boolean) {
                    super.updateItem(item, empty)

                    pseudoClassStateChanged(debug, false)
                    pseudoClassStateChanged(info, false)
                    pseudoClassStateChanged(warn, false)
                    pseudoClassStateChanged(error, false)

                    if (item == null || empty) {
                        text = null
                        return
                    }

                    val context = if (item.context == null)
                        ""
                    else
                        item.context + " "

                    if (showTimestamp.get()) {
                        val timestamp = if (item.timestamp == null)
                            ""
                        else
                            timestampFormatter.format(item.timestamp) + " "
                        text = timestamp + context + item.message
                    } else {
                        text = context + item.message
                    }

                    when (item.level) {
                        Level.DEBUG -> pseudoClassStateChanged(debug, true)

                        Level.INFO -> pseudoClassStateChanged(info, true)

                        Level.WARN -> pseudoClassStateChanged(warn, true)

                        Level.ERROR -> pseudoClassStateChanged(error, true)
                    }
                }
            }
        }
    }

    companion object {
        private val MAX_ENTRIES = 10000

        private val debug = PseudoClass.getPseudoClass("debug")
        private val info = PseudoClass.getPseudoClass("info")
        private val warn = PseudoClass.getPseudoClass("warn")
        private val error = PseudoClass.getPseudoClass("error")

        private val timestampFormatter = SimpleDateFormat("HH:mm:ss.SSS")
    }
}

internal class Lorem {
    private var idx = 0

    private val random = Random(42)

    @Synchronized
    fun nextString(): String {
        val end = Math.min(idx + MSG_WORDS, IPSUM.size)

        val result = StringBuilder()
        for (i in idx until end) {
            result.append(IPSUM[i]).append(" ")
        }

        idx += MSG_WORDS
        idx = idx % IPSUM.size

        return result.toString()
    }

    @Synchronized
    fun nextLevel(): Level {
        val v = random.nextDouble()

        if (v < 0.8) {
            return Level.DEBUG
        }

        if (v < 0.95) {
            return Level.INFO
        }

        return if (v < 0.985) {
            Level.WARN
        } else Level.ERROR

    }

    companion object {
        private val IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque hendrerit imperdiet mi quis convallis. Pellentesque fringilla imperdiet libero, quis hendrerit lacus mollis et. Maecenas porttitor id urna id mollis. Suspendisse potenti. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Cras lacus tellus, semper hendrerit arcu quis, auctor suscipit ipsum. Vestibulum venenatis ante et nulla commodo, ac ultricies purus fringilla. Aliquam lectus urna, commodo eu quam a, dapibus bibendum nisl. Aliquam blandit a nibh tincidunt aliquam. In tellus lorem, rhoncus eu magna id, ullamcorper dictum tellus. Curabitur luctus, justo a sodales gravida, purus sem iaculis est, eu ornare turpis urna vitae dolor. Nulla facilisi. Proin mattis dignissim diam, id pellentesque sem bibendum sed. Donec venenatis dolor neque, ut luctus odio elementum eget. Nunc sed orci ligula. Aliquam erat volutpat.".split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        private val MSG_WORDS = 8
    }

}


