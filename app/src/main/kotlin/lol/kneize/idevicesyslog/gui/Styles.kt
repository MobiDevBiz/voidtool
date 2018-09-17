package lol.kneize.idevicesyslog.gui

import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class Styles : Stylesheet() {
    companion object {
        const val APP_PADDING = 15.0

        val appView by cssclass()
    }

    init {
        select(appView) {
            padding = box(APP_PADDING.px)
            vgap = 14.px
            hgap = 20.px

        }
    }
}