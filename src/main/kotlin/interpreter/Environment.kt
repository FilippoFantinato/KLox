package interpreter

import lexer.LiteralValue

typealias Environment = MutableMap<String, LiteralValue?>
