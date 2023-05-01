//package eval
//
//import ast.*
//import exceptions.RuntimeError
//import lexer.LiteralValue
//import lexer.Token
//import lexer.TokenType
//
//fun eval(ast: Expression): LiteralValue = when(ast) {
//    is Literal -> ast.value
//    is Grouping -> eval(ast.expr)
//    is Unary -> {
//        val right = eval(ast.right)
//
//        when(ast.operator.type) {
//            TokenType.MINUS -> {
//                checkTypeOperands<Double>(ast.operator, right);
//
//                -(right as Double)
//            }
//            TokenType.BANG  -> {
//                !(isTruthy(right))
//            }
//        }
//    }
//    is Binary -> {
//        val left = eval(ast.left)
//        val right = eval(ast.right)
//
//        when(ast.operator.type)
//        {
//            TokenType.PLUS  -> {
//                when(left) {
//                    is Double -> left + (right as Double)
//                    is String -> left + (right as String)
//                    else -> throw RuntimeError(ast.operator, "Operands must be numbers or strings")
//                }
//            }
//            TokenType.MINUS -> {
//                checkTypeOperands<Double>(ast.operator, left, right);
//                (left as Double) - (right as Double)
//            }
//            TokenType.STAR  -> {
//                checkTypeOperands<Double>(ast.operator, left, right);
//                (left as Double) * (right as Double)
//            }
//            TokenType.SLASH -> {
//                checkTypeOperands<Double>(ast.operator, left, right);
//                (left as Double) / (right as Double)
//            }
//            TokenType.GREATER -> {
//                checkTypeOperands<Double>(ast.operator, left, right);
//                (left as Double) > (right as Double)
//            }
//            TokenType.GREATER_EQUAL -> {
//                checkTypeOperands<Double>(ast.operator, left, right);
//                (left as Double) >= (right as Double)
//            }
//            TokenType.LESS -> {
//                checkTypeOperands<Double>(ast.operator, left, right);
//                (left as Double) < (right as Double)
//            }
//            TokenType.LESS_EQUAL -> {
//                checkTypeOperands<Double>(ast.operator, left, right);
//                (left as Double) <= (right as Double)
//            }
//            TokenType.BANG_EQUAL -> !(left == right)
//            TokenType.EQUAL_EQUAL -> left == right
//            else -> { }
//        }
//    }
//}
//
//fun isTruthy(e: Any?) = when(e) {
//    null -> true
//    is Boolean -> e
//    is Double -> e != 0
//    else -> true
//}
//
//inline fun <reified T> checkTypeOperands(operator: Token, vararg operands: Any) {
//    if(!operands.all { operand -> operand is T })
//    {
//        throw RuntimeException("${operator} Operand must be a number.")
//    }
//}
