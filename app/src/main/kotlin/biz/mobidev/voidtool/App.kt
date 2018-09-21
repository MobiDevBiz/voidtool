package biz.mobidev.voidtool

import javafx.application.Application
import javafx.stage.Stage
import tornadofx.App as TornadoApp
import tornadofx.importStylesheet

class App: TornadoApp(AppView::class)

fun main(args: Array<String>) = Application.launch(App::class.java, *args)
