package lol.kneize.idevicesyslog.gui.os

import java.io.IOException
import java.io.File

class WindowsActions : OSActions {
    override fun getDesktopPath(value: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    //override// TODO http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
    // http://superuser.com/a/428176
    override
    val desktopPath: String
        get() = System.getenv("HOMEPATH") + "\\Desktop"

    override fun executable(value: String): String {
        return "$value.exe"
    }

    @Throws(IOException::class)
    override fun openDirectoryViewer(directory: File) {
        // TODO test if we need wrap path in double quotes
        ProcessBuilder("explorer.exe", directory.absolutePath)
                .inheritIO()
                .start()
    }
}
