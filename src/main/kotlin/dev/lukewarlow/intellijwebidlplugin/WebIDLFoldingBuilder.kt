package dev.lukewarlow.intellijwebidlplugin

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor

class WebIDLFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(
        root: PsiElement,
        document: Document,
        quick: Boolean
    ): Array<out FoldingDescriptor?> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        root.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)

                if (element is ASTWrapperPsiElement && element.node.elementType is WebIDLBlockBodyElementType) {
                    val range = element.textRange
                    if (range.length > 2) {
                        try {
                            descriptors.add(FoldingDescriptor(element.node, element.textRange))
                        } catch (e: IllegalArgumentException) {
                        }

                    }
                }
            }
        })

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return "{...}"
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}