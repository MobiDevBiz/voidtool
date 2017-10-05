package lol.kneize.deviceassigner

import javafx.application.Application
import javafx.stage.Stage

open class App: Application() {
    override fun start(primaryStage: Stage?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            Application.launch(App::class.java, *args)
        }
    }
}
