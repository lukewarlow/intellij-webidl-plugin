package dev.lukewarlow.intellijwebidlplugin

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import webidl.generated.WebIDLLexer

class WebIDLPairedBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> = arrayOf(
        BracePair(WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.LBrace]!!, WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.RBrace]!!, true),
        BracePair(WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.LParen]!!, WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.RParen]!!, false),
        BracePair(WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.LBracket]!!, WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.RBracket]!!, false),
    )

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return true
    }

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
}