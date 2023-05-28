package interpreter

import lexer.LiteralValue

class ReturnValue(val value: LiteralValue?) : Throwable()
