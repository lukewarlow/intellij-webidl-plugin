package dev.lukewarlow.intellijwebidlplugin

import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import webidl.generated.WebIDLLexer

class WebIDLDocumentationTargetProvider : DocumentationTargetProvider {
    override fun documentationTargets(file: PsiFile, offset: Int): List<DocumentationTarget> {
        val element = file.findElementAt(offset) ?: return emptyList()
        val type = element.node?.elementType

        if (type !is WebIDLTokenType) return emptyList()

        return when (type.antlrTokenType) {
            WebIDLLexer.Tokens.Interface -> listOf(KeywordDocumentationTarget(element, "https://webidl.spec.whatwg.org/#idl-interfaces"))
            WebIDLLexer.Tokens.Dictionary -> listOf(KeywordDocumentationTarget(element, "https://webidl.spec.whatwg.org/#idl-dictionaries"))
            WebIDLLexer.Tokens.Typedef -> listOf(KeywordDocumentationTarget(element, "https://webidl.spec.whatwg.org/#idl-typedefs"))
            else -> return emptyList()
        }
    }
}

class KeywordDocumentationTarget(
    private val element: PsiElement,
    private val specURL: String
) : DocumentationTarget {
    override fun computePresentation(): TargetPresentation {
        return TargetPresentation.builder(element.text)
            .presentation()
    }

    override fun computeDocumentation(): DocumentationResult {
        return DocumentationResult.documentation("""<p><a href="$specURL" target="_blank">Read the spec</a></p>""")
    }

    override fun createPointer(): Pointer<out DocumentationTarget> = Pointer.hardPointer(this)
}
