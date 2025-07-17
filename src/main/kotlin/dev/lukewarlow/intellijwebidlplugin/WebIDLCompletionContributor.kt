package dev.lukewarlow.intellijwebidlplugin

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.CompletionUtil
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentsWithSelf
import com.intellij.util.ProcessingContext

class WebIDLCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(WebIDLLanguage),
            WebIDLCompletionProvider()
        )
    }
}

class WebIDLCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        val position = parameters.position

        val prevLeaf = PsiTreeUtil.prevVisibleLeaf(position)
        val prevText = prevLeaf?.text

        val prefix = CompletionUtil.findReferenceOrAlphanumericPrefix(parameters)
        val filtered = resultSet.withPrefixMatcher(prefix)

        if (PsiTreeUtil.getParentOfType(position, WebIDLBlockBodyPsiElement::class.java, false) != null) {
            // TODO make this way more intelligent
            // Block level complete
            val suggestions = when (prevText) {
                "readonly" -> listOf("attribute")
                "inherit" -> listOf("attribute")
                else -> listOf("readonly", "static", "attribute", "inherit", "undefined")
            }

            suggestions
                .filter { it.startsWith(prefix) }
                .forEach {
                    val element = LookupElementBuilder.create(it)
                    filtered.addElement(element)
                }

            return
        }

        // Top-level complete

        val definitionSuggestions = listOf(
            "interface", "interface mixin", "dictionary", "namespace",
            "partial interface", "partial interface mixin", "partial dictionary", "partial namespace",
            "enum", "typedef", "callback", "callback interface"
        )

        val suggestions = when (prevText) {
            "partial" -> listOf("interface", "interface mixin", "dictionary", "namespace")
            "callback" -> listOf("interface")
            "interface" -> listOf("mixin")
            in (definitionSuggestions.map { it.split(' ').last() }) -> listOf()
            else -> definitionSuggestions
        }

        val priorities = mapOf(
            "partial dictionary" to 80.0,
            "partial namespace" to 60.0,
            "partial interface mixin" to 60.0,
            "dictionary" to 80.0,
            "namespace" to 60.0,
        )

        suggestions
            .filter { it.startsWith(prefix) }
            .forEach {
                val element = LookupElementBuilder.create(it)
                val priority = priorities[it] ?: 100.0
                filtered.addElement(PrioritizedLookupElement.withPriority(element, priority))
            }
    }
}