package dev.lukewarlow.intellijwebidlplugin

import org.antlr.v4.kotlinruntime.ParserRuleContext
import org.antlr.v4.kotlinruntime.tree.TerminalNode
import webidl.generated.WebIDLBaseVisitor
import webidl.generated.WebIDLParser
import kotlin.Exception

data class SourceRange(val startLine: Int, val startColumn: Int, val endLine: Int, val endColumn: Int, val startOffset: Int, val endOffset: Int)

sealed interface IDLNode {
    val sourceRange: SourceRange
}

data class IDLIdentifier(val value: String, override val sourceRange: SourceRange) : IDLNode

sealed class IDLDefinition(open val identifier: IDLIdentifier) : IDLNode {
    val extendedAttributes: MutableList<IDLExtendedAttribute> = mutableListOf()
}

data class IDLBrokenDefinition(override val identifier: IDLIdentifier,
                               override val sourceRange: SourceRange
) : IDLDefinition(identifier)

data class IDLFile(
    val definitions: List<IDLDefinition>,
    override val sourceRange: SourceRange
) : IDLNode

abstract class IDLMemberHolder(
    override val identifier: IDLIdentifier,
    open val members: List<IDLMember> = emptyList(),
) : IDLDefinition(identifier)

data class IDLInterface(
    override val identifier: IDLIdentifier,
    val inheritance: IDLIdentifier?,
    override val members: List<IDLMember> = emptyList(),
    override val sourceRange: SourceRange
) : IDLMemberHolder(identifier, members)

data class IDLMixin(
    override val identifier: IDLIdentifier,
    override val members: List<IDLMember> = emptyList(),
    override val sourceRange: SourceRange
) : IDLMemberHolder(identifier, members)

data class IDLDictionary(
    override val identifier: IDLIdentifier,
    val inheritance: IDLIdentifier?,
    val members: List<IDLDictionaryMember>,
    override val sourceRange: SourceRange
) : IDLDefinition(identifier)

data class IDLPartialInterface(
    override val identifier: IDLIdentifier,
    override val members: List<IDLMember> = emptyList(),
    override val sourceRange: SourceRange
) : IDLMemberHolder(identifier, members)

data class IDLPartialMixin(
    override val identifier: IDLIdentifier,
    override val members: List<IDLMember> = emptyList(),
    override val sourceRange: SourceRange
) : IDLMemberHolder(identifier, members)

data class IDLPartialDictionary(
    override val identifier: IDLIdentifier,
    val members: List<IDLDictionaryMember>,
    override val sourceRange: SourceRange
) : IDLDefinition(identifier)

data class IDLPartialNamespace(
    override val identifier: IDLIdentifier,
    override val members: List<IDLMember>,
    override val sourceRange: SourceRange
) : IDLMemberHolder(identifier, members)

data class IDLCallbackInterface(
    override val identifier: IDLIdentifier,
    override val members: List<IDLMember> = emptyList(),
    override val sourceRange: SourceRange
) : IDLMemberHolder(identifier, members)

data class IDLTypedef(
    override val identifier: IDLIdentifier,
    val type: IDLType,
    override val sourceRange: SourceRange
) : IDLDefinition(identifier)

data class IDLEnum(
    override val identifier: IDLIdentifier,
    val values: List<String>,
    override val sourceRange: SourceRange
) : IDLDefinition(identifier)

data class IDLIncludes(
    val interfaceName: IDLIdentifier,
    val mixinName: IDLIdentifier,
    override val sourceRange: SourceRange
) : IDLDefinition(interfaceName)

data class IDLNamespace(
    override val identifier: IDLIdentifier,
    val members: List<IDLMember>,
    override val sourceRange: SourceRange
) : IDLDefinition(identifier)

data class IDLCallbackFunction(
    override val identifier: IDLIdentifier,
    val returnType: IDLType,
    val arguments: List<IDLArgument> = emptyList(),
    override val sourceRange: SourceRange
) : IDLDefinition(identifier)

sealed class IDLType() : IDLNode {
    object Any : IDLType()
    object Void : IDLType()
    object Undefined : IDLType()
    object Boolean : IDLType()
    object Byte : IDLType()
    object Octet : IDLType()
    object Short : IDLType()
    object UnsignedShort : IDLType()
    object Long : IDLType()
    object UnsignedLong : IDLType()
    object LongLong : IDLType()
    object UnsignedLongLong : IDLType()
    object Float : IDLType()
    object UnrestrictedFloat : IDLType()
    object Double : IDLType()
    object UnrestrictedDouble : IDLType()
    object Bigint : IDLType()
    object DOMString : IDLType()
    object ByteString : IDLType()
    object USVString : IDLType()
    object Object : IDLType()
    object Symbol : IDLType()
    data class Identifier(val name: IDLIdentifier) : IDLType() // e.g. TrustedHTML
    data class Nullable(val inner: IDLType) : IDLType() // e.g. DOMString?
    data class Sequence(val inner: IDLType) : IDLType()
    data class AsyncSequence(val inner: IDLType) : IDLType()
    data class Record(val keyType: IDLType, val valueType: IDLType) : IDLType()
    data class Promise(val inner: IDLType): IDLType()
    data class Union(val options: List<IDLType>) : IDLType() // (TrustedHTML or DOMString)
    data class FrozenArray(val inner: IDLType) : IDLType()
    data class ObservableArray(val inner: IDLType) : IDLType()
    object ArrayBuffer : IDLType()
    object SharedArrayBuffer : IDLType()
    object DataView : IDLType()
    object Int8Array : IDLType()
    object Int16Array : IDLType()
    object Int32Array : IDLType()
    object Uint8Array : IDLType()
    object Uint16Array : IDLType()
    object Uint32Array : IDLType()
    object Uint8ClampedArray : IDLType()
    object BigInt64Array : IDLType()
    object BigUint64Array : IDLType()
    object Float16Array : IDLType()
    object Float32Array : IDLType()
    object Float64Array : IDLType()

    val extendedAttributes: MutableList<IDLExtendedAttribute> = mutableListOf()
    override lateinit var sourceRange: SourceRange
}

