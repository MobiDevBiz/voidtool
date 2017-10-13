package lol.kneize.deviceassigner

import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class Styles : Stylesheet() {
    companion object {
        val APP_PADDING = 15.0

        val appview by cssclass()
    }

    init {
        select(appview) {
            padding = box(APP_PADDING.px)
            vgap = 14.px
            hgap = 20.px

        }
    }
}