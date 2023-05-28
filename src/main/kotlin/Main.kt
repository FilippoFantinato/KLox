import ast.prettyProgram
import interpreter.environment.Environment
import interpreter.intepret
import lexer.Scanner
import lexer.Token
import parser.Parser
import java.io.File

fun runREPL() {
    val env: Environment = mutableMapOf()
    while(true)
    {
        print("> ")
        val input = readln()
        run(input, env)
    }
}

fun runFile(filename: String) {
    run(File(filename).readText())
}

fun usage() {
    print("Usage: ")
}

fun run(source: String, env: Environment = mutableMapOf()) {
    val scanner = Scanner(source)
    val tokens: MutableList<Token> = scanner.scanTokens() ?: return

    val parser = Parser(tokens)
    val statements = parser.parse()

    println(prettyProgram(statements))

    intepret(statements, env)
}

fun main(args: Array<String>) {
    when(args.size){
        0 -> runREPL()
        1 -> runFile(args[0])
        else -> usage()
    }
}