fun WebIDLParser.BufferRelatedTypeContext.toIDLType() = when (this.text) {
    "ArrayBuffer" -> IDLType.ArrayBuffer
    "SharedArrayBuffer" -> IDLType.SharedArrayBuffer
    "DataView" -> IDLType.DataView
    "Int8Array" -> IDLType.Int8Array
    "Int16Array" -> IDLType.Int16Array
    "Int32Array" -> IDLType.Int32Array
    "Uint8Array" -> IDLType.Uint8Array
    "Uint16Array" -> IDLType.Uint16Array
    "Uint32Array" -> IDLType.Uint32Array
    "Uint8ClampedArray" -> IDLType.Uint8ClampedArray
    "BigInt64Array" -> IDLType.BigInt64Array
    "BigUint64Array" -> IDLType.BigUint64Array
    "Float16Array" -> IDLType.Float16Array
    "Float32Array" -> IDLType.Float32Array
    "Float64Array" -> IDLType.Float64Array
    else -> throw Exception("Unknown BufferRelatedType ${this.text}")
}

fun WebIDLParser.StringTypeContext.toIDLType() = when (this.text) {
    "DOMString" -> IDLType.DOMString
    "ByteString" -> IDLType.ByteString
    "USVString" -> IDLType.USVString
    else -> throw Exception("Unknown StringType ${this.text}")
}

fun WebIDLParser.PrimitiveTypeContext.toIDLType() = when (this.text) {
    "unsignedshort" -> IDLType.UnsignedShort
    "long" -> IDLType.Long
    "unsignedlong" -> IDLType.UnsignedLong
    "longlong" -> IDLType.LongLong
    "unsignedlonglong" -> IDLType.UnsignedLongLong
    "float" -> IDLType.Float
    "unrestrictedfloat" -> IDLType.UnrestrictedFloat
    "double" -> IDLType.Double
    "unrestricteddouble" -> IDLType.UnrestrictedDouble
    "boolean" -> IDLType.Boolean
    "byte" -> IDLType.Byte
    "octet" -> IDLType.Octet
    "short" -> IDLType.Short
    "bigint" -> IDLType.Bigint
    else -> throw Exception("Unknown PrimitiveType ${this.text}")
}

sealed class IDLMember : IDLNode {
    val extendedAttributes: MutableList<IDLExtendedAttribute> = mutableListOf()
}

data class IDLBrokenMember(override val sourceRange: SourceRange) : IDLMember()

data class IDLConstructor(
    val arguments: List<IDLArgument>,
    override val sourceRange: SourceRange
) : IDLMember()

data class IDLConstant(
    val name: IDLIdentifier,
    val type: IDLType,
    val value: String,
    override val sourceRange: SourceRange
) : IDLMember()

data class IDLAttribute(
    val name: IDLIdentifier,
    val type: IDLType,
    var readonly: Boolean = false,
    var static: Boolean = false,
    var stringifier: Boolean = false,
    val inherit: Boolean = false,
    override val sourceRange: SourceRange
) : IDLMember()

enum class OperationType {
    REGULAR,
    STRINGIFIER,
    GETTER,
    SETTER,
    DELETER,
}

data class IDLOperation(
    val name: IDLIdentifier?,
    val returnType: IDLType,
    val arguments: List<IDLArgument>,
    var static: Boolean = false,
    var operationType: OperationType = OperationType.REGULAR,
    override val sourceRange: SourceRange
) : IDLMember()

data class IDLDictionaryMember(
    val name: IDLIdentifier,
    val type: IDLType,
    val required: Boolean = false,
    val defaultValue: String? = null,
    val extendedAttributes: List<IDLExtendedAttribute>,
    override val sourceRange: SourceRange
) : IDLNode

data class IDLMaplike(
    val keyType: IDLType,
    val valueType: IDLType,
    val readonly: Boolean,
    override val sourceRange: SourceRange
) : IDLMember()

data class IDLSetlike(
    val elementType: IDLType,
    val readonly: Boolean,
    override val sourceRange: SourceRange
) : IDLMember()

data class IDLIterable(
    val keyType: IDLType?,
    val valueType: IDLType,
    override val sourceRange: SourceRange
) : IDLMember()

data class IDLAsyncIterable(
    val keyType: IDLType?,
    val valueType: IDLType,
    val arguments: List<IDLArgument> = emptyList(),
    override val sourceRange: SourceRange
) : IDLMember()

data class IDLArgument(
    val name: IDLIdentifier,
    val type: IDLType,
    val optional: Boolean = false,
    val defaultValue: String? = null,
    val variadic: Boolean = false,
    val extendedAttributes: List<IDLExtendedAttribute> = emptyList(),
    override val sourceRange: SourceRange
) : IDLNode

data class IDLExtendedAttribute(
    val name: IDLIdentifier,
    val kind: Kind,
) : IDLNode {
    sealed class Kind {
        object NoArgs : Kind()
        data class ArgList(val arguments: List<IDLArgument>) : Kind()
        data class NamedArgList(val identifier: IDLIdentifier, val arguments: List<IDLArgument>) : Kind()
        data class Ident(val value: String) : Kind()
        data class StringArg(val value: String) : Kind()
        data class Decimal(val value: Double) : Kind()
        data class Integer(val value: Int) : Kind()
        data class IdentList(val values: List<String>) : Kind()
        data class IntegerList(val values: List<Int>) : Kind()
        data class StringList(val values: List<String>) : Kind()
        object Wildcard : Kind()
    }

    override lateinit var sourceRange: SourceRange
}

private fun processArgumentName(ctx: WebIDLParser.ArgumentNameContext): IDLIdentifier {
    return IDLIdentifier(value = when {
        ctx.Identifier() != null -> ctx.Identifier()!!.text
        ctx.argumentNameKeyword() != null -> ctx.argumentNameKeyword()!!.text
        else -> throw Exception("Missing argument name")
    }, sourceRangeOf(ctx))
}

private fun processAttributeName(ctx: WebIDLParser.AttributeNameContext): IDLIdentifier {
    return IDLIdentifier(value = when {
        ctx.Identifier() != null -> ctx.Identifier()!!.text
        ctx.attributeNameKeyword() != null -> ctx.attributeNameKeyword()!!.text
        else -> throw Exception("Missing attribute name")
    }, sourceRangeOf(ctx))
}

private fun processOperationName(ctx: WebIDLParser.OperationNameContext?): IDLIdentifier? {
    if (ctx == null) return null
    return IDLIdentifier(value = when {
        ctx.Identifier() != null -> ctx.Identifier()!!.text
        ctx.operationNameKeyword() != null -> ctx.operationNameKeyword()!!.text
        else -> throw Exception("Missing operation name")
    }, sourceRangeOf(ctx))
}

