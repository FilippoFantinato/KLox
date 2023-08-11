package interpreter.callable

import ast.FunDeclaration
import interpreter.environment.Environment

sealed interface LoxCallable

class LoxFunction(val decl: FunDeclaration, val env: Environment) : LoxCallable


fun arity(c: LoxCallable) : Int = when(c) {
    is LoxFunction -> c.decl.params.size
}
