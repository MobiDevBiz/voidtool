package biz.mobidev.voidtool.os

import java.io.IOException
import java.io.File

class OSXActions : OSActions {
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