fun sourceRangeOf(ctx: ParserRuleContext): SourceRange {
    val start = ctx.start!!
    val end = ctx.stop!!
    return SourceRange(
        start.line,
        start.charPositionInLine,
        end.line,
        end.charPositionInLine + end.text!!.length,
        start.startIndex,
        end.stopIndex + 1
    )
}

fun TerminalNode.sourceRange(): SourceRange {
    val token = this.symbol
    return SourceRange(
        token.line,
        token.charPositionInLine,
        token.line,
        token.charPositionInLine + token.text!!.length,
        token.startIndex,
        token.stopIndex + 1
    )
}

fun TerminalNode.toIDLIdentifier(): IDLIdentifier {
    return IDLIdentifier(this.text, this.sourceRange())
}

/**
 * Visitor that collects the interface name and its simple read/write attributes.
 */
class WebIDLVisitor() : WebIDLBaseVisitor<IDLNode?>() {
    override fun defaultResult(): IDLNode? = null

    override fun visitFile(ctx: WebIDLParser.FileContext): IDLFile {
        val definitions = mutableListOf<IDLDefinition>()
        collectDefinitions(ctx.definitions(), definitions)
        return IDLFile(definitions, sourceRangeOf(ctx))
    }

    private fun collectDefinitions(ctx: WebIDLParser.DefinitionsContext?, accumulator: MutableList<IDLDefinition>) {
        if (ctx == null) return
        val definition: IDLDefinition = try {
            val def = visitDefinition(ctx.definition())
            collectExtendedAttributes(ctx.extendedAttributeList(), def.extendedAttributes)
            def
        } catch (t: Throwable) {
            println("collectDefinitions: $t")
            IDLBrokenDefinition(IDLIdentifier(ctx.text, sourceRangeOf(ctx)), sourceRangeOf(ctx))
        }
        accumulator += definition
        collectDefinitions(ctx.definitions(), accumulator) // recurse
    }

    override fun visitDefinition(ctx: WebIDLParser.DefinitionContext): IDLDefinition {
        return when {
            ctx.callbackOrInterfaceOrMixin() != null -> visitCallbackOrInterfaceOrMixin(ctx.callbackOrInterfaceOrMixin()!!)
            ctx.namespace() != null -> visitNamespace(ctx.namespace()!!)
            ctx.partial() != null -> visitPartial(ctx.partial()!!)
            ctx.dictionary() != null -> visitDictionary(ctx.dictionary()!!)
            ctx.enum() != null -> visitEnum(ctx.enum()!!)
            ctx.typedef() != null -> visitTypedef(ctx.typedef()!!)
            ctx.includesStatement() != null -> visitIncludesStatement(ctx.includesStatement()!!)
            else -> throw Exception("visitDefinition: ${ctx.text}")
        }
    }

    override fun visitCallbackOrInterfaceOrMixin(ctx: WebIDLParser.CallbackOrInterfaceOrMixinContext): IDLDefinition {
        return when {
            ctx.callbackRestOrInterface() != null -> visitCallbackRestOrInterface(ctx.callbackRestOrInterface()!!)
            ctx.interfaceOrMixin() != null -> visitInterfaceOrMixin(ctx.interfaceOrMixin()!!)
            else -> throw Exception("visitCallbackOrInterfaceOrMixin: ${ctx.text}")
        }
    }

    override fun visitNamespace(ctx: WebIDLParser.NamespaceContext): IDLNamespace {
        val name = ctx.Identifier().toIDLIdentifier()
        val members = mutableListOf<IDLMember>()
        collectNamespaceMembers(ctx.namespaceMembers(), members)
        return IDLNamespace(name, members, sourceRangeOf(ctx))
    }

    private fun collectNamespaceMembers(ctx: WebIDLParser.NamespaceMembersContext?, accumulator: MutableList<IDLMember>) {
        if (ctx == null) return
        val member = visitNamespaceMember(ctx.namespaceMember())
        if (member != null) accumulator += member
        collectNamespaceMembers(ctx.namespaceMembers(), accumulator) // recurse
    }

    override fun visitNamespaceMember(ctx: WebIDLParser.NamespaceMemberContext): IDLMember? {
        return when {
            ctx.const() != null -> visitConst(ctx.const()!!)
            ctx.regularOperation() != null -> visitRegularOperation(ctx.regularOperation()!!)
            // Namespace attributes are always readonly
            ctx.attributeRest() != null -> visitAttributeRest(ctx.attributeRest()!!).apply { readonly = true }
            else -> throw Exception("visitNamespaceMember: ${ctx.text}")
        }
    }

    override fun visitPartial(ctx: WebIDLParser.PartialContext): IDLDefinition {
        val def = ctx.partialDefinition()

        return when {
            def.partialInterfaceOrPartialMixin()?.partialInterfaceRest() != null -> {
                val partialInterface = def.partialInterfaceOrPartialMixin()!!.partialInterfaceRest()!!
                val name = partialInterface.Identifier().toIDLIdentifier()
                val members = mutableListOf<IDLMember>()
                collectPartialInterfaceMembers(partialInterface.partialInterfaceMembers(), members)
                IDLPartialInterface(name, members, sourceRangeOf(ctx))
            }
            def.partialInterfaceOrPartialMixin()?.mixinRest() != null -> {
                val mixin = def.partialInterfaceOrPartialMixin()!!.mixinRest()!!
                val name = mixin.Identifier().toIDLIdentifier()
                val members = mutableListOf<IDLMember>()
                collectMixinMembers(mixin.mixinMembers(), members)
                IDLPartialMixin(name, members, sourceRangeOf(ctx))
            }
            def.partialDictionary() != null -> {
                val partialDict = def.partialDictionary()!!
                val name = partialDict.Identifier().toIDLIdentifier()
                val members = mutableListOf<IDLDictionaryMember>()
                collectDictionaryMembers(partialDict.dictionaryMembers(), members)
                IDLPartialDictionary(name, members, sourceRangeOf(ctx))
            }
            def.namespace() != null -> {
                val namespace = def.namespace()!!
                val name = namespace.Identifier().toIDLIdentifier()
                val members = mutableListOf<IDLMember>()
                collectNamespaceMembers(namespace.namespaceMembers(), members)
                IDLPartialNamespace(name, members, sourceRangeOf(ctx))
            }
            else -> throw Exception("visitPartial: ${ctx.text}")
        }
    }

