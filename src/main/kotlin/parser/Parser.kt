package parser

import ast.*
import exceptions.Errors
import exceptions.ParseError
import lexer.Token
import lexer.TokenType


class Parser(private val tokens: List<Token>)
{
    private var current = 0

    fun parse(): Expression? {
        return try { expression() } catch (error: ParseError) { null }
    }

    private fun synchronize() {
        advance()

        while(!isAtEnd()) {
            if(previous().type == TokenType.SEMICOLON) return

            when(peek().type) {
                TokenType.CLASS     -> { }
                TokenType.FUN       -> { }
                TokenType.VAR       -> { }
                TokenType.FOR       -> { }
                TokenType.IF        -> { }
                TokenType.WHILE     -> { }
                TokenType.PRINT     -> { }
                TokenType.RETURN    -> { }
                else -> { }
            }
        }

        advance()
    }

    private fun expression(): Expression {
        return equality()
    }

    // equality -> comparison ( ( "!=" | "==" ) comparison )* ;
    private fun equality(): Expression {
        var expr = comparison()

        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL))
        {
            val operator = previous()
            val right = comparison()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    // comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private fun comparison(): Expression {
        var expr: Expression = term()

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right: Expression = term()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expression {
        var expr = factor()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expression {
        var expr = unary()

        while(match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    // unary -> ( "!" | "-" ) unary | primary ;
    private fun unary(): Expression {
        return if(match(TokenType.BANG, TokenType.MINUS))
            Unary(previous(), unary())
        else
            primary()
    }

    private fun primary(): Expression {
        if(match(TokenType.FALSE)) return Literal(false)
        if(match(TokenType.TRUE)) return Literal(true)
        if(match(TokenType.NIL)) return Literal(null)
        if(match(TokenType.NUMBER, TokenType.STRING)) return Literal(previous().literal)

        if(match(TokenType.LEFT_PAREN)){
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Grouping(expr)
        }

        throw error(peek(), "Expect expression.")
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()

        throw error(peek(), message)
    }

    private fun error(token: Token, message: String): ParseError {
        Errors.error(token, message)
        return ParseError()
    }

    private fun match(vararg types: TokenType): Boolean {
        return types.any { type ->
            val checked = check(type)
            if(checked) advance()
            checked
        }
    }

    private fun check(type: TokenType): Boolean = !isAtEnd() && peek().type == type

    private fun advance(): Token {
        if(!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type == TokenType.EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }
}
