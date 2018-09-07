package lol.kneize.idevicesyslog.gui.os

import java.io.IOException
import java.io.File

class LinuxActions : OSActions {
    override fun getDesktopPath(value: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val desktopPath: String
        get() {
            try {
                val proc = ProcessBuilder("xdg-user-dir", "DESKTOP")
                        .start()
                val lines = proc.inputStream.bufferedReader().readLines()
                if (!lines.isEmpty()) {
                    return lines.get(0)
                }
            } catch (e: IOException) {
            }

            return System.getenv("HOME") + "/Desktop"
        }

    override fun executable(value: String): String {
        return value
    }

    @Throws(IOException::class)
    override fun openDirectoryViewer(directory: File) {
        ProcessBuilder("xdg-open", directory.absolutePath)
                .inheritIO()
                .start()
    }
}