    private fun collectPartialInterfaceMembers(
        ctx: WebIDLParser.PartialInterfaceMembersContext?,
        accumulator: MutableList<IDLMember>
    ) {
        if (ctx == null) return
        val member = visitPartialInterfaceMember(ctx.partialInterfaceMember())
        collectExtendedAttributes(ctx.extendedAttributeList(), member.extendedAttributes)
        accumulator += member
        collectPartialInterfaceMembers(ctx.partialInterfaceMembers(), accumulator)
    }

    override fun visitDictionary(ctx: WebIDLParser.DictionaryContext): IDLDictionary {
        val name = ctx.Identifier().toIDLIdentifier()
        val inheritance = ctx.inheritance()?.Identifier()?.toIDLIdentifier()
        val members = mutableListOf<IDLDictionaryMember>()
        collectDictionaryMembers(ctx.dictionaryMembers(), members)

        return IDLDictionary(name, inheritance, members, sourceRangeOf(ctx))
    }

    override fun visitEnum(ctx: WebIDLParser.EnumContext): IDLEnum {
        val name = ctx.Identifier().toIDLIdentifier()
        val values = extractEnumValues(ctx.enumValueList())
        return IDLEnum(name, values, sourceRangeOf(ctx))
    }

    private fun extractEnumValues(ctx: WebIDLParser.EnumValueListContext): List<String> {
        val values = mutableListOf<String>()

        fun TerminalNode.unquoted(): String = text.trim('"')

        values += ctx.String().unquoted()

        var commaCtx = ctx.enumValueListComma()
        while (commaCtx != null) {
            val stringCtx = commaCtx.enumValueListString()
            if (stringCtx != null) {
                values += stringCtx.String().unquoted()
                commaCtx = stringCtx.enumValueListComma()
            } else {
                break
            }
        }

        return values
    }

    private fun collectDictionaryMembers(ctx: WebIDLParser.DictionaryMembersContext?, accumulator: MutableList<IDLDictionaryMember>) {
        if (ctx == null) return
        val member = visitDictionaryMember(ctx.dictionaryMember())
        if (member != null) accumulator += member
        collectDictionaryMembers(ctx.dictionaryMembers(), accumulator) // recurse
    }

    override fun visitDictionaryMember(ctx: WebIDLParser.DictionaryMemberContext): IDLDictionaryMember? {
        val rest = ctx.dictionaryMemberRest()
        val name = rest.Identifier().toIDLIdentifier()

        val extendedAttributes = mutableListOf<IDLExtendedAttribute>()
        collectExtendedAttributes(ctx.extendedAttributeList(), extendedAttributes)

        val type = if (rest.typeWithExtendedAttributes() != null)
            visitTypeWithExtendedAttributes(rest.typeWithExtendedAttributes()!!)
        else visitType(rest.type()!!)

        val required = rest.text.startsWith("required")

        val defaultValue = rest.default()?.defaultValue()?.let { defaultCtx ->
            when {
                defaultCtx.constValue() != null -> {
                    val constValue = defaultCtx.constValue()!!
                    constValue.booleanLiteral()?.text
                        ?: constValue.floatLiteral()?.text
                        ?: constValue.Integer()?.text
                        ?: throw Exception(constValue.text)
                }
                defaultCtx.String() != null -> defaultCtx.String()!!.text
                defaultCtx.text in listOf("[]", "{}", "null", "undefined") -> defaultCtx.text
                else -> throw Exception("visitDictionaryMember defaultValue: ${defaultCtx.text}")
            }
        }

        return IDLDictionaryMember(
            name = name,
            type = type,
            required = required,
            defaultValue = defaultValue,
            extendedAttributes = extendedAttributes,
            sourceRangeOf(ctx)
        )
    }

    override fun visitTypedef(ctx: WebIDLParser.TypedefContext): IDLTypedef =
        IDLTypedef(ctx.Identifier().toIDLIdentifier(), visitTypeWithExtendedAttributes(ctx.typeWithExtendedAttributes()), sourceRangeOf(ctx))

    override fun visitIncludesStatement(ctx: WebIDLParser.IncludesStatementContext): IDLIncludes {
        val identifiers = ctx.Identifier()
        check(identifiers.size == 2) {
            "Includes statement must have exactly two identifiers: ${ctx.text}"
        }

        val interfaceName = identifiers[0].toIDLIdentifier()
        val mixinName = identifiers[1].toIDLIdentifier()

        return IDLIncludes(interfaceName, mixinName, sourceRangeOf(ctx))
    }

    override fun visitCallbackRestOrInterface(ctx: WebIDLParser.CallbackRestOrInterfaceContext): IDLDefinition {
        ctx.callbackRest()?.let {
            return visitCallbackRest(it)
        }

        val identifier = ctx.Identifier()
        if (identifier == null)
            throw Exception(ctx.text)

        val name = identifier.toIDLIdentifier()
        val members = mutableListOf<IDLMember>()
        collectCallbackInterfaceMembers(ctx.callbackInterfaceMembers(), members)
        return IDLCallbackInterface(name, members, sourceRangeOf(ctx))
    }

    private fun collectCallbackInterfaceMembers(
        ctx: WebIDLParser.CallbackInterfaceMembersContext?,
        accumulator: MutableList<IDLMember>
    ) {
        if (ctx == null) return
        val member = visitCallbackInterfaceMember(ctx.callbackInterfaceMember())
        collectExtendedAttributes(ctx.extendedAttributeList(), member.extendedAttributes)
        accumulator += member
        collectCallbackInterfaceMembers(ctx.callbackInterfaceMembers(), accumulator)
    }

    override fun visitCallbackInterfaceMember(ctx: WebIDLParser.CallbackInterfaceMemberContext): IDLMember {
        return when {
            ctx.const() != null -> visitConst(ctx.const()!!)
            ctx.regularOperation() != null -> visitRegularOperation(ctx.regularOperation()!!)
            else -> throw Exception("visitCallbackInterfaceMember: ${ctx.text}")
        }
    }

