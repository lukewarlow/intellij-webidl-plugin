package dev.lukewarlow.intellijwebidlplugin

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType

class WebIDLLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (element !is WebIDLPsiIdentifier) return
        if (element.node.elementType !is WebIDLIdentifierElementType) return
        (element.parent as? ASTWrapperPsiElement)?.let { parent ->
            (parent.elementType as? WebIDLBlockElementType)?.let { parentType ->
                (parentType.idlDefinition as? IDLInterface)?.let { interfaceIdl ->
                    if (interfaceIdl.inheritance != null) {
                        if (element.node.elementType is WebIDLInterfaceInheritanceIdentifierElementType) {
                            val target = findParentInterfaceByName(element.project, element.text)
                            if (target != null && element.firstChild is LeafPsiElement) {
                                result.add(
                                    NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementingMethod)
                                        .setTooltipText("Go to interface")
                                        .setTargets(target)
                                        .createLineMarkerInfo(element.firstChild)
                                )
                            }
                        }
                    }

                    if (element.node.elementType is WebIDLInterfaceIdentifierElementType) {
                        val target = findChildInterfacesByName(element.project, element.text)
                        if (target.isNotEmpty() && element.firstChild is LeafPsiElement) {
                            result.add(
                                NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementedMethod)
                                    .setTooltipText("Go to interfaces")
                                    .setTargets(target)
                                    .createLineMarkerInfo(element.firstChild)
                            )
                        }
                    }
                }
                (parentType.idlDefinition as? IDLDictionary)?.let { idl ->
                    if (idl.inheritance != null) {
                        if (element.node.elementType is WebIDLDictionaryInheritanceIdentifierElementType) {
                            val target = findParentDictionaryByName(element.project, element.text)
                            if (target != null && element.firstChild is LeafPsiElement) {
                                result.add(
                                    NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementingMethod)
                                        .setTooltipText("Go to dictionary")
                                        .setTargets(target)
                                        .createLineMarkerInfo(element.firstChild)
                                )
                            }
                        }
                    }

                    if (element.node.elementType is WebIDLDictionaryIdentifierElementType) {
                        val target = findChildDictionariesByName(element.project, element.text)
                        if (target.isNotEmpty() && element.firstChild is LeafPsiElement) {
                            result.add(
                                NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementedMethod)
                                    .setTooltipText("Go to dictionaries")
                                    .setTargets(target)
                                    .createLineMarkerInfo(element.firstChild)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun findParentInterfaceByName(project: Project, name: String): WebIDLPsiIdentifier? {
    val scope = GlobalSearchScope.allScope(project)
    val files = FileTypeIndex.getFiles(WebIDLFileType.INSTANCE, scope)

    for (virtualFile in files) {
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: continue

        val candidates = PsiTreeUtil.findChildrenOfType(psiFile, WebIDLPsiIdentifier::class.java)
        for (identifier in candidates) {
            val parent = identifier.parent as? ASTWrapperPsiElement ?: continue
            val blockType = parent.node.elementType as? WebIDLBlockElementType ?: continue
            if (blockType.idlDefinition !is IDLInterface) continue
            if (identifier.text == name && blockType.idlDefinition.inheritance?.value != name)
                return identifier
        }
    }

    return null
}

fun findParentDictionaryByName(project: Project, name: String): WebIDLPsiIdentifier? {
    val scope = GlobalSearchScope.allScope(project)
    val files = FileTypeIndex.getFiles(WebIDLFileType.INSTANCE, scope)

    for (virtualFile in files) {
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: continue

        val candidates = PsiTreeUtil.findChildrenOfType(psiFile, WebIDLPsiIdentifier::class.java)
        for (identifier in candidates) {
            val parent = identifier.parent as? ASTWrapperPsiElement ?: continue
            val blockType = parent.node.elementType as? WebIDLBlockElementType ?: continue
            if (blockType.idlDefinition !is IDLDictionary) continue
            if (identifier.text == name && blockType.idlDefinition.inheritance?.value != name)
                return identifier
        }
    }

    return null
}

fun findChildInterfacesByName(project: Project, name: String): List<WebIDLPsiIdentifier> {
    val scope = GlobalSearchScope.allScope(project)
    val files = FileTypeIndex.getFiles(WebIDLFileType.INSTANCE, scope)

    val list = mutableListOf<WebIDLPsiIdentifier>()
    for (virtualFile in files) {
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: continue

        val candidates = PsiTreeUtil.findChildrenOfType(psiFile, WebIDLPsiIdentifier::class.java)
        for (identifier in candidates) {
            identifier.elementType as? WebIDLInterfaceIdentifierElementType ?: continue
            val parent = identifier.parent as? ASTWrapperPsiElement ?: continue
            val blockType = parent.node.elementType as? WebIDLBlockElementType ?: continue
            if (blockType.idlDefinition !is IDLInterface) continue
            if (blockType.idlDefinition.inheritance?.value == name)
                list.add(identifier)
        }
    }

    return list
}

fun findChildDictionariesByName(project: Project, name: String): List<WebIDLPsiIdentifier> {
    val scope = GlobalSearchScope.allScope(project)
    val files = FileTypeIndex.getFiles(WebIDLFileType.INSTANCE, scope)

    val list = mutableListOf<WebIDLPsiIdentifier>()
    for (virtualFile in files) {
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: continue

        val candidates = PsiTreeUtil.findChildrenOfType(psiFile, WebIDLPsiIdentifier::class.java)
        for (identifier in candidates) {
            identifier.elementType as? WebIDLDictionaryIdentifierElementType ?: continue
            val parent = identifier.parent as? ASTWrapperPsiElement ?: continue
            val blockType = parent.node.elementType as? WebIDLBlockElementType ?: continue
            if (blockType.idlDefinition !is IDLDictionary) continue
            if (blockType.idlDefinition.inheritance?.value == name)
                list.add(identifier)
        }
    }

    return list
}

fun findMixinByName(project: Project, name: String): WebIDLPsiIdentifier? {
    val scope = GlobalSearchScope.allScope(project)
    val files = FileTypeIndex.getFiles(WebIDLFileType.INSTANCE, scope)

    for (virtualFile in files) {
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: continue

        val candidates = PsiTreeUtil.findChildrenOfType(psiFile, WebIDLPsiIdentifier::class.java)
        for (identifier in candidates) {
            val parent = identifier.parent as? ASTWrapperPsiElement ?: continue
            val blockType = parent.node.elementType as? WebIDLBlockElementType ?: continue
            if (blockType.idlDefinition !is IDLMixin) continue
            if (identifier.text == name)
                return identifier
        }
    }

    return null
}

fun findNamespaceByName(project: Project, name: String): WebIDLPsiIdentifier? {
    val scope = GlobalSearchScope.allScope(project)
    val files = FileTypeIndex.getFiles(WebIDLFileType.INSTANCE, scope)

    for (virtualFile in files) {
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: continue

        val candidates = PsiTreeUtil.findChildrenOfType(psiFile, WebIDLPsiIdentifier::class.java)
        for (identifier in candidates) {
            val parent = identifier.parent as? ASTWrapperPsiElement ?: continue
            val blockType = parent.node.elementType as? WebIDLBlockElementType ?: continue
            if (blockType.idlDefinition !is IDLNamespace) continue
            if (identifier.text == name)
                return identifier
        }
    }

    return null
}