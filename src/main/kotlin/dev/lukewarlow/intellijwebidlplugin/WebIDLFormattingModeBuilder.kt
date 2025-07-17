package dev.lukewarlow.intellijwebidlplugin

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.formatter.common.AbstractBlock
import webidl.generated.WebIDLLexer


class WebIDLFormattingModelBuilder : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val settings = formattingContext.codeStyleSettings

        val spacingBuilder = createSpacingBuilder(settings)

        val rootBlock = WebIDLBlock(
            node = formattingContext.node,
            wrap = Wrap.createWrap(WrapType.NONE, false),
            alignment = Alignment.createAlignment(),
            spacingBuilder = spacingBuilder
        )

        return FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            rootBlock,
            settings
        )
    }

    private fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
        return SpacingBuilder(settings, WebIDLLanguage)
            .around(WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Colon]).spaceIf(true)
            .before(WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.LBrace]).spaceIf(true)
    }
}

class WebIDLBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val spacingBuilder: SpacingBuilder
) : AbstractBlock(node, wrap, alignment) {

    override fun buildChildren(): MutableList<Block> {
        return myNode.getChildren(null)
            .filter { it.elementType != TokenType.WHITE_SPACE && it.textRange.length > 0 }
            .map { child ->
                WebIDLBlock(
                    child,
                    Wrap.createWrap(WrapType.NONE, false),
                    Alignment.createAlignment(),
                    spacingBuilder
                )
            }
            .toMutableList()
    }

    override fun getIndent(): Indent? {
        val parentType = myNode.treeParent?.elementType
        val currentType = myNode.elementType

        return when {
            // Don't indent braces themselves
            currentType in listOf(
                WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.LBrace],
                WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.RBrace],
                WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Interface],
                WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Semicolon]
            ) -> Indent.getNoneIndent()

            // Indent members inside interface body
            parentType?.toString() == "BlockBody" -> Indent.getNormalIndent()

            // Don't indent top-level declarations
            else -> Indent.getNoneIndent()
        }
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }
}