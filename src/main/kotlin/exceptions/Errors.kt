package exceptions

import lexer.Token
import lexer.TokenType

class Errors {
    companion object {
        fun error(line: Int, message: String) {
            report(line, "", message)
        }

        fun error(token: Token, message: String?) {
            if (token.type === TokenType.EOF) {
                report(token.line, " at end", message!!)
            } else {
                report(token.line, " at '" + token.lexeme + "'", message!!)
            }
        }

        private fun report(line: Int, where: String, message: String) {
            println("[lind: $line] Error $where: $message]")
        }
    }
}
