package ast

import lexer.Token

fun prettyAST(ast: Expression): String {
    return when(ast) {
        is Literal -> prettyValue(ast.value)
        is Binary -> "${prettyAST(ast.left)} ${prettyToken(ast.operator)} ${prettyAST(ast.right)}"
        is Grouping -> "(${prettyAST(ast.expr)})"
        is Unary -> "${prettyToken(ast.operator)} ${prettyAST(ast.right)}"
    }
}

fun prettyToken(token: Token): String {
    return "${token.literal ?: token.lexeme}"
}

fun prettyValue(value: Any?): String {
    return "$value"
}
