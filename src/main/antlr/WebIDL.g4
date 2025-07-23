grammar WebIDL;

Interface: 'interface';
Partial: 'partial';
Mixin: 'mixin';
Callback: 'callback';
Dictionary: 'dictionary';
Typedef: 'typedef';
Includes: 'includes';
Namespace: 'namespace';
Enum: 'enum';

Attribute: 'attribute';
Const: 'const';
Readonly: 'readonly';
Static: 'static';
Required: 'required';
Optional: 'optional';
Stringifier: 'stringifier';
Getter: 'getter';
Setter: 'setter';
Deleter: 'deleter';
Constructor: 'constructor';
Inherit: 'inherit';
Or: 'or';

Maplike: 'maplike';
Setlike: 'setlike';
Sequence: 'sequence';
AsyncSequence: 'async_sequence';

Iterable: 'iterable';
AsyncIterable: 'async_iterable';

Any: 'any';
Undefined: 'undefined';
// Chromium and WebKit both still have some uses of this legacy alias of undefined
Void: 'void';
Boolean: 'boolean';
Byte: 'byte';
Octet: 'octet';
Bigint: 'bigint';
Float: 'float';
Double: 'double';
Short: 'short';
Long: 'long';
Unrestricted: 'unrestricted';
Unsigned: 'unsigned';
ByteString: 'ByteString';
DOMString: 'DOMString';
USVString: 'USVString';

ArrayBuffer: 'ArrayBuffer';
SharedArrayBuffer: 'SharedArrayBuffer';
DataView: 'DataView';
Int8Array: 'Int8Array';
Int16Array: 'Int16Array';
Int32Array: 'Int32Array';
Uint8Array: 'Uint8Array';
Uint16Array: 'Uint16Array';
Uint32Array: 'Uint32Array';
Uint8ClampedArray: 'Uint8ClampedArray';
BigInt64Array: 'BigInt64Array';
BigUint64Array: 'BigUint64Array';
Float16Array: 'Float16Array';
Float32Array: 'Float32Array';
Float64Array: 'Float64Array';

Promise: 'Promise';
Record: 'record';

LBrace: '{';
RBrace: '}';
LParen: '(';
RParen: ')';
LBracket: '[';
RBracket: ']';
Semicolon: ';';
LGeneric: '<';
RGeneric: '>';
Equal: '=';
Question: '?';
Colon: ':';
Comma: ',';
Variadic: '...';
Quote: '"';

// Ladybird includes C++ style #import declarations, just ignore them.
Import
    : '#import' ~[\r\n]* -> channel(HIDDEN)
    ;

