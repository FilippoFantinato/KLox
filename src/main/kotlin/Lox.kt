class Lox {
    companion object {
        fun error(line: Int, message: String) {
            report(line, "", message)
        }

        private fun report(line: Int, where: String, message: String) {
            println("[lind: $line] Error $where: $message]")
        }
    }
}