package dev.lukewarlow.intellijwebidlplugin

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object WebIDLIcons {
    val LOGO: Icon = IconLoader.getIcon("/logo-webidl.svg", WebIDLIcons::class.java)
}

object WebIDLLanguage : Language("WebIDL")

class WebIDLFileType : LanguageFileType(WebIDLLanguage) {
    override fun getName() = "WebIDL File"
    override fun getDescription() = "Web Interface Definition Language file"
    override fun getDefaultExtension() = "webidl"
    override fun getIcon() = WebIDLIcons.LOGO

    companion object {
        @JvmField
        val INSTANCE = WebIDLFileType()
    }
}