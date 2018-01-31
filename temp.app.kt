open class `AppModel` : Application() {
    override fun start(primaryStage: Stage?) {
        val appController: AppController by inject()

        override fun start(stage: Stage) {
            importStylesheet(InternalWindow.Styles::class)
            super.start(stage)
            appController.init()
        }
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            Application.launch(`AppModel`::class.java, *args)
        }
    }
}
