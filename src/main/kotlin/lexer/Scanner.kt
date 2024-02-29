package lexer

import arrow.core.Either
import arrow.core.Either.*
import errors.LexerError
import errors.LexerError.*

typealias Tokens = List<Token>

fun lex(source: String) =
    lex(source, 0, 0, 1, listOf())

private fun lex(source: String, start: Int, current: Int, line: Int, tokens: Tokens) : Either<LexerError, Tokens>
{
    if(isAtEnd(source, current)) return Right(tokens + Token(TokenType.EOF, "", null, line))

    val current = current + 1
    return when(val c = source[current-1])
    {
        '(' -> lex(source, current, current, line,
            tokens + addToken(TokenType.LEFT_PAREN, source, start, current, line)
        )
        ')' -> lex(source, current, current, line,
            tokens + addToken(TokenType.RIGHT_PAREN, source, start, current, line)
        )
        '{' -> lex(source, current, current, line,
            tokens + addToken(TokenType.LEFT_BRACE, source, start, current, line)
        )
        '}' -> lex(source, current, current, line,
            tokens + addToken(TokenType.RIGHT_BRACE, source, start, current, line)
        )
        ',' -> lex(source, current, current, line,
            tokens + addToken(TokenType.COMMA, source, start, current, line)
        )
        '.' -> lex(source, current, current, line,
            tokens + addToken(TokenType.DOT, source, start, current, line)
        )
        '-' -> lex(source, current, current, line,
            tokens + addToken(TokenType.MINUS, source, start, current, line)
        )
        '+' -> lex(source, current, current, line,
            tokens + addToken(TokenType.PLUS, source, start, current, line)
        )
        ';' -> lex(source, current, current, line,
            tokens + addToken(TokenType.SEMICOLON, source, start, current, line)
        )
        '*' -> lex(source, current, current, line,
            tokens + addToken(TokenType.STAR, source, start, current, line)
        )
        '!' -> {
            if(match('=', source, current))
            {
                val current = current + 1
                val t = addToken(TokenType.BANG_EQUAL, source, start, current, line)
                lex(source, current, current, line, tokens + t)
            }
            else
            {
                val t = addToken(TokenType.BANG, source, start, current, line)
                lex(source, current, current, line, tokens + t)
            }
        }

        '=' -> {
            if(match('=', source, current))
            {
                val current = current + 1
                val t = addToken(TokenType.EQUAL_EQUAL, source, start, current, line)
                lex(source, current, current, line, tokens + t)
            }
            else
            {
                val t = addToken(TokenType.EQUAL, source, start, current, line)
                lex(source, current, current, line, tokens + t)
            }
        }
        '<' -> {
            if(match('=', source, current))
            {
                val current = current + 1
                val t = addToken(TokenType.LESS_EQUAL, source, start, current, line)
                lex(source, current, current, line, tokens + t)
            }
            else
            {
                val t = addToken(TokenType.LESS, source, start, current, line)
                lex(source, current, current, line, tokens + t)
            }
        }
        '>' -> {
            if(match('=', source, current))
            {
                val current = current + 1
                val t = addToken(TokenType.GREATER_EQUAL, source, start, current, line)
                lex(source, current, current, line, tokens + t)
            }
            else
            {
                val t = addToken(TokenType.GREATER, source, start, current, line)
                lex(source, current, current, line, tokens + t)
            }
        }
        '/' -> {
            when {
                match('/', source, current) -> {
                    val current = inlineComment(source, current + 1)
                    lex(source, current, current, line+1, tokens)
                }

                match('*', source, current) -> {
                    val (current, line) = multiBlockComment(source, current, line)
                    lex(source, current, current, line, tokens)
                }
                else -> {
                    val t = addToken(TokenType.SLASH, source, start, current, line)
                    lex(source, current, current, line, tokens + t)
                }
            }
        }
        '"' -> {
            when(val s = string(source, start, current, line)) {
                is Left -> Left(s.value)
                is Right -> {
                    val (current, line) = s.value
                    val t = addToken(TokenType.STRING, source, start, current, line, source.substring(start + 1, current - 1))
                    lex(source, current, current, line, tokens + t)
                }
            }
        }
        '\n' -> lex(source, current, current, line + 1, tokens)
        ' ', '\r', '\t'-> lex(source, current, current, line, tokens)
        else -> {
            when{
                c.isDigit() -> {
                    when(val s = number(source, start, current, line)) {
                        is Left -> Left(s.value)
                        is Right -> {
                            val current = s.value
                            val t = addToken(TokenType.NUMBER, source, start, current, line, source.substring(start, current).toDouble())
                            lex(source, current, current, line, tokens + t)
                        }
                    }
                }
                c.isLetter() -> {
                    val current = identifier(source, current)
                    val text = source.substring(start, current)
                    val type = keywords[text] ?: TokenType.IDENTIFIER
                    val t = addToken(type, source, start, current, line)

                    lex(source, current, current, line, tokens + t)
                }
                else -> {
                    Left(UnknownCharacter(line, source.substring(start, current)))
                }
            }
        }
    }
}

private fun addToken(type: TokenType, source: String, start: Int, current: Int, line: Int, literal: LiteralValue? = null): Token =
    Token(type, source.substring(start, current), literal, line)

private fun isAtEnd(source: String, current: Int) =
    current >= source.length

private fun match(expected: Char, source: String, current: Int) =
    !isAtEnd(source, current) && source[current] == expected

private fun peek(source: String, current: Int): Char =
    if(isAtEnd(source, current)) Char.MIN_VALUE else source[current]

private fun inlineComment(source: String, current: Int) : Int =
    when {
        isAtEnd(source, current) || source[current] == '\n' -> current + 1
        else -> inlineComment(source, current + 1)
    }

private fun multiBlockComment(source: String, current: Int, line: Int) : Pair<Int, Int> =
    when {
        isAtEnd(source, current) ||
        (source[current] == '*' && (isAtEnd(source, current+1) || source[current + 1] == '/')) ->
            Pair(current + 2, line)
        else -> multiBlockComment(source, current + 1, if(source[current] == '\n') line + 1 else line)
    }

private fun string(source: String, start: Int, current: Int, line: Int): Either<LexerError, Pair<Int, Int>> =
    when {
        !isAtEnd(source, current) && source[current] == '"' -> Right(Pair(current + 1, line))
        !isAtEnd(source, current) && source[current] != '"' ->
            string(source, start, current + 1, if(source[current]=='\n') line + 1 else line)
        else -> Left(UnterminatedString(line, source.substring(start, current)))
    }

private fun number(source: String, start: Int, current: Int, line: Int) : Either<LexerError, Int> =
        when(peek(source, current)) {
            in '0'..'9' -> number(source, start, current, line)
            '.' -> when {
                peek(source, current + 1) in '0'..'9' -> number(source, start, current + 2, line)
                else -> Left(UnterminatedNumber(line, source.substring(start, current + 1)))
            }
            else -> Right(current)
        }

private fun identifier(source: String, current: Int): Int =
    when {
        peek(source, current).isLetterOrDigit() -> identifier(source, current + 1)
        else -> current
    }
