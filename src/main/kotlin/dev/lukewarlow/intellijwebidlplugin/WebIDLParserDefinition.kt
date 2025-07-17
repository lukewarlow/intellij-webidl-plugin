package dev.lukewarlow.intellijwebidlplugin

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.StringCharStream
import webidl.generated.WebIDLLexer
import webidl.generated.WebIDLParser

class WebIDLParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(WebIDLLanguage)

        val COMMENTS = TokenSet.create(
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.BlockComment],
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.LineComment],
        )

        val WHITESPACE = TokenSet.create(
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Whitespace],
        )

        val STRINGS = TokenSet.create(
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.Quote],
            WebIDLElementTypes.tokenTypeMap[WebIDLLexer.Tokens.String],
        )
    }

    override fun createLexer(project: Project?) = WebIDLLexerProvider()
    override fun createParser(project: Project?) = WebIDLPsiParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getWhitespaceTokens(): TokenSet = WHITESPACE
    override fun getCommentTokens(): TokenSet = COMMENTS
    override fun getStringLiteralElements(): TokenSet = STRINGS

    override fun createFile(viewProvider: FileViewProvider): PsiFile = WebIDLPsiFile(viewProvider)

    override fun createElement(node: ASTNode): PsiElement {
        (node.elementType as? WebIDLIdentifierElementType)?.let { type ->
            return WebIDLPsiIdentifier(node)
        }
        (node.elementType as? WebIDLBlockElementType)?.let { type ->
            return WebIDLBlockBodyPsiElement(node)
        }

        return ASTWrapperPsiElement(node)
    }

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode) =
        ParserDefinition.SpaceRequirements.MAY

}

class WebIDLPsiFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, WebIDLLanguage) {
    override fun getFileType() = WebIDLFileType.INSTANCE

    fun getWebIDLRoot(): IDLFile {
        val input = StringCharStream(text)
        val lexer = WebIDLLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = WebIDLParser(tokens)

        return WebIDLVisitor().visitFile(parser.file())
    }

    override fun toString(): String = "WebIDL File"
}

class WebIDLPsiIdentifier(node: ASTNode) : ASTWrapperPsiElement(node), PsiNamedElement {
    override fun getName(): String? {
        val elementType = node.elementType as WebIDLIdentifierElementType
        return elementType.identifier.value
    }

    override fun setName(name: String): PsiElement {
        throw UnsupportedOperationException()
    }

    override fun getReference(): PsiReference? {
        if (node.elementType !is WebIDLDictionaryInheritanceIdentifierElementType &&
        node.elementType !is WebIDLInterfaceInheritanceIdentifierElementType &&
        node.elementType !is WebIDLPartialMixinIdentifierElementType &&
        node.elementType !is WebIDLPartialInterfaceIdentifierElementType &&
        node.elementType !is WebIDLPartialDictionaryIdentifierElementType &&
        node.elementType !is WebIDLPartialNamespaceIdentifierElementType &&
        node.elementType !is WebIDLIncludesInterfaceIdentifierElementType &&
        node.elementType !is WebIDLIncludesMixinIdentifierElementType) return null
        return WebIDLReference(this)
    }
}

class WebIDLReference(private val element: WebIDLPsiIdentifier) : PsiReferenceBase<WebIDLPsiIdentifier>(element,
    TextRange(0, element.textLength)
) {
    override fun resolve(): PsiElement? {
        return when (element.node.elementType) {
            is WebIDLPartialInterfaceIdentifierElementType,
            is WebIDLIncludesInterfaceIdentifierElementType,
            is WebIDLInterfaceInheritanceIdentifierElementType -> findParentInterfaceByName(element.project, element.text)
            is WebIDLPartialDictionaryIdentifierElementType,
            is WebIDLDictionaryInheritanceIdentifierElementType -> findParentDictionaryByName(element.project, element.text)
            is WebIDLIncludesMixinIdentifierElementType,
            is WebIDLPartialMixinIdentifierElementType -> findMixinByName(element.project, element.text)
            is WebIDLPartialNamespaceIdentifierElementType -> findNamespaceByName(element.project, element.text)
            else -> null
        }
    }

    override fun getVariants(): Array<Any> = emptyArray()
}

class WebIDLTokenType(
    val name: String,
    val antlrTokenType: Int
) : IElementType(name, WebIDLLanguage) {
    override fun toString(): String = "WebIDLTokenType.$name"
}

open class WebIDLBlockElementType(val idlDefinition: IDLDefinition, debugName: String) : IElementType(debugName, WebIDLLanguage)

open class WebIDLIdentifierElementType(val identifier: IDLIdentifier, debugName: String) : IElementType(debugName, WebIDLLanguage)
open class WebIDLInterfaceIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "InterfaceIdentifier")
open class WebIDLPartialInterfaceIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "PartialInterfaceIdentifier")
open class WebIDLInterfaceInheritanceIdentifierElementType(val childIdentifier: IDLIdentifier, superIdentifier: IDLIdentifier) : WebIDLIdentifierElementType(superIdentifier, "InterfaceInheritanceIdentifier")
open class WebIDLDictionaryIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "DictionaryIdentifier")
open class WebIDLPartialDictionaryIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "PartialDictionaryIdentifier")
open class WebIDLDictionaryInheritanceIdentifierElementType(val childIdentifier: IDLIdentifier, superIdentifier: IDLIdentifier) : WebIDLIdentifierElementType(superIdentifier, "DictionaryInheritanceIdentifier")
open class WebIDLMixinIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "MixinIdentifier")
open class WebIDLPartialMixinIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "PartialMixinIdentifier")
open class WebIDLIncludesInterfaceIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "IncludesInterfaceIdentifier")
open class WebIDLIncludesMixinIdentifierElementType(val interfaceIdentifier: IDLIdentifier, mixinIdentifier: IDLIdentifier) : WebIDLIdentifierElementType(mixinIdentifier, "IncludesMixinIdentifier")
open class WebIDLNamespaceIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "NamespaceIdentifier")
open class WebIDLPartialNamespaceIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "PartialNamespaceIdentifier")
open class WebIDLCallbackInterfaceIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "CallbackInterfaceIdentifier")
open class WebIDLEnumIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "EnumIdentifier")
open class WebIDLTypedefIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "TypedefIdentifier")
open class WebIDLCallbackFunctionIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "CallbackFunctionIdentifier")

open class WebIDLAttributeIdentifierElementType(identifier: IDLIdentifier) : WebIDLIdentifierElementType(identifier, "AttributeIdentifier")

open class WebIDLBlockBodyElementType(val definition: IDLDefinition) : IElementType("BlockBody", WebIDLLanguage)
open class WebIDLMemberElementType(val member: IDLMember, debugName: String) : IElementType(debugName, WebIDLLanguage)

class WebIDLBlockBodyPsiElement(node: ASTNode) : ASTWrapperPsiElement(node)