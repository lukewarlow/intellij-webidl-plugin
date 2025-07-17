package dev.lukewarlow.intellijwebidlplugin

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

class WebIDLColorSettingsPage : ColorSettingsPage {

    override fun getDisplayName(): String = "Web IDL"

    override fun getIcon(): Icon? = WebIDLIcons.LOGO

    override fun getHighlighter(): SyntaxHighlighter = WebIDLSyntaxHighlighter()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? =
        mapOf(
            "keyword" to WebIDLSyntaxHighlightingColors.KEYWORD,
            "type" to WebIDLSyntaxHighlightingColors.TYPE,
            "string" to WebIDLSyntaxHighlightingColors.STRING,
            "number" to WebIDLSyntaxHighlightingColors.NUMBER,
            "line_comment" to WebIDLSyntaxHighlightingColors.LINE_COMMENT,
            "block_comment" to WebIDLSyntaxHighlightingColors.BLOCK_COMMENT,
            "identifier" to WebIDLSyntaxHighlightingColors.IDENTIFIER,
            "braces" to WebIDLSyntaxHighlightingColors.BRACES,
            "brackets" to WebIDLSyntaxHighlightingColors.BRACKETS,
            "parentheses" to WebIDLSyntaxHighlightingColors.PARENTHESES,
            "semicolon" to WebIDLSyntaxHighlightingColors.SEMICOLON,
            "operator_sign" to WebIDLSyntaxHighlightingColors.OPERATORS,
            "comma" to WebIDLSyntaxHighlightingColors.COMMA,
            "constant" to WebIDLSyntaxHighlightingColors.CONSTANT,
        )

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = arrayOf(
        AttributesDescriptor("Braces and Operators//Braces", WebIDLSyntaxHighlightingColors.BRACES),
        AttributesDescriptor("Braces and Operators//Brackets", WebIDLSyntaxHighlightingColors.BRACKETS),
        AttributesDescriptor("Braces and Operators//Colon", WebIDLSyntaxHighlightingColors.COLON),
        AttributesDescriptor("Braces and Operators//Comma", WebIDLSyntaxHighlightingColors.COMMA),
        AttributesDescriptor("Braces and Operators//Parentheses", WebIDLSyntaxHighlightingColors.PARENTHESES),
        AttributesDescriptor("Braces and Operators//Semicolon", WebIDLSyntaxHighlightingColors.SEMICOLON),
        AttributesDescriptor("Braces and Operators//Operator sign", WebIDLSyntaxHighlightingColors.OPERATORS),
        AttributesDescriptor("Comments//Block comment", WebIDLSyntaxHighlightingColors.BLOCK_COMMENT),
        AttributesDescriptor("Comments//Line comment", WebIDLSyntaxHighlightingColors.LINE_COMMENT),
        AttributesDescriptor("Identifiers//Constant", WebIDLSyntaxHighlightingColors.CONSTANT),
        AttributesDescriptor("Identifiers//Default", WebIDLSyntaxHighlightingColors.IDENTIFIER),
        AttributesDescriptor("Keyword", WebIDLSyntaxHighlightingColors.KEYWORD),
        AttributesDescriptor("Number", WebIDLSyntaxHighlightingColors.NUMBER),
        AttributesDescriptor("String", WebIDLSyntaxHighlightingColors.STRING),
        AttributesDescriptor("Type", WebIDLSyntaxHighlightingColors.TYPE),
    )

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDemoText(): @NonNls String = """
// Extended attributes
[SecureContext, Exposed=Window]
interface TestInterface {
	const unsigned short CONSTANT = 1;
	const double CONSTANT_DOUBLE = 1.0;
	readonly attribute DOMString name;
	[Reflect, ReflectDefault=2] attribute long value;
	getter DOMString getThingy();
	setter undefined setThingy(DOMString identifier);
	deleter undefined deleteThingy(DOMString identifier);
	getter DOMString ();
	setter undefined (DOMString identifier);
	deleter undefined (DOMString identifier);

	undefined doSomething();
	boolean check([AllowShared] BufferSource buffer);
	sequence<DOMString> doTheThing();

	stringifier attribute DOMString stringy;
	stringifier;

	maplike<DOMString, long>;
	setlike<DOMString>;

	constructor(DOMString name);
	static long staticMethod();
	static attribute boolean enabled;
};

/*
    Block comment
*/

// Partial interface
partial interface TestInterface {
	undefined extended();
};

// Callback interface
callback interface CallbackInterface {
	undefined callbackMethod();
};

// Dictionary
dictionary TestDictionary {
	required DOMString id;
	long count = 0;
	boolean? optionalFlag;
};

// Partial dictionary
partial dictionary TestDictionary {
	double extraValue;
};

// Namespace
namespace Utils {
	undefined log(DOMString message);
	readonly attribute boolean enabled;
};

// Includes
TestInterface includes MixinInterface;

// Mixin
interface mixin MixinInterface {
	undefined mix();
};

// Typedefs
typedef unsigned long long DOMHighResTimeStamp;

// Enums
[EmptyValueDefault="up"]
enum Direction {
	"up",
	"down",
	"left",
	"right"
};

// Callback
callback ComparisonCallback = boolean (DOMString a, DOMString b);

// Union types
interface UnionExample {
	attribute (DOMString or long)? unionAttr;
	undefined acceptUnion((ArrayBuffer or DOMString) input);
};

// Nullable and optional types
interface Optionals {
	attribute DOMString? maybeString;
	undefined acceptOptional(optional long count);
};

// Variadic arguments
interface VariadicExample {
	undefined acceptMany(DOMString... args);
};

interface AsyncSequenceExample {
	attribute async_sequence<DOMString> foo;
};

// Inheritance
interface ChildInterface : TestInterface {
	undefined childMethod();
};
    """.trimIndent()
}