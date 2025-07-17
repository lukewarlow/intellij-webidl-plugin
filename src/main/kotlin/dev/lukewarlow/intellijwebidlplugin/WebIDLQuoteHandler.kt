package dev.lukewarlow.intellijwebidlplugin

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler

class WebIDLQuoteHandler : SimpleTokenSetQuoteHandler(
    WebIDLParserDefinition.STRINGS
)

