package lol.kneize.idevicesyslog.gui.os

enum class OS constructor(private val actions: OSActions) {
    WINDOWS(WindowsActions()),
    LINUX(LinuxActions()),
    MACOS(OSXActions());

    companion object {
        private val current: OS
            get() {
                val os = System.getProperty("os.name")
                return when {
                    os.startsWith("Windows") -> WINDOWS
                    os.startsWith("Linux") -> LINUX
                    else -> MACOS
                }
            }

        fun getActions(): OSActions {
            return current.actions
        }
    }
}
