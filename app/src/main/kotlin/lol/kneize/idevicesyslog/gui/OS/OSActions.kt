package lol.kneize.idevicesyslog.gui.OS

import java.io.IOException
import java.io.File


interface OSActions {
    val desktopPath: String
    fun executable(value: String): String
    @Throws(IOException::class)
    fun openDirectoryViewer(directory: File)
    fun getDesktopPath(value: String): String
}