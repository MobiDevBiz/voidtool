package biz.mobidev.voidtool.utils

import java.io.Writer

val lineSep: String = System.getProperty("line.separator")

fun Writer.appendln(csq: CharSequence) = append("$csq$lineSep")