    override fun visitCallbackRest(ctx: WebIDLParser.CallbackRestContext): IDLCallbackFunction {
        val name = ctx.Identifier().toIDLIdentifier()
        val returnType = visitType(ctx.type())
        val argumentList = collectArguments(ctx.argumentList())
        return IDLCallbackFunction(name, returnType, argumentList, sourceRangeOf(ctx))
    }

    override fun visitInterfaceOrMixin(ctx: WebIDLParser.InterfaceOrMixinContext): IDLDefinition {
        return when {
            ctx.interfaceRest() != null -> visitInterfaceRest(ctx.interfaceRest()!!)
            ctx.mixinRest() != null -> visitMixinRest(ctx.mixinRest()!!)
            else -> throw Exception("visitInterfaceOrMixin: ${ctx.text}")
        }
    }

    override fun visitInterfaceRest(ctx: WebIDLParser.InterfaceRestContext): IDLInterface {
        val name = ctx.Identifier().toIDLIdentifier()
        val inheritance = ctx.inheritance()?.Identifier()?.toIDLIdentifier()
        val members = mutableListOf<IDLMember>()
        collectInterfaceMembers(ctx.interfaceMembers(), members)
        return IDLInterface(name, inheritance, members, sourceRangeOf(ctx))
    }

    override fun visitMixinRest(ctx: WebIDLParser.MixinRestContext): IDLMixin {
        val name = ctx.Identifier().toIDLIdentifier()
        val members = mutableListOf<IDLMember>()

        collectMixinMembers(ctx.mixinMembers(), members)

        return IDLMixin(name, members, sourceRangeOf(ctx))
    }

    private fun collectMixinMembers(
        ctx: WebIDLParser.MixinMembersContext?,
        accumulator: MutableList<IDLMember>
    ) {
        if (ctx == null) return

        val member = visitMixinMember(ctx.mixinMember())
        collectExtendedAttributes(ctx.extendedAttributeList(), member.extendedAttributes)
        accumulator += member

        collectMixinMembers(ctx.mixinMembers(), accumulator) // recurse
    }

    override fun visitMixinMember(
        ctx: WebIDLParser.MixinMemberContext
    ): IDLMember {
        return when {
            ctx.const() != null -> visitConst(ctx.const()!!)
            ctx.regularOperation() != null -> visitRegularOperation(ctx.regularOperation()!!)
            ctx.stringifier() != null -> visitStringifier(ctx.stringifier()!!)
            ctx.attributeRest() != null -> visitAttributeRest(ctx.attributeRest()!!)
            else -> throw Exception("visitMixinMember: ${ctx.text}")
        }
    }

    private fun collectInterfaceMembers(ctx: WebIDLParser.InterfaceMembersContext?, accumulator: MutableList<IDLMember>) {
        if (ctx == null) return
        accumulator += try {
            val member = visitInterfaceMember(ctx.interfaceMember())
            collectExtendedAttributes(ctx.extendedAttributeList(), member.extendedAttributes)
            member
        } catch (t: Throwable) {
            println("collectIntefaceMembers: $t")
            IDLBrokenMember(sourceRangeOf(ctx))
        }
        collectInterfaceMembers(ctx.interfaceMembers(), accumulator) // recurse
    }

    override fun visitRegularOperation(ctx: WebIDLParser.RegularOperationContext): IDLOperation {
        val returnType = visitType(ctx.type())
        val opRest = ctx.operationRest()

        val name = processOperationName(opRest.operationName())
        val args = collectArguments(opRest.argumentList())

        return IDLOperation(
            name = name,
            returnType = returnType,
            arguments = args,
            static = false,
            sourceRange = sourceRangeOf(ctx)
        )
    }

    override fun visitSpecialOperation(ctx: WebIDLParser.SpecialOperationContext): IDLOperation {
        val specialKeyword = ctx.special().text
        val type = when (specialKeyword) {
            "getter" -> OperationType.GETTER
            "setter" -> OperationType.SETTER
            "deleter" -> OperationType.DELETER
            else -> throw Exception("Unknown special operation: $specialKeyword")
        }

        return visitRegularOperation(ctx.regularOperation()).apply {operationType = type}
    }

    override fun visitAttributeRest(ctx: WebIDLParser.AttributeRestContext): IDLAttribute {
        val type = visitTypeWithExtendedAttributes(ctx.typeWithExtendedAttributes())
        val name = processAttributeName(ctx.attributeName())
        return IDLAttribute(
            name = name,
            type = type,
            sourceRange = sourceRangeOf(ctx)
        )
    }

    override fun visitInterfaceMember(ctx: WebIDLParser.InterfaceMemberContext): IDLMember {
        return when {
            ctx.partialInterfaceMember() != null -> visitPartialInterfaceMember(ctx.partialInterfaceMember()!!)
            ctx.constructor() != null -> visitConstructor(ctx.constructor()!!)
            else -> throw Exception("visitInterfaceMember: ${ctx.text}")
        }
    }

    override fun visitPartialInterfaceMember(ctx: WebIDLParser.PartialInterfaceMemberContext): IDLMember {
        return when {
            ctx.const() != null -> visitConst(ctx.const()!!)
            ctx.operation() != null -> visitOperation(ctx.operation()!!)
            ctx.stringifier() != null -> visitStringifier(ctx.stringifier()!!)
            ctx.staticMember() != null -> visitStaticMember(ctx.staticMember()!!)
            ctx.iterable() != null -> visitIterable(ctx.iterable()!!)
            ctx.asyncIterable() != null -> visitAsyncIterable(ctx.asyncIterable()!!)
            ctx.readOnlyMember() != null -> visitReadOnlyMember(ctx.readOnlyMember()!!)
            ctx.readWriteAttribute() != null -> visitReadWriteAttribute(ctx.readWriteAttribute()!!)
            ctx.readWriteMaplike() != null -> visitReadWriteMaplike(ctx.readWriteMaplike()!!)
            ctx.readWriteSetlike() != null -> visitReadWriteSetlike(ctx.readWriteSetlike()!!)
            ctx.inheritAttribute() != null -> visitInheritAttribute(ctx.inheritAttribute()!!)
            else -> throw Exception("visitPartialInterfaceMember: ${ctx.text}")
        }
    }

    override fun visitConstructor(ctx: WebIDLParser.ConstructorContext) = IDLConstructor(collectArguments(ctx.argumentList()), sourceRangeOf(ctx))

