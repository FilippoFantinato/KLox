import lexer.Scanner
import lexer.Token
import java.io.File

fun runREPL() {
    while(true)
    {
        print("> ")
        val input = readLine()!!
        run(input)
    }
}

fun runFile(filename: String){
    run(File(filename).readText())
}

fun usage() {
    print("Usage: ")
}

private fun run(source: String) {
    val scanner = Scanner(source)
    val tokens: MutableList<Token> = scanner.scanTokens()

    tokens.forEach { token -> println(token) }
}

fun main(args: Array<String>) {
    if(args.isEmpty())
    {
        runREPL()
    }
    else
    {
        if(args.size == 1)
        {
            runFile(args[0])
        }
        else
        {
            usage()
        }
    }
}
