package ast

import lexer.Token

sealed interface Expression

class Binary(val left: Expression, val operator: Token, val right: Expression): Expression
class Grouping(val expr: Expression): Expression
class Literal(val value: Any?): Expression
class Unary(val operator: Token, val right: Expression): Expression
