package dev.lukewarlow.intellijwebidlplugin

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.tree.IElementType
import org.antlr.v4.kotlinruntime.BaseErrorListener
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.RecognitionException
import org.antlr.v4.kotlinruntime.Recognizer
import org.antlr.v4.kotlinruntime.StringCharStream
import org.antlr.v4.kotlinruntime.Token
import webidl.generated.WebIDLLexer
import webidl.generated.WebIDLParser

class WebIDLPsiParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()

        val text = builder.originalText.toString()
        val lexer = WebIDLLexer(StringCharStream(text))
        val tokens = CommonTokenStream(lexer)
        val parser = WebIDLParser(tokens)

        val errorListener = SyntaxErrorListener()
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)

        val ast: IDLFile? = try {
            val tree = parser.file()
            WebIDLVisitor().visitFile(tree)
        } catch (t: Throwable) {
            println("parse: $t")
            null
        }

        if (ast != null) {
            // Safe to use AST-based structure
            buildPsiFromAST(builder, ast, errorListener.errors)
        } else {
            // Syntax errors detected: fallback to token-driven PSI builder
            buildFallbackPsi(builder, errorListener.errors)
        }

        while (!builder.eof()) {
            builder.advanceLexer()
        }

        rootMarker.done(root)
        builder.setDebugMode(true)
        return builder.treeBuilt
    }
}

class ErrorCursor(var index: Int)

fun buildPsiFromAST(builder: PsiBuilder, ast: IDLFile, errors: List<SyntaxErrorListener.SyntaxError>) {
    val errorCursor = ErrorCursor(0)
    for (definition in ast.definitions) {
        when (definition) {
            is IDLInterface,
            is IDLPartialInterface,
            is IDLDictionary,
            is IDLPartialDictionary,
            is IDLMixin,
            is IDLPartialMixin,
            is IDLNamespace,
            is IDLPartialNamespace,
            is IDLEnum,
            is IDLCallbackInterface -> {
                buildBlock(builder, definition, definition::class.simpleName ?: "Unknown", errors, errorCursor)
            }
            is IDLCallbackFunction,
            is IDLTypedef,
            is IDLIncludes -> {
                buildNonBlockDefinitions(builder, definition, definition::class.simpleName ?: "Unknown", errors, errorCursor)
            }

            else -> {
                val endOffset = definition.sourceRange.endOffset

                while (!builder.eof() && builder.currentOffset < endOffset) {
                    val offset = builder.currentOffset

                    // Check for lexer error at this position
                    while (errorCursor.index < errors.size &&
                        errors[errorCursor.index].startIndex <= offset
                    ) {
                        val err = errors[errorCursor.index]
                        val errMarker = builder.mark()

                        if (err.endIndex > err.startIndex && builder.currentOffset < err.endIndex) {
                            while (!builder.eof() && builder.currentOffset < err.endIndex) {
                                builder.advanceLexer()
                            }
                        } else if (!builder.eof()) {
                            builder.advanceLexer()
                        }

                        errMarker.error(err.msg)
                        errorCursor.index++
                    }

                    builder.advanceLexer()
                }
            }
        }
    }

    // Consume any remaining tokens
    while (!builder.eof()) {
        val offset = builder.currentOffset

        while (errorCursor.index < errors.size && errors[errorCursor.index].startIndex <= offset) {
            val err = errors[errorCursor.index]
            val marker = builder.mark()
            if (err.endIndex > err.startIndex && builder.currentOffset < err.endIndex) {
                while (!builder.eof() && builder.currentOffset < err.endIndex) {
                    builder.advanceLexer()
                }
            } else if (!builder.eof()) {
                builder.advanceLexer()
            }
            marker.error(err.msg)
            errorCursor.index++
        }

        builder.advanceLexer()
    }

    while (errorCursor.index < errors.size) {
        val err = errors[errorCursor.index]
        val marker = builder.mark()
        marker.error(err.msg)
        errorCursor.index++
    }
}

