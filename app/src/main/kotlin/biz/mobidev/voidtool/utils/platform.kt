package biz.mobidev.voidtool.utils

import biz.mobidev.voidtool.os.OS
import java.io.File

fun runBinary(binary: String, vararg params: String): Process? =
        System.getenv("PATH")
                .split(File.pathSeparator)
                .asSequence()
                .map { File(it, OS.actions.executable(binary)) }
                .firstOrNull { it.isFile && it.canExecute() }
                ?.let {
                    ProcessBuilder(it.absolutePath, *params)
                            .redirectOutput(ProcessBuilder.Redirect.PIPE)
                            .redirectError(ProcessBuilder.Redirect.PIPE)
                            .start()
                }