    override fun visitConst(ctx: WebIDLParser.ConstContext): IDLConstant {
        val name = (ctx.Constant() ?: ctx.Identifier())!!.toIDLIdentifier()
        val type = visitConstType(ctx.constType())
        val constValue = ctx.constValue()
        val value = constValue.booleanLiteral()?.text
            ?: constValue.floatLiteral()?.text
            ?: constValue.Integer()?.text
            ?: throw Exception(ctx.text)

        return IDLConstant(name, type, value, sourceRangeOf(ctx))
    }

    override fun visitConstType(ctx: WebIDLParser.ConstTypeContext): IDLType {
        return when {
            ctx.primitiveType() != null -> ctx.primitiveType()!!.toIDLType()
            ctx.Identifier() != null -> IDLType.Identifier(ctx.Identifier()!!.toIDLIdentifier())
            else -> throw Exception("visitConstType: ${ctx.text}")
        }.apply { sourceRangeOf(ctx) }
    }

    override fun visitOperation(ctx: WebIDLParser.OperationContext): IDLOperation {
        return when {
            ctx.regularOperation() != null -> visitRegularOperation(ctx.regularOperation()!!)
            ctx.specialOperation() != null -> visitSpecialOperation(ctx.specialOperation()!!)
            else -> throw Exception("visitOperation: ${ctx.text}")
        }
    }

    override fun visitStringifier(ctx: WebIDLParser.StringifierContext): IDLMember {
        val rest = ctx.stringifierRest()
        when {
            rest.attributeRest() != null -> {
                val attrCtx = rest.attributeRest()!!

                val type = visitType(attrCtx.typeWithExtendedAttributes().type())
                val name = processAttributeName(attrCtx.attributeName())

                return IDLAttribute(
                    name = name,
                    type = type,
                    stringifier = true,
                    sourceRange = sourceRangeOf(ctx)
                )
            }
            else -> {
                return IDLOperation(
                    name = null,
                    returnType = IDLType.DOMString,
                    arguments = emptyList(),
                    operationType = OperationType.STRINGIFIER,
                    sourceRange = sourceRangeOf(ctx)
                )
            }
        }
    }

    override fun visitStaticMember(ctx: WebIDLParser.StaticMemberContext): IDLMember {
        val rest = ctx.staticMemberRest()

        return when {
            rest.attributeRest() != null -> visitAttributeRest(rest.attributeRest()!!).apply { static = true }
            rest.regularOperation() != null -> visitRegularOperation(rest.regularOperation()!!).apply { static = true }
            else -> throw Exception("visitStaticMember: ${ctx.text}")
        }
    }

    private fun collectArguments(ctx: WebIDLParser.ArgumentListContext?): List<IDLArgument> {
        if (ctx == null) return emptyList()
        val head = visitArgument(ctx.argument())
        val tail = collectArgumentsTail(ctx.arguments())
        return listOf(head) + tail
    }

    private fun collectArgumentsTail(ctx: WebIDLParser.ArgumentsContext?): List<IDLArgument> {
        if (ctx == null) return emptyList()
        val head = visitArgument(ctx.argument())
        val tail = collectArgumentsTail(ctx.arguments())
        return listOf(head) + tail
    }

    override fun visitArgument(ctx: WebIDLParser.ArgumentContext): IDLArgument {
        val argument = ctx.argumentRest()
        val extendedAttributes = mutableListOf<IDLExtendedAttribute>()
        collectExtendedAttributes(ctx.extendedAttributeList(), extendedAttributes)

        val type = if (argument.typeWithExtendedAttributes() != null)
            visitTypeWithExtendedAttributes(argument.typeWithExtendedAttributes()!!)
        else visitType(argument.type()!!)

        val name = processArgumentName(argument.argumentName())
        val defaultValue = argument.default()?.defaultValue()?.text

        // This feels very iffy?
        val isVariadic = argument.text.contains("...")
        val isOptional = argument.text.startsWith("optional")

        return IDLArgument(
            name = name,
            type = type,
            optional = isOptional,
            defaultValue = defaultValue,
            variadic = isVariadic,
            extendedAttributes = extendedAttributes,
            sourceRange = sourceRangeOf(ctx)
        )
    }

    override fun visitIterable(ctx: WebIDLParser.IterableContext): IDLIterable {
        val keyType = ctx.optionalType()?.let {
            visitTypeWithExtendedAttributes(it.typeWithExtendedAttributes())
        }
        val valueType = visitTypeWithExtendedAttributes(ctx.typeWithExtendedAttributes())

        return IDLIterable(keyType, valueType, sourceRangeOf(ctx))
    }

    override fun visitAsyncIterable(ctx: WebIDLParser.AsyncIterableContext): IDLAsyncIterable {
        val keyType: IDLType?
        val valueType: IDLType

        val firstType = visitTypeWithExtendedAttributes(ctx.typeWithExtendedAttributes())
        val secondType = ctx.optionalType()?.let { visitTypeWithExtendedAttributes(it.typeWithExtendedAttributes()) }

        if (secondType != null) {
            keyType = firstType
            valueType = secondType
        } else {
            keyType = null
            valueType = firstType
        }

        val args = ctx.optionalArgumentList()?.let { collectArguments(it.argumentList()) } ?: emptyList()

        return IDLAsyncIterable(
            keyType = keyType,
            valueType = valueType,
            arguments = args,
            sourceRange = sourceRangeOf(ctx)
        )
    }

    override fun visitReadOnlyMember(ctx: WebIDLParser.ReadOnlyMemberContext): IDLMember {
        val rest = ctx.readOnlyMemberRest()

        return when {
            rest.attributeRest() != null -> visitAttributeRest(rest.attributeRest()!!).apply { readonly = true }
            rest.maplikeRest() != null -> {
                val maplike = rest.maplikeRest()!!
                val types = maplike.typeWithExtendedAttributes()
                check(types.size == 2) {
                    "maplike must have exactly 2 types got ${maplike.text}"
                }
                val keyType = visitTypeWithExtendedAttributes(types[0])
                val valueType = visitTypeWithExtendedAttributes(types[1])
                IDLMaplike(keyType, valueType, readonly = true, sourceRange = sourceRangeOf(ctx))
            }
            rest.setlikeRest() != null -> {
                val elementType = visitTypeWithExtendedAttributes(rest.setlikeRest()!!.typeWithExtendedAttributes())
                IDLSetlike(elementType, readonly = true, sourceRange = sourceRangeOf(ctx))
            }
            else -> throw Exception("visitReadOnlyMember: ${ctx.text}")
        }
    }

