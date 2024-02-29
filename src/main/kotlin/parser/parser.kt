package parser

import arrow.core.fold
import ast.Declaration
import lexer.TokenType
import lexer.Tokens

typealias Declarations = List<Declaration>

fun parse(tokens: Tokens) : Declarations =
    tokens.fold(listOf()) { acc, token ->
        acc + declaration(token)
    }

private fun isAtEnd(tokens: Tokens, i: Int) = i >= tokens.size || tokens[i].type == TokenType.EOF

private fun check(tokens: Tokens, i: Inttype: TokenType): Boolean =
    !isAtEnd(tokens, i) && peek().type == type

private fun match(vararg types: TokenType) =
    types.any { type ->
        val checked = check(type)
        if(checked) advance()
        checked
    }

private fun declaration() = when {
    match(TokenType.VAR) -> varDeclaration()
    match(TokenType.FUN) -> funDeclaration("function")
    else -> statement()
}

class Parser(private val tokens: List<Token>)
{
    private var current = 0

    fun parse() : List<Declaration> {
        val program = mutableListOf<Declaration>()
        while(!isAtEnd()) program.add(declaration())
        return program
    }



    private fun varDeclaration() : Declaration {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name")
        val initializer = if(match(TokenType.EQUAL)) expression() else null

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")

        return VarDeclaration(name, initializer)
    }

    private fun funDeclaration(kind: String) : Declaration {
        val name = consume(TokenType.IDENTIFIER, "Expect $kind + name.")
        consume(TokenType.LEFT_PAREN, "Expect '(' after $kind name.")
        val parameters = mutableListOf<Token>()
        if(!check(TokenType.RIGHT_PAREN))
        {
            do{
                if(parameters.size >= 255)
                {
                    error(peek(), "Can't have more than 255 parameters.")
                }

                parameters.add(
                    consume(TokenType.IDENTIFIER, "Expect parameter name.")
                )
            }while(match(TokenType.COMMA))
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")

        consume(TokenType.LEFT_BRACE, "Expect '{ before $kind body.")

        val body = blockStatement();

        return FunDeclaration(name, parameters, body)
    }

    private fun statement(): Statement = when {
        match(TokenType.IF)         -> ifStatement()
        match(TokenType.FOR)        -> forLoopStatement()
        match(TokenType.WHILE)      -> whileStatement()
        match(TokenType.PRINT)      -> printStatement()
        match(TokenType.LEFT_BRACE) -> blockStatement()
        match(TokenType.RETURN)     -> returnStatement()
        else -> expressionStatement()
    }

    private fun printStatement() : Statement {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value")
        return Print(value)
    }

    private fun blockStatement() : Block {
        val declarations = mutableListOf<Declaration>()

        while(!isAtEnd() && !check(TokenType.RIGHT_BRACE))
        {
            declarations.add(declaration())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block")
        return Block(declarations)
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
            match(TokenType.VAR) -> varDeclaration()
            match(TokenType.SEMICOLON) -> null
            else -> expressionStatement()
        }
        val condition = if(!check(TokenType.SEMICOLON)) expression() else Literal(true)

        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")

        val increment = if(!check(TokenType.RIGHT_PAREN)) expression() else null

        consume(TokenType.RIGHT_PAREN, "Expect ')' after 'for'.")

        val body = statement() . let { body ->
            (increment ?. let { inc -> Block(listOf(body, inc)) } ?: body) . let { body ->
                While(condition, body) . let { body ->
                    initializer ?. let {Block(listOf(initializer, body))} ?: body
                }
            }
        }

        return body
    }

    private fun returnStatement() : Return {
        val keyword = previous()
        val value: Expression? = if(!check(TokenType.SEMICOLON)) expression() else null

        consume(TokenType.SEMICOLON, "Expect ';' after return value.")
        return Return(keyword, value)
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
            call()
    }

    private fun call(): Expression {
        var expr = primary()

        while(true) {
            if(match(TokenType.LEFT_PAREN))
            {
                expr = finishCall(expr)
            } else {
                break;
            }
        }

        return expr
    }

    private fun finishCall(callee: Expression) : Expression {
        val arguments = mutableListOf<Expression>()

        if(!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size >= 255) {
                    error(peek(), "Can't have more than 255 arguments")
                }
                arguments.add(expression())
            } while(match(TokenType.COMMA));
        }

        val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")

        return Call(callee, paren, arguments)
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


    private fun advance(): Token {
        if(!isAtEnd()) current++
        return previous()
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }
}
