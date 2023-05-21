package exceptions

class VariableAlreadyDeclared(private val name: String) :
        RuntimeException("Variable '$name' is already declared")
