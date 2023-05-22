package interpreter

import exceptions.RuntimeError
import exceptions.VariableAlreadyDeclared

class VariablesMap() : HashMap<String, Boolean>(mutableMapOf())
{
    constructor(env: Environment) : this() {
        env.forEach { (k, _) -> checkFor(k) }
    }

    fun checkFor(key: String) {
        when(super.getOrDefault(key, false)) {
            true -> throw VariableAlreadyDeclared(key)
            false -> super.put(key, true)
        }
    }
}
