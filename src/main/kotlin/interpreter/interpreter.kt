package interpreter

import ast.*
import exceptions.Errors
import exceptions.RuntimeError
import interpreter.environment.Environment
import interpreter.environment.VariablesMap

fun intepret(declarations: List<Declaration>, env: Environment = mutableMapOf()) {
    val newVars = VariablesMap(env)
    try {
        declarations.forEach { decl ->
            when(decl) {
                is VarDeclaration -> newVars.checkFor(decl.name.lexeme)
                else -> {}
            }

            evalDeclaration(decl, env)
        }
    } catch (e: RuntimeError) {
        Errors.error(e.token, e.message)
    }
}