fun buildBlock(builder: PsiBuilder, astNode: IDLDefinition, debugName: String, syntaxErrors: List<SyntaxErrorListener.SyntaxError>,
               errorCursor: ErrorCursor) {
    val marker = builder.mark()
    val endOffset = astNode.sourceRange.endOffset
    var insideBody = false
    var bodyMarker: PsiBuilder.Marker? = null

    while (!builder.eof() && builder.currentOffset < endOffset) {
        val offset = builder.currentOffset

        while (errorCursor.index < syntaxErrors.size && syntaxErrors[errorCursor.index].startIndex <= offset) {
            val err = syntaxErrors[errorCursor.index]
            val errMarker = builder.mark()

            if (err.endIndex > err.startIndex && builder.currentOffset < err.endIndex) {
                while (!builder.eof() && builder.currentOffset < err.endIndex) {
                    builder.advanceLexer()
                }
            } else if (!builder.eof()) {
                builder.advanceLexer()
            }

            errMarker.error(err.msg)
            errorCursor.index++
        }

        val token = builder.tokenType as? WebIDLTokenType
        if (astNode is IDLInterface && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLInterfaceIdentifierElementType(astNode.identifier))
                astNode.inheritance?.value -> idMarker.done(WebIDLInterfaceInheritanceIdentifierElementType(astNode.identifier, astNode.inheritance!!))
                else -> idMarker.drop()
            }
            continue
        }
        if (astNode is IDLDictionary && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLDictionaryIdentifierElementType(astNode.identifier))
                astNode.inheritance?.value -> idMarker.done(WebIDLDictionaryInheritanceIdentifierElementType(astNode.identifier, astNode.inheritance!!))
                else -> idMarker.drop()
            }
            continue
        }
        if (astNode is IDLMixin && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLMixinIdentifierElementType(astNode.identifier))
                else -> idMarker.drop()
            }
            continue
        }
        if (astNode is IDLPartialMixin && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLPartialMixinIdentifierElementType(astNode.identifier))
                else -> idMarker.drop()
            }
            continue
        }
        if (astNode is IDLPartialInterface && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLPartialInterfaceIdentifierElementType(astNode.identifier))
                else -> idMarker.drop()
            }
            continue
        }
        if (astNode is IDLPartialDictionary && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLPartialDictionaryIdentifierElementType(astNode.identifier))
                else -> idMarker.drop()
            }
            continue
        }
        if (astNode is IDLNamespace && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLNamespaceIdentifierElementType(astNode.identifier))
                else -> idMarker.drop()
            }
            continue
        }
        if (astNode is IDLPartialNamespace && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLPartialNamespaceIdentifierElementType(astNode.identifier))
                else -> idMarker.drop()
            }
            continue
        }
        if (astNode is IDLCallbackInterface && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLCallbackInterfaceIdentifierElementType(astNode.identifier))
                else -> idMarker.drop()
            }
            continue
        }
        if (astNode is IDLEnum && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLEnumIdentifierElementType(astNode.identifier))
                else -> idMarker.drop()
            }
            continue
        }

        if (!insideBody && token?.antlrTokenType == WebIDLLexer.Tokens.LBrace) {
            bodyMarker = builder.mark()
            insideBody = true
        }

        if (insideBody && token?.antlrTokenType == WebIDLLexer.Tokens.RBrace) {
            builder.advanceLexer() // consume the }
            bodyMarker?.done(WebIDLBlockBodyElementType(astNode)) // finish the body node
            insideBody = false
            continue
        }

        if (astNode is IDLMemberHolder && insideBody && token?.antlrTokenType != WebIDLLexer.Tokens.RBracket) {
            for (member in astNode.members) {
                buildMember(builder, member, member::class.simpleName!!, syntaxErrors, errorCursor)
            }
        }

        builder.advanceLexer()
    }

    if (insideBody) {
        bodyMarker?.done(WebIDLBlockBodyElementType(astNode))
    }

    marker.done(WebIDLBlockElementType(astNode, debugName))
}

