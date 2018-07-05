package lol.kneize.idevicesyslog.gui.OS

import java.io.IOException
import java.io.File


class OSXActions : OSActions {
    override fun getDesktopPath(value: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // TODO Fallback must be here
    override
    val desktopPath: String
        get() = System.getenv("HOME") + "/Desktop"

    override fun executable(value: String): String {
        return value
    }

    @Throws(IOException::class)
    override fun openDirectoryViewer(directory: File) {
        ProcessBuilder("open", "-a", "Finder", directory.absolutePath)
                .inheritIO()
                .start()
    }
}