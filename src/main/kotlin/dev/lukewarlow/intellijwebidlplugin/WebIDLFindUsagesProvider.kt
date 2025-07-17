package dev.lukewarlow.intellijwebidlplugin

import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement

class WebIDLFindUsagesProvider : FindUsagesProvider {
    override fun canFindUsagesFor(element: PsiElement): Boolean {
        return element is WebIDLPsiIdentifier
    }

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String = "interface"

    override fun getDescriptiveName(element: PsiElement): String {
        return (element as? PsiNamedElement)?.name ?: ""
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return (element as? PsiNamedElement)?.name ?: ""
    }

    override fun getWordsScanner(): WordsScanner? = null
}