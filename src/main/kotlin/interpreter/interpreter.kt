package interpreter

import ast.*
import exceptions.Errors
import exceptions.RuntimeError
import interpreter.callable.LoxCallable
import interpreter.callable.LoxFunction
import interpreter.callable.arity
import interpreter.environment.Environment
import interpreter.environment.VariablesMap
import lexer.LiteralValue
import lexer.Token
import lexer.TokenType

fun intepret(declarations: List<Declaration>, env: Environment = mutableMapOf()) {
    val newVars = VariablesMap(env)
    try {
        declarations.forEach { decl ->
            if(decl is VarDeclaration)
            {
                newVars.checkFor(decl.name.lexeme)
            }

            evalDeclaration(decl, env)
        }
    } catch (e: RuntimeError) {
        Errors.error(e.token, e.message)
    }
}
