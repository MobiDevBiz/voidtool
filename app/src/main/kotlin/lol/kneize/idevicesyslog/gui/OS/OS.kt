package lol.kneize.idevicesyslog.gui.OS

enum class OS private constructor(private val actions: OSActions) {
    WINDOWS(WindowsActions()),
    LINUX(LinuxActions()),
    MACOSX(OSXActions());


    companion object {

        val current: OS
            get() {
                val os = System.getProperty("os.name")
                return if (os.startsWith("Windows")) {
                    WINDOWS
                } else if (os.startsWith("Linux")) {
                    LINUX
                } else {
                    MACOSX
                }
            }

        fun getActions(): OSActions {
            return current.actions
        }
    }
}