    override fun visitReadWriteAttribute(ctx: WebIDLParser.ReadWriteAttributeContext): IDLAttribute {
        val attr = ctx.attributeRest()
        val name = processAttributeName(attr.attributeName())
        val type = visitTypeWithExtendedAttributes(attr.typeWithExtendedAttributes())
        return IDLAttribute(name, type, sourceRange = sourceRangeOf(ctx))
    }

    override fun visitInheritAttribute(ctx: WebIDLParser.InheritAttributeContext): IDLAttribute {
        val attr = ctx.attributeRest()
        val name = processAttributeName(attr.attributeName())
        val type = visitTypeWithExtendedAttributes(attr.typeWithExtendedAttributes())
        return IDLAttribute(name, type, inherit = true, sourceRange = sourceRangeOf(ctx))
    }

    override fun visitReadWriteMaplike(ctx: WebIDLParser.ReadWriteMaplikeContext): IDLMaplike {
        val types = ctx.maplikeRest().typeWithExtendedAttributes()
        check(types.size == 2) {
            "maplike must have exactly 2 types got ${ctx.maplikeRest().text}"
        }
        val keyType = visitTypeWithExtendedAttributes(types[0])
        val valueType = visitTypeWithExtendedAttributes(types[1])
        return IDLMaplike(keyType, valueType, readonly = false, sourceRange = sourceRangeOf(ctx))
    }

    override fun visitReadWriteSetlike(ctx: WebIDLParser.ReadWriteSetlikeContext): IDLSetlike {
        val elementType = visitTypeWithExtendedAttributes(ctx.setlikeRest().typeWithExtendedAttributes())
        return IDLSetlike(elementType, readonly = false, sourceRange = sourceRangeOf(ctx))
    }

    override fun visitTypeWithExtendedAttributes(ctx: WebIDLParser.TypeWithExtendedAttributesContext): IDLType {
        val type = visitType(ctx.type())
        collectExtendedAttributes(ctx.extendedAttributeList(), type.extendedAttributes)
        return type
    }

    override fun visitType(ctx: WebIDLParser.TypeContext): IDLType {
        return when {
            ctx.unionType() != null -> {
                val union = visitUnionType(ctx.unionType()!!)
                if (ctx.nullable() != null) IDLType.Nullable(union).apply { sourceRange = sourceRangeOf(ctx) } else union
            }
            ctx.singleType()?.promiseType() != null -> {
                val inner = visitType(ctx.singleType()!!.promiseType()!!.type())
                IDLType.Promise(inner).apply { sourceRange = sourceRangeOf(ctx) }
            }
            ctx.singleType()?.distinguishableType() != null -> {
                visitDistinguishableType(ctx.singleType()!!.distinguishableType()!!)
            }
            ctx.singleType()?.Any() != null -> IDLType.Any.apply { sourceRange = sourceRangeOf(ctx) }
            else -> throw Exception("visitType: ${ctx.text}")
        }
    }

    override fun visitUnionType(ctx: WebIDLParser.UnionTypeContext): IDLType {
        val types = mutableListOf<IDLType>()
        ctx.unionMemberType().forEach { memberCtx ->
            visitUnionMemberType(memberCtx)?.let { types += it }
        }
        ctx.unionMemberTypes()?.let {
            types += collectUnionMemberTypes(it)
        }
        return IDLType.Union(types).apply { sourceRange = sourceRangeOf(ctx) }
    }

    override fun visitUnionMemberType(ctx: WebIDLParser.UnionMemberTypeContext): IDLType? {
        return when {
            ctx.distinguishableType() != null -> {
                val base = visitDistinguishableType(ctx.distinguishableType()!!)
                if (ctx.nullable() != null) IDLType.Nullable(base) else base
            }
            ctx.unionType() != null -> {
                val innerUnion = visitUnionType(ctx.unionType()!!)
                if (ctx.nullable() != null) IDLType.Nullable(innerUnion) else innerUnion
            }
            else -> throw Exception(ctx.text)
        }
    }

    private fun collectUnionMemberTypes(ctx: WebIDLParser.UnionMemberTypesContext): List<IDLType> {
        val types = mutableListOf<IDLType>()
        var current: WebIDLParser.UnionMemberTypesContext? = ctx

        while (current != null) {
            visitUnionMemberType(current.unionMemberType())?.let { types += it }
            current = current.unionMemberTypes()
        }

        return types
    }

    override fun visitDistinguishableType(ctx: WebIDLParser.DistinguishableTypeContext): IDLType {
        val base: IDLType = when {
            ctx.primitiveType() != null -> ctx.primitiveType()!!.toIDLType()
            ctx.stringType() != null -> ctx.stringType()!!.toIDLType()
            ctx.Identifier() != null -> {
                IDLType.Identifier(ctx.Identifier()!!.toIDLIdentifier())
            }
            ctx.text.startsWith("sequence") -> {
                val inner = visitTypeWithExtendedAttributes(ctx.typeWithExtendedAttributes()!!)
                IDLType.Sequence(inner)
            }
            ctx.text.startsWith("async_sequence") -> {
                val inner = visitTypeWithExtendedAttributes(ctx.typeWithExtendedAttributes()!!)
                IDLType.AsyncSequence(inner)
            }
            ctx.text == "object" -> IDLType.Object
            ctx.text == "symbol" -> IDLType.Symbol
            ctx.bufferRelatedType() != null -> ctx.bufferRelatedType()!!.toIDLType()
            ctx.text.startsWith("FrozenArray") -> {
                val inner = visitTypeWithExtendedAttributes(ctx.typeWithExtendedAttributes()!!)
                IDLType.FrozenArray(inner)
            }
            ctx.text.startsWith("ObservableArray") -> {
                val inner = visitTypeWithExtendedAttributes(ctx.typeWithExtendedAttributes()!!)
                IDLType.ObservableArray(inner)
            }
            ctx.recordType() != null -> {
                val record = ctx.recordType()!!
                val keyType = record.stringType().toIDLType()
                val valueType = visitTypeWithExtendedAttributes(record.typeWithExtendedAttributes())
                IDLType.Record(keyType, valueType)
            }
            ctx.text == "undefined" -> IDLType.Undefined
            ctx.text == "any" -> IDLType.Any
            ctx.text == "void" -> IDLType.Void

            else -> throw Exception("visitDistinguishableType: ${ctx.text}")
        }

        return (if (ctx.nullable() != null) IDLType.Nullable(base) else base).apply { sourceRange = sourceRangeOf(ctx) }
    }

