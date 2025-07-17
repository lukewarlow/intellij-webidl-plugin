package dev.lukewarlow.intellijwebidlplugin

import com.intellij.lang.Commenter

class WebIDLCommenter : Commenter {
    override fun getLineCommentPrefix(): String = "//"
    override fun getBlockCommentPrefix() = "/*"
    override fun getBlockCommentSuffix() = "*/"
    override fun getCommentedBlockCommentPrefix() = "/*"
    override fun getCommentedBlockCommentSuffix() = "*/"
}