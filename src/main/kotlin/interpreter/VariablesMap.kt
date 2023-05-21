package interpreter

import exceptions.RuntimeError
import exceptions.VariableAlreadyDeclared

class VariablesMap() : HashMap<String, Boolean>(mutableMapOf())
{
    fun checkFor(key: String) {
        val v = super.getOrDefault(key, false)
        when(v) {
            true -> throw VariableAlreadyDeclared(key)
            false -> super.put(key, true)
        }
    }
}
