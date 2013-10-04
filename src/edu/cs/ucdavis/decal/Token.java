package edu.cs.ucdavis.decal;

public enum Token {

    ABSTRACT("abstract"),
    ASSERT("assert"),
    BOOLEAN("boolean"),
    BREAK("break"),
    BYTE("byte"),
    CASE("case"),
    CATCH("catch"),
    CHAR("char"),
    CLASS("class"),
    CONST("const"),
    CONTINUE("continue"),
    DEFAULT("default"),
    DO("do"),
    DOUBLE("double"),
    ELSE("else"),
    ENUM("enum"),
    EXTENDS("extends"),
    FINAL("final"),
    FINALLY("finally"),
    FLOAT("float"),
    FOR("for"),
    IF("if"),
    GOTO("goto"),
    IMPLEMENTS("implements"),
    IMPORT("import"),
    INSTANCEOF("instanceof"),
    INT("int"),
    INTERFACE("interface"),
    LONG("long"),
    NATIVE("native"),
    NEW("new"),
    PACKAGE("package"),
    PRIVATE("private"),
    PROTECTED("protected"),
    PUBLIC("public"),
    RETURN("return"),
    SHORT("short"),
    STATIC("static"),
    STRICTFP("strictfp"),
    SUPER("super"),
    SWITCH("switch"),
    SYNCHRONIZED("synchronized"),
    THIS("this"),
    THROW("throw"),
    THROWS("throws"),
    TRANSIENT("transient"),
    TRY("try"),
    VOID("void"),
    VOLATILE("volatile"),
    WHILE("while"),

    // The Null Literal
    NULL_LITERAL("null"),

    // Separators
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACK("["),
    RBRACK("]"),
    SEMI(";"),
    COMMA(","),
    DOT("."),

    // Operators
    ASSIGN("="),
    GT(">"),
    LT("<"),
    BANG("!"),
    TILDE("~"),
    QUESTION("?"),
    COLON(":"),
    EQUAL("=="),
    LE("<="),
    GE(">="),
    NOTEQUAL("!="),
    AND("&&"),
    OR("||"),
    INC("++"),
    DEC("--"),
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    BITAND("&"),
    BITOR("|"),
    CARET("^"),
    MOD("%"),

    ADD_ASSIGN("+="),
    SUB_ASSIGN("-="),
    MUL_ASSIGN("*="),
    DIV_ASSIGN("/="),
    AND_ASSIGN("&="),
    OR_ASSIGN("|="),
    XOR_ASSIGN("^="),
    MOD_ASSIGN("%="),
    LSHIFT_ASSIGN("<<="),
    RSHIFT_ASSIGN(">>="),
    URSHIFT_ASSIGN(">>>="),

    AT("@"),
    ELLIPSIS("...");

    private final String token;
    private Token(String token) {
      this.token = token;
    }

    public String getToken() { return token; }
    public String toString() { return name(); }
    public int getId() { return ordinal() + 100; }
}
