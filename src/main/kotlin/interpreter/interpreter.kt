package interpreter

import ast.*
import exceptions.Errors
import exceptions.RuntimeError
import interpreter.callable.LoxCallable
import interpreter.callable.LoxFunction
import interpreter.callable.arity
import lexer.LiteralValue
import lexer.Token
import lexer.TokenType

fun intepret(declarations: List<Declaration>, env: Environment = mutableMapOf()) {
    val newVars = VariablesMap(env)
    try {
        declarations.forEach { decl ->
            when(decl) {
                is VarDeclaration -> {
                    newVars.checkFor(decl.name.lexeme)
                    evalDeclaration(decl, env)
                }
                else -> evalDeclaration(decl, env)
            }
        }
    } catch (e: RuntimeError) {
        Errors.error(e.token, e.message)
    }
}

private fun evalDeclaration(decl: Declaration, env: Environment) = when(decl) {
    is Statement -> evalStatement(decl, env)
    is VarDeclaration -> {
        env.put(decl.name.lexeme,
            decl.init?. let { evalExpression(it, env) }
        )
    }
    is FunDeclaration -> {
        val f = LoxFunction(decl)
        assign(env, decl.name, f)
    }
}

private fun evalStatement(stmt: Statement, env: Environment) {
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
            when(isTruthy(stmt.cond)) {
                true -> evalStatement(stmt.thenBranch, env)
                false -> if(stmt.elseBranch != null) evalStatement(stmt.elseBranch, env)
            }
        }
        is While -> {
            while(isTruthy(evalExpression(stmt.cond, env)))
            {
                evalStatement(stmt.body, env)
            }
         }
    }
}

private fun evalBlock(
    b: Block,
    env: Environment,
    bindedVars: Environment = mutableMapOf()
)
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

private fun evalExpression(expr: Expression, env: Environment) : LiteralValue {
    return when (expr) {
        is Literal -> expr.value!!
        is Grouping -> evalExpression(expr.expr, env)
        is Variable -> env[expr.name.lexeme]!!
        is Assignment -> {
            val value = evalExpression(expr.value, env)
            assign(env, expr.name, value)
            value
        }
        is Call -> {
            val callee = evalExpression(expr.callee, env)

            if(callee !is LoxCallable) {
                throw RuntimeError(expr.paren,
                    "Can only call functions and classes")
            }

            val args = expr.args.map { arg ->
                evalExpression(arg, env)
            }

            if(args.size != arity(callee)) {
                throw RuntimeError(expr.paren,
                    "Expected ${arity(callee)} arguments but got ${args.size}.")
            }

            call(callee, env, args)
        }

        is Unary -> {
            val right = evalExpression(expr.right, env)

            when (expr.operator.type) {
                TokenType.MINUS -> {
                    checkTypeOperands<Double>(expr.operator, right)
                    -(right as Double)
                }
                TokenType.BANG -> {
                    !(isTruthy(right))
                }
                else -> throw RuntimeError(expr.operator, "Unexpected operand")
            }
        }

        is Binary -> {
            val left = evalExpression(expr.left, env)
            val right = evalExpression(expr.right, env)

            when (expr.operator.type) {
                TokenType.PLUS -> {
                    when (left) {
                        is Double -> left + (right as Double)
                        is String -> left + (right as String)
                        else -> throw RuntimeError(expr.operator, "Operands must be numbers or strings")
                    }
                }

                TokenType.MINUS -> {
                    checkTypeOperands<Double>(expr.operator, left, right)
                    (left as Double) - (right as Double)
                }

                TokenType.STAR -> {
                    checkTypeOperands<Double>(expr.operator, left, right)
                    (left as Double) * (right as Double)
                }

                TokenType.SLASH -> {
                    checkTypeOperands<Double>(expr.operator, left, right)
                    (left as Double) / (right as Double)
                }

                TokenType.GREATER -> {
                    checkTypeOperands<Double>(expr.operator, left, right)
                    (left as Double) > (right as Double)
                }

                TokenType.GREATER_EQUAL -> {
                    checkTypeOperands<Double>(expr.operator, left, right)
                    (left as Double) >= (right as Double)
                }

                TokenType.LESS -> {
                    checkTypeOperands<Double>(expr.operator, left, right)
                    (left as Double) < (right as Double)
                }

                TokenType.LESS_EQUAL -> {
                    checkTypeOperands<Double>(expr.operator, left, right)
                    (left as Double) <= (right as Double)
                }

                TokenType.BANG_EQUAL -> left != right
                TokenType.EQUAL_EQUAL -> left == right
                else -> Errors.error(expr.operator, "Unexpected operator")
            }
        }
        is Logical -> {
            val left = evalExpression(expr.left, env)

            when(expr.operator.type) {
                TokenType.OR -> {
                    if(isTruthy(left))
                        left
                    else
                        evalExpression(expr.right, env)
                }
                TokenType.AND -> {
                    if(isTruthy(left))
                        evalExpression(expr.left, env)
                    else
                        left
                }
                else -> throw RuntimeError(expr.operator, "Unexpected operand")
            }
        }
    }
}


fun call(c: LoxCallable, env: Environment, args: List<LiteralValue>) {
    return when(c) {
        is LoxFunction -> {
            val params = c.decl.params
            val bindedVars = env.toMutableMap()
            (params.zip(args)).forEach { (param, arg) ->
                bindedVars.put(
                    param.lexeme,
                    arg
                )
            }
            evalBlock(c.decl.body, env, bindedVars)
        }
    }
}


fun assign(env: Environment, name: Token, value: LiteralValue) {
    if(env.containsKey(name.lexeme))
    {
        env[name.lexeme] = value
        return
    }

    throw RuntimeError(
        name,
        "Undefined variable '" + name.lexeme + "'."
    )
}

fun isTruthy(e: LiteralValue?) = when(e) {
    null -> false
    is Boolean -> e
    is Double -> e != 0
    else -> true
}

inline fun <reified T> checkTypeOperands(operator: Token, vararg operands: LiteralValue?) {
    if(!operands.all { operand -> operand is T })
    {
        throw RuntimeException("$operator Operand must be a number.")
    }
}