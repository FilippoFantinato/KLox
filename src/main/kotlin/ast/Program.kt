package ast

import lexer.LiteralValue
import lexer.Token

sealed interface Declaration

// Statements
sealed interface Statement : Declaration

// Expressions
sealed interface Expression : Statement
class Binary(val left: Expression, val operator: Token, val right: Expression) : Expression
class Logical(val left: Expression, val operator: Token, val right: Expression) : Expression
class Grouping(val expr: Expression) : Expression
class Literal(val value: LiteralValue?) : Expression
class Unary(val operator: Token, val right: Expression) : Expression
class Variable(val name: Token) : Expression
class Assignment(val name: Token, val value: Expression) : Expression
class Call(val callee: Expression, val paren: Token, val args: List<Expression>):
    Expression

// Print statement
class Print(val expr: Expression) : Statement

// Block statement
class Block(val declarations: List<Declaration>) : Statement

class IfThenElse(val cond: Expression, val thenBranch: Statement, val elseBranch: Statement?) : Statement
class While(val cond: Expression, val body: Statement) : Statement
class Return(val keyword: Token, val value: Expression?) : Statement

// Var declaration
class VarDeclaration(val name: Token, val init: Expression?) : Declaration

class FunDeclaration(val name: Token, val params: List<Token>, val body: Block):
        Declaration
