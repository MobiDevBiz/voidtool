package lol.kneize.deviceassigner

import javafx.application.Application
import javafx.stage.Stage
import tornadofx.App
import tornadofx.importStylesheet

class AppModel : App(AppView::class) {
    val appController: AppController by inject()

    override fun start(stage: Stage) {
        importStylesheet(Styles::class)
        super.start(stage)
        appController.init()
    }
}

fun main(args: Array<String>) {
    Application.launch(AppModel::class.java, *args)
}
