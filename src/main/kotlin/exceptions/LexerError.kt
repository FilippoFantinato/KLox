package exceptions

sealed class LexerError
{
    data class UnknownCharacter(val line: Int, val err: String) : LexerError()
    data class UnterminatedString(val line: Int, val err: String) : LexerError()
    data class UnterminatedNumber(val line: Int, val err: String) : LexerError()
}
