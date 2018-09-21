package biz.mobidev.voidtool.os

import java.io.IOException
import java.io.File

class WindowsActions : OSActions {
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
