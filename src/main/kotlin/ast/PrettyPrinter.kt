package ast

import lexer.Token

fun prettyAST(ast: Expression): String = when(ast) {
    is Literal -> prettyValue(ast.value)
    is Binary -> "${prettyAST(ast.left)} ${prettyToken(ast.operator)} ${prettyAST(ast.right)}"
    is Grouping -> "(${prettyAST(ast.expr)})"
    is Unary -> "${prettyToken(ast.operator)} ${prettyAST(ast.right)}"
}
fun prettyToken(token: Token) = "${token.literal ?: token.lexeme}"
fun prettyValue(value: Any?) = "$value"
