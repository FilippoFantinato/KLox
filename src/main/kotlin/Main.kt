import interpreter.intepret
import lexer.Scanner
import lexer.Token
import parser.Parser
import java.io.File

fun runREPL() {
    while(true)
    {
        print("> ")
        val input = readln()
        run(input)
    }
}

fun runFile(filename: String){
    run(File(filename).readText())
}

fun usage() {
    print("Usage: ")
}

fun run(source: String) {
    val scanner = Scanner(source)
    val tokens: MutableList<Token> = scanner.scanTokens() ?: return

//    tokens.forEach { token -> println(token) }

    val parser = Parser(tokens)
    val statements = parser.parse()

//    val prg = prettyProgram(statements)

    intepret(statements)
}

fun main(args: Array<String>) {
    when(args.size){
        0 -> runREPL()
        1 -> runFile(args[0])
        else -> usage()
    }
}
