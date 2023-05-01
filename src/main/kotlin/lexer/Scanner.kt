package lexer

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import exceptions.Errors
import java.util.LinkedList

class Scanner(private val source: String) {
    private val tokens: MutableList<Token> = LinkedList()
    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1

    private var keywords = mapOf(
        "and" to TokenType.AND,
        "class" to TokenType.CLASS,
        "else" to TokenType.ELSE,
        "false" to TokenType.FALSE,
        "fun" to TokenType.FUN,
        "for" to TokenType.FOR,
        "if" to TokenType.IF,
        "nil" to TokenType.NIL,
        "or" to TokenType.OR,
        "print" to TokenType.PRINT,
        "return" to TokenType.RETURN,
        "super" to TokenType.SUPER,
        "this" to TokenType.THIS,
        "true" to TokenType.TRUE,
        "var" to TokenType.VAR,
        "while" to TokenType.WHILE
    )

    fun scanTokens(): MutableList<Token>? {
        var notError = true
        while (notError && !isAtEnd()) {
            start = current
            notError = scanToken()
        }

        if(!notError) return null

        tokens.add(Token(TokenType.EOF, "", null, line))

        return tokens
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun scanToken(): Boolean = when(val c = advance()) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '!' -> addToken(if(match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if(match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if(match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if(match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '/' -> {
                when {
                    match('/') -> {
                        while(peek() != '\n' && !isAtEnd()) advance()
                        true
                    }
                    match('*') -> {
                        while(!(peek() == '*' || peekNext() == '\\') && !isAtEnd()) advance()
                        true
                    }
                    else -> addToken(TokenType.SLASH)
                }
            }
            '"' -> string()
            '\n' -> {
                ++line
                true
            }
            ' ', '\r', '\t'-> true
            else -> {
                when{
                    c.isDigit() -> number()
                    c.isLetter() -> identifier()
                    else -> {
                        Errors.error(line, "Unexpected character.")
                        false
                    }
                }
            }
        }

    private fun advance(): Char {
        return source[current++]
    }

    private fun addToken(type: TokenType, literal: LiteralValue? = null): Boolean {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
        return true
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd() || source[current] != expected) return false
        ++current
        return true
    }

    private fun peek(): Char {
        if(isAtEnd()) return Char.MIN_VALUE
        return source[current]
    }

    private fun peekNext(): Char {
        if(current + 1 >= source.length) return Char.MIN_VALUE
        return source[current + 1]
    }

    private fun string(): Boolean {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++
            advance()
        }

        if(isAtEnd()) {
            Errors.error(line, "Unterminated string.")
            return false
        }

        advance()

        val value = source.substring(start + 1, current - 1)
        return addToken(TokenType.STRING, value)
    }

    private fun number(): Boolean {
        while(peek().isDigit()) advance()

        if(peek() == '.' && peekNext().isDigit()) {
            advance()

            while(peek().isDigit()) advance()
        }

        return addToken(
            TokenType.NUMBER,
            source.substring(start, current).toDouble()
        )
    }

    private fun identifier(): Boolean {
        while(peek().isLetterOrDigit()) advance()

        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER
        return addToken(type)
    }
}