fun buildNonBlockDefinitions(builder: PsiBuilder, astNode: IDLDefinition, debugName: String, syntaxErrors: List<SyntaxErrorListener.SyntaxError>,
               errorCursor: ErrorCursor) {
    val marker = builder.mark()
    val endOffset = astNode.sourceRange.endOffset

    while (!builder.eof() && builder.currentOffset < endOffset) {
        val offset = builder.currentOffset

        while (errorCursor.index < syntaxErrors.size && syntaxErrors[errorCursor.index].startIndex <= offset) {
            val err = syntaxErrors[errorCursor.index]
            val errMarker = builder.mark()

            if (err.endIndex > err.startIndex && builder.currentOffset < err.endIndex) {
                while (!builder.eof() && builder.currentOffset < err.endIndex) {
                    builder.advanceLexer()
                }
            } else if (!builder.eof()) {
                builder.advanceLexer()
            }

            errMarker.error(err.msg)
            errorCursor.index++
        }

        val token = builder.tokenType as? WebIDLTokenType
        if (astNode is IDLIncludes && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLIncludesInterfaceIdentifierElementType(astNode.identifier))
                astNode.mixinName.value -> idMarker.done(WebIDLIncludesMixinIdentifierElementType(astNode.identifier, astNode.mixinName))
                else -> idMarker.drop()
            }
            continue
        }
        if (astNode is IDLCallbackFunction && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLCallbackFunctionIdentifierElementType(astNode.identifier))
                else -> idMarker.drop()
            }
            continue
        }
        if (astNode is IDLTypedef && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.identifier.value -> idMarker.done(WebIDLTypedefIdentifierElementType(astNode.identifier))
                else -> idMarker.drop()
            }
            continue
        }
        builder.advanceLexer()
    }

    marker.done(WebIDLBlockElementType(astNode, debugName))
}

fun buildMember(builder: PsiBuilder, astNode: IDLMember, debugName: String, syntaxErrors: List<SyntaxErrorListener.SyntaxError>, errorCursor: ErrorCursor) {
    val marker = builder.mark()
    val endOffset = astNode.sourceRange.endOffset

    while (!builder.eof() && builder.currentOffset < endOffset) {
        val offset = builder.currentOffset

        while (errorCursor.index < syntaxErrors.size && syntaxErrors[errorCursor.index].startIndex <= offset) {
            val err = syntaxErrors[errorCursor.index]
            val errMarker = builder.mark()

            if (err.endIndex > err.startIndex && builder.currentOffset < err.endIndex) {
                while (!builder.eof() && builder.currentOffset < err.endIndex) {
                    builder.advanceLexer()
                }
            } else if (!builder.eof()) {
                builder.advanceLexer()
            }

            errMarker.error(err.msg)
            errorCursor.index++
        }

        val token = builder.tokenType as? WebIDLTokenType
        if (astNode is IDLAttribute && token?.antlrTokenType == WebIDLLexer.Tokens.Identifier) {
            val idMarker = builder.mark()
            val tokenText = builder.tokenText
            builder.advanceLexer()
            when (tokenText) {
                astNode.name.value -> idMarker.done(WebIDLAttributeIdentifierElementType(astNode.name))
                else -> idMarker.drop()
            }
            continue
        }
        builder.advanceLexer()
    }

    marker.done(WebIDLMemberElementType(astNode, debugName))
}

fun buildFallbackPsi(builder: PsiBuilder, syntaxErrors: List<SyntaxErrorListener.SyntaxError>) {
    val sortedErrors = syntaxErrors.sortedBy { it.startIndex }
    var currentErrorIndex = 0

    while (!builder.eof()) {
        val offset = builder.currentOffset

        // Show error markers
        while (currentErrorIndex < sortedErrors.size && sortedErrors[currentErrorIndex].startIndex <= offset) {
            val err = sortedErrors[currentErrorIndex]
            val marker = builder.mark()

            if (err.endIndex > err.startIndex && builder.currentOffset < err.endIndex) {
                while (!builder.eof() && builder.currentOffset < err.endIndex) {
                    builder.advanceLexer()
                }
            } else if (!builder.eof()) {
                builder.advanceLexer()
            }

            marker.error(err.msg)
            currentErrorIndex++
        }

        builder.advanceLexer()
    }

    // Final errors at EOF
    while (currentErrorIndex < sortedErrors.size) {
        val err = sortedErrors[currentErrorIndex]
        val marker = builder.mark()
        marker.error(err.msg)
        currentErrorIndex++
    }
}

class SyntaxErrorListener : BaseErrorListener() {
    data class SyntaxError(val msg: String, val startIndex: Int, val endIndex: Int)

    val errors = mutableListOf<SyntaxError>()


    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?
    ) {
        val token = offendingSymbol as? Token ?: return

        val start = token.startIndex
        val end = token.stopIndex + 1

        if (start >= 0 && end >= 0 && end >= start) {
            errors.add(SyntaxError(msg, start, end))
        } else {
            Logger.getInstance(SyntaxErrorListener::class.java)
                .warn("Skipping syntax error with invalid offsets: $msg [start=$start, end=$end]")
        }
    }
}