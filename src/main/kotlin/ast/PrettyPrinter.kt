package ast

import lexer.Token

fun prettyProgram(prg: List<Declaration?>) : String =
    prg.fold("") { acc, decl ->
        when(decl) {
            null -> ""
            else -> acc + prettyDeclaration(decl) + "\n"
        }
    }

private fun prettyDeclaration(decl: Declaration) : String = when(decl) {
    is VarDeclaration -> "var ${prettyToken(decl.name)}" +
            if(decl.init != null)
                " = ${prettyExpression(decl.init)};"
            else ";"
    is Statement -> prettyStatement(decl)
}

fun prettyStatement(stmt: Statement) : String {
    return when(stmt) {
        is Expression ->
            prettyExpression(stmt)
        is Block ->
            "{\n${prettyProgram(stmt.declarations)}}"
        is IfThenElse ->
            "if(${prettyExpression(stmt.cond)})\n" +
            "then \n ${prettyStatement(stmt.thenBranch)}" +
            if(stmt.elseBranch != null)
                "\nelse \n ${prettyStatement(stmt.elseBranch)}"
            else ""
        is While ->
            "while(${prettyExpression(stmt.cond)})\n" +
            prettyStatement(stmt.body)
        is Print ->
            "print ${prettyExpression(stmt.expr)};"
    }
}

fun prettyExpression(expr: Expression) : String = when(expr) {
    is Literal -> prettyValue(expr.value)
    is Variable -> prettyToken(expr.name)

    is Unary ->
        "${prettyToken(expr.operator)} ${prettyExpression(expr.right)}"
    is Binary ->
        "${prettyExpression(expr.left)} " +
        prettyToken(expr.operator) +
        " ${prettyExpression(expr.right)}"
    is Logical ->
        "${prettyExpression(expr.left)} " +
        prettyToken(expr.operator) +
        " ${prettyExpression(expr.right)}"

    is Grouping ->
        "(${prettyExpression(expr.expr)})"

    is Assignment ->
        "${prettyToken(expr.name)} = ${prettyExpression(expr.value)};"
}

fun prettyToken(token: Token) = "${token.literal ?: token.lexeme}"
fun prettyValue(value: Any?) = "$value"
