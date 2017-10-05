package lol.kneize.deviceassigner

import tornadofx.CssRule
import tornadofx.Stylesheet.Companion.label
import tornadofx.View
import tornadofx.hbox

class HelloWorld : View() {
    override val root = hbox {
        label("Hello world")
    }
}

private operator fun CssRule.invoke(s: String) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}
