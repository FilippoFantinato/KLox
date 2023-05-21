package parser

import ast.*
import exceptions.Errors
import exceptions.ParseError
import lexer.Token
import lexer.TokenType


class Parser(private val tokens: List<Token>)
{
    private var current = 0

    fun parse(): List<Declaration?> {
        val program = mutableListOf<Declaration?>()

        while(!isAtEnd()) program.add(declaration())

        return program
    }

    private fun declaration(): Declaration? {
        return try {
            when {
                match(TokenType.VAR) ->
                    varDeclaration()
                else ->
                    statement()
            }
        } catch (error: ParseError) {
            synchronize()
            null
        }
    }

    private fun varDeclaration(): VarDeclaration {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name")
        val initializer = if(match(TokenType.EQUAL)) expression() else null

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")

        return VarDeclaration(name, initializer)
    }

    private fun statement() = when {
        match(TokenType.PRINT) ->
            printStatement()
        match(TokenType.LEFT_BRACE) ->
            Block(blockStatement())
        match(TokenType.IF) ->
            ifStatement()
        match(TokenType.WHILE) ->
            whileStatement()
        match(TokenType.FOR) ->
            forLoopStatement()
        else -> expressionStatement()
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

    private fun printStatement() : Statement {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value")
        return Print(value)
    }

    private fun blockStatement() : List<Declaration> {
        val declarations = mutableListOf<Declaration>()

        while(!isAtEnd() && !check(TokenType.RIGHT_BRACE))
        {
            declarations.add(declaration()!!)
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block")
        return declarations
    }

    private fun ifStatement() : Statement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        val elseBranch = if (match(TokenType.ELSE)) statement() else null

        return IfThenElse(condition, thenBranch, elseBranch)
    }

    private fun whileStatement() : Statement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()

        return While(condition, body)
    }

    private fun forLoopStatement() : Statement {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")

        val initializer = when {
            match(TokenType.SEMICOLON) -> null
            match(TokenType.VAR) -> varDeclaration()
            else -> expressionStatement()
        }
        var condition = if(!check(TokenType.SEMICOLON)) expression() else null
        var increment = if(!check(TokenType.RIGHT_PAREN)) expression() else null

        consume(TokenType.RIGHT_PAREN, "Expect ')' after 'for'.")

        var body = statement()

        if(increment != null) {
            body = Block(listOf(body, increment))
        }

        if(condition == null) {
            condition = Literal(true)
        }

        body = While(condition, body)

        if(initializer != null) {
            body = Block(listOf(initializer, body))
        }

        return body
    }

    private fun expressionStatement() : Statement {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value")
        return expr
    }

    private fun expression(): Expression {
        return assignment()
    }

    private fun assignment(): Expression {
        val expr = or()

        if(match(TokenType.EQUAL)) {
            val equals = previous()
            val value  = assignment()

            if(expr is Variable) {
                return Assignment(expr.name, value)
            }

            Errors.error(equals, "Invalid assignment target.")
        }

        return expr
    }

    private fun or(): Expression {
        var expr = and()

        while(match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Binary(expr, operator, right)
        }

        return expr
    }

    private fun and(): Expression {
        var expr = equality()

        while(match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Binary(expr, operator, right)
        }

        return expr
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

    private fun primary(): Expression = when {
        match(TokenType.FALSE) ->
            Literal(false)
        match(TokenType.TRUE) ->
            Literal(true)
        match(TokenType.NIL) ->
            Literal(null)
        match(TokenType.NUMBER, TokenType.STRING) ->
            Literal(previous().literal)
        match(TokenType.IDENTIFIER) ->
            Variable(previous())
        match(TokenType.LEFT_PAREN) -> {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            Grouping(expr)
        }
        else -> throw error(peek(), "Expect expression.")
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
