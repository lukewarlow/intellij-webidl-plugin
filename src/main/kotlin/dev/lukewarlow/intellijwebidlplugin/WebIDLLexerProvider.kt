package dev.lukewarlow.intellijwebidlplugin

import com.intellij.lexer.LexerBase
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.antlr.v4.kotlinruntime.StringCharStream
import org.antlr.v4.kotlinruntime.Token
import webidl.generated.WebIDLLexer

class WebIDLLexerProvider : LexerBase() {
    private lateinit var antlrLexer: WebIDLLexer
    private lateinit var tokens: MutableList<AdjustedToken>
    private var currentIndex = 0
    private lateinit var buffer: CharSequence

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer

        val fragment = buffer.subSequence(startOffset, endOffset).toString()
        val input = StringCharStream(fragment)
        antlrLexer = WebIDLLexer(input)

        val rawTokens = antlrLexer.allTokens

        var offset = startOffset
        tokens = mutableListOf()
        for (token in rawTokens) {
            val length = token.stopIndex - token.startIndex + 1
            if (length <= 0 || offset + length > buffer.length) {
                Logger.getInstance(WebIDLLexerProvider::class.java)
                    .warn("Skipping token '${token.text}' due to invalid range (length=$length, offset=$offset)")
                continue
            }

            tokens.add(AdjustedToken(token, offset, offset + length))
            offset += length
        }

        currentIndex = 0
    }

    override fun getTokenType(): IElementType? {
        val token = tokens.getOrNull(currentIndex)?.delegate ?: return null
        return WebIDLElementTypes.tokenTypeMap[token.type] ?: TokenType.BAD_CHARACTER
    }

    override fun getTokenStart(): Int =
        tokens.getOrNull(currentIndex)?.startOffset ?: buffer.length

    override fun getTokenEnd(): Int =
        tokens.getOrNull(currentIndex)?.endOffset ?: buffer.length

    override fun advance() {
        currentIndex++
    }

    override fun getState(): Int = 0

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = buffer.length
}


object WebIDLElementTypes {
    val tokenTypeMap: Map<Int, IElementType> by lazy {
        val vocab = WebIDLLexer(StringCharStream("")).vocabulary

        buildMap {
            for (i in 0..vocab.maxTokenType) {
                val name = vocab.getSymbolicName(i)
                if (i == WebIDLLexer.Tokens.ErrorChar)
                    put(i, TokenType.BAD_CHARACTER)
                else if (i == WebIDLLexer.Tokens.Whitespace)
                    put(i, TokenType.WHITE_SPACE)
                else
                    put(i, WebIDLTokenType(name ?: "Unnamed", i))
            }
        }
    }
}
data class AdjustedToken(
    val delegate: Token,
    val startOffset: Int,
    val endOffset: Int
)

