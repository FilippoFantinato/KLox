import arrow.core.Either
import interpreter.environment.Environment
import lexer.lex
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
    val tokens = lex(source)
    when(tokens)
    {
        is Either.Right -> print(tokens.value)
        is Either.Left -> print(tokens.value)
    }

//    val parser = Parser(tokens)
//    val statements = parser.parse()
//
//    println(prettyProgram(statements))
//
//    interpret(statements, env)
}

fun main(args: Array<String>) =
    when(args.size){
        0 -> runREPL()
        1 -> runFile(args[0])
        else -> usage()
    }