    private fun collectExtendedAttributes(
        ctx: WebIDLParser.ExtendedAttributeListContext?,
        accumulator: MutableList<IDLExtendedAttribute>
    ) {
        if (ctx == null) return

        for (attrCtx in ctx.extendedAttribute()) {
            val attr = visitExtendedAttribute(attrCtx)
            accumulator += attr
        }
    }

    override fun visitExtendedAttribute(ctx: WebIDLParser.ExtendedAttributeContext): IDLExtendedAttribute {
        return when {
            ctx.extendedAttributeNoArgs() != null -> {
                val name = ctx.extendedAttributeNoArgs()!!.Identifier().toIDLIdentifier()
                IDLExtendedAttribute(name, IDLExtendedAttribute.Kind.NoArgs)
            }
            ctx.extendedAttributeArgList() != null -> {
                val attr = ctx.extendedAttributeArgList()!!
                val name = attr.Identifier().toIDLIdentifier()
                val args = collectArguments(attr.argumentList())
                IDLExtendedAttribute(name, IDLExtendedAttribute.Kind.ArgList(args))
            }
            ctx.extendedAttributeNamedArgList() != null -> {
                val attr = ctx.extendedAttributeNamedArgList()!!
                val identifiers = attr.Identifier()
                check(identifiers.size == 2) { "NamedArgList failed: ${attr.text}" }
                val name = identifiers[0].toIDLIdentifier()
                val secondary = identifiers[1].toIDLIdentifier()
                val args = collectArguments(attr.argumentList())
                IDLExtendedAttribute(name, IDLExtendedAttribute.Kind.NamedArgList(secondary, args))
            }
            ctx.extendedAttributeIdent() != null -> {
                val attr = ctx.extendedAttributeIdent()!!
                val identifiers = attr.Identifier()
                if (identifiers.size != 2) {
                    throw Exception("Ident failed: ${attr.text}")
                }
                val name = identifiers[0].toIDLIdentifier()
                val value = identifiers[1].text
                IDLExtendedAttribute(name, IDLExtendedAttribute.Kind.Ident(value))
            }
            ctx.extendedAttributeString() != null -> {
                val attr = ctx.extendedAttributeString()!!
                val identifier = attr.Identifier().toIDLIdentifier()
                val string = attr.String().text
                IDLExtendedAttribute(identifier, IDLExtendedAttribute.Kind.StringArg(string))
            }
            ctx.extendedAttributeDecimal() != null -> {
                val attr = ctx.extendedAttributeDecimal()!!
                val identifier = attr.Identifier().toIDLIdentifier()
                val decimal = attr.Decimal().text.toDouble()
                IDLExtendedAttribute(identifier, IDLExtendedAttribute.Kind.Decimal(decimal))
            }
            ctx.extendedAttributeInteger() != null -> {
                val attr = ctx.extendedAttributeInteger()!!
                val identifier = attr.Identifier().toIDLIdentifier()
                val integer = attr.Integer().text.toInt()
                IDLExtendedAttribute(identifier, IDLExtendedAttribute.Kind.Integer(integer))
            }
            ctx.extendedAttributeIdentList() != null -> {
                val attr = ctx.extendedAttributeIdentList()!!
                val name = attr.Identifier().toIDLIdentifier()
                val values = collectIdentifiers(attr.identifierList())
                IDLExtendedAttribute(name, IDLExtendedAttribute.Kind.IdentList(values))
            }
            ctx.extendedAttributeIntegerList() != null -> {
                val attr = ctx.extendedAttributeIntegerList()!!
                val name = attr.Identifier().toIDLIdentifier()
                val values = collectIntegers(attr.integerList())
                IDLExtendedAttribute(name, IDLExtendedAttribute.Kind.IntegerList(values))
            }
            ctx.extendedAttributeWildcard() != null -> {
                val name = ctx.extendedAttributeWildcard()!!.Identifier().toIDLIdentifier()
                IDLExtendedAttribute(name, IDLExtendedAttribute.Kind.Wildcard)
            }
            ctx.extendedAttributeStringList() != null -> {
                val attr = ctx.extendedAttributeStringList()!!
                val name = attr.Identifier().toIDLIdentifier()
                val values = collectStrings(attr.stringList())
                IDLExtendedAttribute(name, IDLExtendedAttribute.Kind.StringList(values))
            }
            else -> throw Exception("visitExtendedAttribute: ${ctx.text}")
        }.apply { sourceRange = sourceRangeOf(ctx) }
    }

    private fun collectIdentifiers(ctx: WebIDLParser.IdentifierListContext): List<String> {
        val result = mutableListOf<String>()
        result += ctx.Identifier().text
        result += collectIdentifiers(ctx.identifiers())
        return result
    }

    private fun collectIdentifiers(ctx: WebIDLParser.IdentifiersContext?): List<String> {
        if (ctx == null) return emptyList()
        val result = mutableListOf<String>()
        result += ctx.Identifier().text
        result += collectIdentifiers(ctx.identifiers())
        return result
    }

    private fun collectIntegers(ctx: WebIDLParser.IntegerListContext): List<Int> {
        val result = mutableListOf<Int>()
        result += ctx.Integer().text.toInt()
        result += collectIntegers(ctx.integers())
        return result
    }

    private fun collectIntegers(ctx: WebIDLParser.IntegersContext?): List<Int> {
        if (ctx == null) return emptyList()
        val result = mutableListOf<Int>()
        result += ctx.Integer().text.toInt()
        result += collectIntegers(ctx.integers())
        return result
    }

    private fun collectStrings(ctx: WebIDLParser.StringListContext): List<String> {
        val result = mutableListOf<String>()
        result += ctx.String().text
        result += collectStrings(ctx.strings())
        return result
    }

    private fun collectStrings(ctx: WebIDLParser.StringsContext?): List<String> {
        if (ctx == null) return emptyList()
        val result = mutableListOf<String>()
        result += ctx.String().text
        result += collectStrings(ctx.strings())
        return result
    }
}