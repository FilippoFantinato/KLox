package lexer

import arrow.core.Either.Left
import arrow.core.Either.Right
import exceptions.LexerError
import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

internal class ScannerTest
{
    @Test
    fun `scanTokens returns a list of tokens when the input string is valid`()
    {
        val source = """
and class else false fun for if nil or print return super this true var while {} () // comment
/* 
block comment 
*/
, . - + ; * ! = != == < > / <= >= "Hello world!" 5.7 9 variable var /*
""".trimIndent()
        val expected = Right(
            listOf(
                Token(TokenType.AND, "and", null, 1),
                Token(TokenType.CLASS, "class", null, 1),
                Token(TokenType.ELSE, "else", null, 1),
                Token(TokenType.FALSE, "false", null, 1),
                Token(TokenType.FUN, "fun", null, 1),
                Token(TokenType.FOR, "for", null, 1),
                Token(TokenType.IF, "if", null, 1),
                Token(TokenType.NIL, "nil", null, 1),
                Token(TokenType.OR, "or", null, 1),
                Token(TokenType.PRINT, "print", null, 1),
                Token(TokenType.RETURN, "return", null, 1),
                Token(TokenType.SUPER, "super", null, 1),
                Token(TokenType.THIS, "this", null, 1),
                Token(TokenType.TRUE, "true", null, 1),
                Token(TokenType.VAR, "var", null, 1),
                Token(TokenType.WHILE, "while", null, 1),
                Token(TokenType.LEFT_BRACE, "{", null, 1),
                Token(TokenType.RIGHT_BRACE, "}", null, 1),
                Token(TokenType.LEFT_PAREN, "(", null, 1),
                Token(TokenType.RIGHT_PAREN, ")", null, 1),
                Token(TokenType.COMMA, ",", null, 5),
                Token(TokenType.DOT, ".", null, 5),
                Token(TokenType.MINUS, "-", null, 5),
                Token(TokenType.PLUS, "+", null, 5),
                Token(TokenType.SEMICOLON, ";", null, 5),
                Token(TokenType.STAR, "*", null, 5),
                Token(TokenType.BANG, "!", null, 5),
                Token(TokenType.EQUAL, "=", null, 5),
                Token(TokenType.BANG_EQUAL, "!=", null, 5),
                Token(TokenType.EQUAL_EQUAL, "==", null, 5),
                Token(TokenType.LESS, "<", null, 5),
                Token(TokenType.GREATER, ">", null, 5),
                Token(TokenType.SLASH, "/", null, 5),
                Token(TokenType.LESS_EQUAL, "<=", null, 5),
                Token(TokenType.GREATER_EQUAL, ">=", null, 5),
                Token(TokenType.STRING, "\"Hello world!\"", "Hello world!", 5),
                Token(TokenType.NUMBER, "5.7", 5.7, 5),
                Token(TokenType.NUMBER, "9", 9.0, 5),
                Token(TokenType.IDENTIFIER, "variable", null, 5),
                Token(TokenType.VAR, "var", null, 5),
                Token(TokenType.EOF, "", null, 5)
            )
        )
        val actual = lex(source)

        assertEquals(expected, actual)
    }

    @Test
    fun `scanTokens returns an error when there is an unknown character`()
    {
        val source = "var c = false; if(c && !c){}"
        val expected = Left(LexerError.UnknownCharacter(1, "&"))
        val actual = lex(source)

        assertEquals(expected, actual)
    }

    @Test
    fun `scanTokens returns an error when there is an unterminated string`()
    {
        val source = "var c = \"Hello World!;"
        val expected = Left(LexerError.UnterminatedString(1, "\"Hello World!;"))
        val actual = lex(source)

        assertEquals(expected, actual)
    }

    @Test
    fun `scanTokens returns an error when there is an unterminated number`()
    {
        val source = "var c = 5.;"
        val expected = Left(LexerError.UnterminatedNumber(1, "5."))
        val actual = lex(source)

        assertEquals(expected, actual)
    }
}
