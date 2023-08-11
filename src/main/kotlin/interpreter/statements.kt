package interpreter

import ast.*
import interpreter.environment.Environment
import interpreter.environment.VariablesMap

internal fun evalStatement(stmt: Statement, env: Environment) {
    when(stmt) {
        is Print -> {
            val value = evalExpression(stmt.expr, env)
            println(value)
        }
        is Expression -> {
            evalExpression(stmt, env)
        }
        is Block -> evalBlock(stmt, env)
        is IfThenElse -> {
            when(isTruthy(evalExpression(stmt.cond, env))) {
                true -> evalStatement(stmt.thenBranch, env)
                false -> stmt.elseBranch ?. let { evalStatement(it, env) }
            }
        }
        is While -> {
            while(isTruthy(evalExpression(stmt.cond, env)))
            {
                evalStatement(stmt.body, env)
            }
        }
        is Return -> {
            throw ReturnValue(stmt.value ?. let { evalExpression(it, env) })
        }
    }
}

internal fun evalBlock(b: Block,
                       env: Environment,
                       bindedVars: Environment = mutableMapOf())
{
    val newEnv: Environment = (env.toMutableMap() + bindedVars) as Environment
    val newVars = VariablesMap(bindedVars)

    b.declarations.forEach { decl: Declaration ->
        evalDeclaration(decl, newEnv)
        when(decl) {
            is VarDeclaration ->
                newVars.checkFor(decl.name.lexeme)
            else -> newEnv.forEach { (k, v) ->
                if(!newVars.getOrDefault(k, false)) {
                    env[k] = v
                }
            }
        }
    }
}
