package search

fun main(args: Array<String>) {
    val filePath = args[1]
    val inputSets = InputReader().getInputSets(filePath)
    val invertedIndexMap = InputReader().getInvertedIndexMap(filePath)

    menuLoop@ while (true) {
        Menu().getMenu()
        when (readln()) {
            "1" -> {
                println("\nSelect a matching strategy: ${SearchStrategy.values().joinToString()}")
                val strategy: SearchStrategy
                runCatching { SearchStrategy.valueOf(readln()) }
                    .onSuccess {
                        strategy = it
                        println("\nEnter data to search compositions:")
                        SearchEngine().printMatchingItems(
                            query = readln(),
                            dataSets = inputSets,
                            invertedIndexMap = invertedIndexMap,
                            strategy = strategy
                        )
                    }
                    .onFailure { println("Incorrect strategy provided $it") }
            }

            "2" -> {
                println("\n=== List of compositions ===")
                println("Total of ${inputSets.size} compositions")
                inputSets.forEach { println(it.joinToString(" ")) }
            }

            "0" -> {
                println("\nBye!")
                return
            }

            else -> println("\nIncorrect option! Try again.")
        }
    }
}
