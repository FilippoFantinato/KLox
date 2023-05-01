package exceptions

import lexer.Token
class RuntimeError(val token: Token, msg: String) : RuntimeException(msg) { }
