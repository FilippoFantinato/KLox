package interpreter

import ast.*
import exceptions.RuntimeError
import interpreter.callable.LoxFunction
import interpreter.environment.Environment

internal fun evalDeclaration(decl: Declaration, env: Environment) {
    when(decl) {
        is Statement -> evalStatement(decl, env)
        is VarDeclaration -> {
            env[decl.name.lexeme] = null
            if (decl.init != null) {
                evalExpression(Assignment(decl.name, decl.init), env)
            }
        }

        is FunDeclaration -> {
            val f = LoxFunction(decl)
            env[decl.name.lexeme] = f
        }
    }
}
