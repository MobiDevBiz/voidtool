package lol.kneize.idevicesyslog.gui

data class LogMessage(
        val logdate: String,
        val deviceName: String,
        val parentProcess: String,
        val logLevel: String,
        val message: String
) {
    fun matchesFilter(filter: String) = filter.isBlank() ||
            deviceName.contains(filter, ignoreCase = true) ||
            message.contains(filter, ignoreCase = true) ||
            logdate.contains(filter, ignoreCase = true)
}