Integer: '-'?( [1-9] [0-9]*|'0'[Xx] [0-9A-Fa-f]+|'0'[0-7]*);
Decimal: '-'?(([0-9]+'.'[0-9]*|[0-9]*'.'[0-9]+)([Ee][+-]?[0-9]+)?|[0-9]+[Ee][+-]?[0-9]+);
Constant: [_-]?[A-Z][0-9A-Z_-]*;
Identifier: [_-]?[A-Za-z][0-9A-Z_a-z-]*;
String: '"'(~["\r\n])*'"';
Whitespace: [\t\n\r ]+ -> channel(HIDDEN);
LineComment: ('//' ~ [\n]*) -> channel(HIDDEN);
BlockComment: ('/*' .*? '*/') -> channel(HIDDEN);

ErrorChar: .;

file
	: definitions? EOF
	;

definitions
    : extendedAttributeList? definition definitions?
    ;

definition
    : callbackOrInterfaceOrMixin
    | namespace
    | partial
    | dictionary
    | enum
    | typedef
    | includesStatement
    ;

argumentNameKeyword
    : Attribute
    | Callback
    | Const
    | Constructor
    | Deleter
    | Dictionary
    | Enum
    | Getter
    | Includes
    | Inherit
    | Interface
    | Iterable
    | Maplike
    | Mixin
    | Namespace
    | Partial
    | Readonly
    | Required
    | Setlike
    | Setter
    | Static
    | Stringifier
    | Typedef
    | Unrestricted
    ;

callbackOrInterfaceOrMixin
    : Callback callbackRestOrInterface
    | Interface interfaceOrMixin
    ;

interfaceOrMixin
    : interfaceRest
    | mixinRest
    ;

interfaceRest
    : Identifier inheritance? '{' interfaceMembers? '}' Semicolon
    ;

partial
    : Partial partialDefinition
    ;

partialDefinition
    : Interface partialInterfaceOrPartialMixin
    | partialDictionary
    | namespace
    ;

partialInterfaceOrPartialMixin
    : partialInterfaceRest
    | mixinRest
    ;

partialInterfaceRest
    : Identifier '{' partialInterfaceMembers? '}' Semicolon
    ;

interfaceMembers
    : extendedAttributeList? interfaceMember interfaceMembers?
    ;

interfaceMember
    : partialInterfaceMember
    | constructor
    ;

partialInterfaceMembers
    : extendedAttributeList? partialInterfaceMember partialInterfaceMembers?
    ;

partialInterfaceMember
    : const
    | operation
    | stringifier
    | staticMember
    | iterable
    | asyncIterable
    | readOnlyMember
    | readWriteAttribute
    | readWriteMaplike
    | readWriteSetlike
    | inheritAttribute
    ;

inheritance
    : ':' Identifier
    ;

mixinRest
    : Mixin Identifier '{' mixinMembers? '}' Semicolon
    ;

mixinMembers
    : extendedAttributeList? mixinMember mixinMembers?
    ;

mixinMember
    : const
    | regularOperation
    | stringifier
    | Readonly? attributeRest
    ;

includesStatement
    : Identifier Includes Identifier Semicolon
    ;

callbackRestOrInterface
    : callbackRest
    | Interface Identifier '{' callbackInterfaceMembers? '}' Semicolon
    ;

callbackInterfaceMembers
    : extendedAttributeList? callbackInterfaceMember callbackInterfaceMembers?
    ;

callbackInterfaceMember
    : const
    | regularOperation
    ;

const
    : Const constType Constant '=' constValue Semicolon
    | Const constType Identifier '=' constValue Semicolon
    ;

constValue
    : booleanLiteral
    | floatLiteral
    | Integer
    ;

booleanLiteral
    : 'true'
    | 'false'
    ;

floatLiteral
    : Decimal
    | '-Infinity'
    | 'Infinity'
    | 'NaN'
    ;

constType
    : primitiveType
    | Identifier
    ;

readOnlyMember
    : Readonly readOnlyMemberRest
    ;

readOnlyMemberRest
    : attributeRest
    | maplikeRest
    | setlikeRest
    ;

readWriteAttribute
    : attributeRest
    ;

inheritAttribute
    : 'inherit' attributeRest
    ;

attributeRest
    : Attribute typeWithExtendedAttributes attributeName Semicolon
    ;

attributeName
    : attributeNameKeyword
    | Identifier
    ;

attributeNameKeyword
    : Required
    ;

defaultValue
    : constValue
    | String
    | '[' ']'
    | '{' '}'
    | 'null'
    | Undefined
    ;

operation
    : regularOperation
    | specialOperation
    ;

regularOperation
    : type operationRest
    ;

specialOperation
    : special regularOperation
    ;

special
    : Getter
    | Setter
    | Deleter
    ;

operationRest
    : operationName? '(' argumentList? ')' Semicolon
    ;

operationName
    : operationNameKeyword
    | Identifier
    ;

operationNameKeyword
    : Includes
    ;

argumentList
    : argument arguments?
    ;

arguments
    : ',' argument arguments?
    ;

argument
    : extendedAttributeList? argumentRest
    ;

argumentRest
    : Optional typeWithExtendedAttributes argumentName default?
    | type Variadic? argumentName
    ;

argumentName
    : argumentNameKeyword
    | Identifier
    ;

constructor
    : Constructor '(' argumentList? ')' Semicolon
    ;

stringifier
    : Stringifier stringifierRest
    ;

stringifierRest
    : Readonly? attributeRest
    | Semicolon
    ;

staticMember
    : Static staticMemberRest
    ;

staticMemberRest
    : Readonly? attributeRest
    | regularOperation
    ;

iterable
    : Iterable '<' typeWithExtendedAttributes optionalType? '>' Semicolon
    ;

optionalType
    : ',' typeWithExtendedAttributes
    ;

asyncIterable
    : AsyncIterable '<' typeWithExtendedAttributes optionalType? '>' optionalArgumentList? Semicolon
    ;

optionalArgumentList
    : '(' argumentList? ')'
    ;

readWriteMaplike
    : maplikeRest
    ;

maplikeRest
    : Maplike '<' typeWithExtendedAttributes ',' typeWithExtendedAttributes '>' Semicolon
    ;

readWriteSetlike
    : setlikeRest
    ;

setlikeRest
    : Setlike '<' typeWithExtendedAttributes '>' Semicolon
    ;

namespace
    : Namespace Identifier '{' namespaceMembers? '}' Semicolon
    ;

namespaceMembers
    : extendedAttributeList? namespaceMember namespaceMembers?
    ;

namespaceMember
    : regularOperation
    | Readonly attributeRest
    | const
    ;

dictionary
    : Dictionary Identifier inheritance? '{' dictionaryMembers? '}' Semicolon
    ;

dictionaryMembers
    : dictionaryMember dictionaryMembers?
    ;

dictionaryMember
    : extendedAttributeList? dictionaryMemberRest
    ;

dictionaryMemberRest
    : Required typeWithExtendedAttributes Identifier Semicolon
    | type Identifier default? Semicolon
    ;

partialDictionary
    : Dictionary Identifier '{' dictionaryMembers? '}' Semicolon
    ;

default
    : '=' defaultValue
    ;

enum
    : Enum Identifier '{' enumValueList '}' Semicolon
    ;

enumValueList
    : String enumValueListComma?
    ;

enumValueListComma
    : ',' enumValueListString?
    ;

enumValueListString
    : String enumValueListComma?
    ;

callbackRest
    : Identifier '=' type '(' argumentList? ')' Semicolon
    ;

typedef
    : Typedef typeWithExtendedAttributes Identifier Semicolon
    ;

type
    : singleType
    | unionType nullable?
    ;

typeWithExtendedAttributes
    : extendedAttributeList? type
    ;

singleType
    : distinguishableType
    | Any
    | promiseType
    ;

unionType
    : '(' unionMemberType Or unionMemberType unionMemberTypes? ')'
    ;

unionMemberType
    : extendedAttributeList? distinguishableType
    | unionType nullable?
    ;

unionMemberTypes
    : Or unionMemberType unionMemberTypes?
    ;

distinguishableType
    : primitiveType nullable?
    | stringType nullable?
    | Identifier nullable?
    | Sequence '<' typeWithExtendedAttributes '>' nullable?
    | AsyncSequence '<' typeWithExtendedAttributes '>' nullable?
    | 'object' nullable?
    | 'symbol' nullable?
    | bufferRelatedType nullable?
    | 'FrozenArray' '<' typeWithExtendedAttributes '>' nullable?
    | 'ObservableArray' '<' typeWithExtendedAttributes '>' nullable?
    | recordType nullable?
    | Undefined nullable?
    | Void nullable?
    ;

primitiveType
    : unsignedIntegerType
    | unrestrictedFloatType
    | Boolean
    | Byte
    | Octet
    | Bigint
    ;

unrestrictedFloatType
    : Unrestricted floatType
    | floatType
    ;

floatType
    : Float
    | Double
    ;

unsignedIntegerType
    : Unsigned integerType
    | integerType
    ;

integerType
    : Short
    | Long Long?
    ;

stringType
    : ByteString
    | DOMString
    | USVString
    ;

promiseType
    : Promise '<' type '>'
    ;

recordType
    : Record '<' stringType ',' typeWithExtendedAttributes '>'
    ;

nullable // null in spec
    : '?'
    ;

bufferRelatedType
    : ArrayBuffer
    | SharedArrayBuffer
    | DataView
    | Int8Array
    | Int16Array
    | Int32Array
    | Uint8Array
    | Uint16Array
    | Uint32Array
    | Uint8ClampedArray
    | BigInt64Array
    | BigUint64Array
    | Float16Array
    | Float32Array
    | Float64Array
    ;

extendedAttributeList
    : '[' extendedAttribute (',' extendedAttribute)* ']'
    ;

extendedAttribute
    : extendedAttributeNoArgs
    | extendedAttributeArgList
    | extendedAttributeNamedArgList
    | extendedAttributeString
    | extendedAttributeIdent
    | extendedAttributeInteger
    | extendedAttributeDecimal
    | extendedAttributeIdentList
    | extendedAttributeIntegerList
    | extendedAttributeWildcard
    | extendedAttributeStringList
    ;


extendedAttributeNoArgs
    : Identifier
    ;

extendedAttributeArgList
    : Identifier '(' argumentList? ')'
    ;

extendedAttributeIdent
    : Identifier '=' Identifier
    ;

extendedAttributeString
    : Identifier '=' String
    ;

extendedAttributeInteger
    : Identifier '=' Integer
    ;

extendedAttributeDecimal
    : Identifier '=' Decimal
    ;

extendedAttributeWildcard
    : Identifier '=' '*'
    ;

extendedAttributeIntegerList
    : Identifier '=' '(' integerList ')'
    ;

extendedAttributeIdentList
    : Identifier '=' '(' identifierList ')'
    ;

extendedAttributeNamedArgList
    : Identifier '=' Identifier '(' argumentList? ')'
    ;

// Non-standard but used by Chromium
extendedAttributeStringList
    : Identifier '=' '(' stringList ')'
    ;

identifierList
    : Identifier identifiers?
    ;

identifiers
    : ',' Identifier identifiers?
    ;

integerList
    : Integer integers?
    ;

integers
    : ',' Integer integers?
    ;

stringList
    : String strings?
    ;

strings
    : ',' String strings?
    ;

// Permissive syntax allowed by main spec
//extendedAttributeList
//    : '[' extendedAttribute extendedAttributes? ']'
//    ;
//
//extendedAttributes
//    : ',' extendedAttribute extendedAttributes?
//    ;
//
//extendedAttribute
//    : '(' extendedAttributeInner? ')' extendedAttributeRest?
//    | '[' extendedAttributeInner? ']' extendedAttributeRest?
//    | '{' extendedAttributeInner? '}' extendedAttributeRest?
//    | other extendedAttributeRest?
//    ;
//
//extendedAttributeRest
//    : extendedAttribute
//    ;
//
//extendedAttributeInner
//    : '(' extendedAttributeInner? ')' extendedAttributeInner?
//    | '[' extendedAttributeInner? ']' extendedAttributeInner?
//    | '{' extendedAttributeInner? '}' extendedAttributeInner?
//    | otherOrComma extendedAttributeInner?
//    ;
//
//other
//    : Integer
//    | Decimal
//    | Identifier
//    | String
//    | Other
//    | '-'
//    | '-Infinity'
//    | '.'
//    | Variadic
//    | ':'
//    | Semicolon
//    | '<'
//    | '='
//    | '>'
//    | '?'
//    | '*'
//    | 'ByteString'
//    | 'DOMString'
//    | 'FrozenArray'
//    | 'Infinity'
//    | 'NaN'
//    | 'ObservableArray'
//    | 'Promise'
//    | 'USVString'
//    | 'any'
//    | 'bigint'
//    | 'boolean'
//    | 'byte'
//    | 'double'
//    | 'false'
//    | 'float'
//    | 'long'
//    | 'null'
//    | 'object'
//    | 'octet'
//    | 'or'
//    | 'optional'
//    | 'record'
//    | 'sequence'
//    | 'short'
//    | 'symbol'
//    | 'true'
//    | 'unsigned'
//    | 'undefined'
//    | argumentNameKeyword
//    | bufferRelatedType
//    ;
//
//otherOrComma
//    : other
//    | ','
//    ;