package dev.lukewarlow.intellijwebidlplugin

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType
import webidl.generated.WebIDLLexer

object WebIDLSyntaxHighlightingColors {
    val KEYWORD = TextAttributesKey.createTextAttributesKey("WEBIDL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
    val IDENTIFIER = TextAttributesKey.createTextAttributesKey("WEBIDL_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
    val STRING = TextAttributesKey.createTextAttributesKey("WEBIDL_STRING", DefaultLanguageHighlighterColors.STRING)
    val NUMBER = TextAttributesKey.createTextAttributesKey("WEBIDL_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    val LINE_COMMENT = TextAttributesKey.createTextAttributesKey("WEBIDL_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
    val BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey("WEBIDL_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
    val BRACES = TextAttributesKey.createTextAttributesKey("WEBIDL_BRACES", DefaultLanguageHighlighterColors.BRACES)
    val BRACKETS = TextAttributesKey.createTextAttributesKey("WEBIDL_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
    val PARENTHESES = TextAttributesKey.createTextAttributesKey("WEBIDL_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
    val SEMICOLON = TextAttributesKey.createTextAttributesKey("WEBIDL_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON)
    val OPERATORS = TextAttributesKey.createTextAttributesKey("WEBIDL_OPERATORS", DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val COMMA = TextAttributesKey.createTextAttributesKey("WEBIDL_COMMA", DefaultLanguageHighlighterColors.COMMA)
    val COLON = TextAttributesKey.createTextAttributesKey("WEBIDL_COLON")
    val CONSTANT = TextAttributesKey.createTextAttributesKey("WEBIDL_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT)
}

class WebIDLSyntaxHighlighter : SyntaxHighlighterBase() {
    val keywordTokenTypes: Set<IElementType> = setOf(
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Interface],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Partial],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Mixin],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Callback],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Dictionary],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Typedef],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Namespace],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Enum],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Includes],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Or],

        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Attribute],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Const],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Readonly],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Static],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Required],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Optional],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Stringifier],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Getter],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Setter],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Deleter],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Constructor],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Inherit],

        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Maplike],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Setlike],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Sequence],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.AsyncIterable],

        // TODO should types be separated?
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Any],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Void],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Undefined],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Boolean],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Byte],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Octet],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Bigint],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Float],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Double],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Short],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Long],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Unsigned],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Unrestricted],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.ByteString],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.DOMString],
        WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.USVString],
    ).filterNotNull().toSet()

    override fun getHighlightingLexer(): Lexer = WebIDLLexerProvider()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Identifier] -> arrayOf(WebIDLSyntaxHighlightingColors.IDENTIFIER)
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Constant] -> arrayOf(WebIDLSyntaxHighlightingColors.CONSTANT)
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Quote],
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.String] -> arrayOf(WebIDLSyntaxHighlightingColors.STRING)
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Integer],
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Decimal] -> arrayOf(WebIDLSyntaxHighlightingColors.NUMBER)

            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.LineComment] -> arrayOf(WebIDLSyntaxHighlightingColors.LINE_COMMENT)
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.BlockComment] -> arrayOf(WebIDLSyntaxHighlightingColors.BLOCK_COMMENT)

            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.LBrace],
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.RBrace] -> arrayOf(WebIDLSyntaxHighlightingColors.BRACES)

            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.LBracket],
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.RBracket] -> arrayOf(WebIDLSyntaxHighlightingColors.BRACKETS)

            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.LGeneric],
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.RGeneric],
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Question],
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Variadic],
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Equal], -> arrayOf(WebIDLSyntaxHighlightingColors.OPERATORS)

            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.LParen],
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.RParen] -> arrayOf(WebIDLSyntaxHighlightingColors.PARENTHESES)

            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Semicolon] -> arrayOf(WebIDLSyntaxHighlightingColors.SEMICOLON)
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Comma] -> arrayOf(WebIDLSyntaxHighlightingColors.COMMA)
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Colon] -> arrayOf(WebIDLSyntaxHighlightingColors.COLON)

            // Keywords
            in keywordTokenTypes -> arrayOf(WebIDLSyntaxHighlightingColors.KEYWORD)

            else -> emptyArray()
        }
    }
}

class WebIDLSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?) =
        WebIDLSyntaxHighlighter()
}