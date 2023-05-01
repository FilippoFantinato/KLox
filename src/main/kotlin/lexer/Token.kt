package lexer

typealias LiteralValue = Any

class Token(val type: TokenType, val lexeme: String, val literal: LiteralValue?, val line: Int){
    override fun toString(): String {
        return "$type $lexeme $literal"
    }
}
