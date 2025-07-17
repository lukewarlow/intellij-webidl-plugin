package dev.lukewarlow.intellijwebidlplugin

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.JBColor

fun SourceRange.toTextRange(document: Document): TextRange = TextRange.create(
    document.getLineStartOffset(this.startLine - 1) + this.startColumn,
    document.getLineStartOffset(this.endLine - 1) + this.endColumn
)

class WebIDLAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // TODO
    }

    fun IDLIdentifier.annotate(holder: AnnotationHolder, document: Document) {
        val attributes = TextAttributes().apply {
            effectColor = JBColor.RED  // or any color you want
            effectType = EffectType.WAVE_UNDERSCORE
        }
        val textRange = this.sourceRange.toTextRange(document)
        if (this.value.first().isLowerCase()) {
            holder.newAnnotation(HighlightSeverity.ERROR, "Identifier '${this.value}' should start with an uppercase letter")
                .range(textRange)
                .textAttributes(TextAttributesKey.createTextAttributesKey("ZIGZAG_UNDERLINE", attributes))
                .withFix(CapitalizeIdentifierIntention(textRange))
                .create()
        }
    }
}

class CapitalizeIdentifierIntention(
    private val range: TextRange
) : IntentionAction {

    override fun getText(): String = "Capitalize identifier"
    override fun getFamilyName(): String = "WebIDL intentions"
    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile): Boolean = true

    override fun invoke(project: Project, editor: Editor?, file: PsiFile) {
        if (editor == null) return
        val document = editor.document
        val currentText = document.getText(range)
        if (currentText.isNotEmpty()) {
            val capitalized = currentText.replaceFirstChar { it.uppercaseChar() }
            document.replaceString(range.startOffset, range.endOffset, capitalized)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }

    override fun startInWriteAction(): Boolean = true
}