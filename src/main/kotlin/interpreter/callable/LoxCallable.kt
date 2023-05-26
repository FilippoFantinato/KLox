package interpreter.callable

import ast.FunDeclaration

sealed interface LoxCallable

class LoxFunction(val decl: FunDeclaration) : LoxCallable


fun arity(c: LoxCallable) : Int = when(c) {
    is LoxFunction -> c.decl.params.size
}
