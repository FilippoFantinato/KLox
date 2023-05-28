package interpreter.environment

import lexer.LiteralValue

typealias Environment = MutableMap<String, LiteralValue?>
