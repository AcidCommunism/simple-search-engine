package search

class SearchEngine {
    fun printMatchingItems(
        query: String,
        dataSets: MutableList<Set<String>>,
    ) {
        val matches = mutableSetOf<String>()
        dataSets
            .forEach { set ->
                set
                    .map { it.lowercase() }
                    .forEach { string ->
                        if (string == query.lowercase().strip())
                            matches.add(set.joinToString(" "))
                    }
            }
            .run {
                if (matches.size > 0) {
                    println("${matches.size} compositions found:")
                    matches.forEach(::println)
                } else println("No matching compositions found.")
            }
    }

    private fun printMatchingItems(
        dataSets: MutableList<Set<String>>,
        defineMatchingIndexes: () -> MutableSet<Int>
    ) {
        val matchingIndexes = defineMatchingIndexes()
        if (matchingIndexes.size > 0) {
            println("${matchingIndexes.size} compositions found:")
            matchingIndexes.forEach { println(dataSets[it].joinToString(" ")) }
            return
        }
        println("No compositions found.")
    }

    fun printMatchingItems(
        query: String,
        dataSets: MutableList<Set<String>>,
        invertedIndexMap: Map<String, Set<Int>>,
        strategy: SearchStrategy
    ) {
        when (strategy) {
            SearchStrategy.ANY -> printMatchingItems(dataSets) {
                val matchingIndexes = mutableSetOf<Int>()
                query
                    .split(" ")
                    .filter { word -> word.isNotBlank() }
                    .forEach {
                        runCatching { invertedIndexMap.getValue(it.lowercase()) }
                            .onSuccess { matchingIndexes.addAll(it) }
                    }
                    .run { matchingIndexes }
            }

            SearchStrategy.ALL -> printMatchingItems(dataSets) {
                val matchingIndexes = mutableSetOf<Int>()
                query
                    .split(" ")
                    .filter { word -> word.isNotBlank() }
                    .forEach {
                        runCatching { invertedIndexMap.getValue(it.lowercase()) }
                            .onSuccess {
                                if (matchingIndexes.isEmpty())
                                    matchingIndexes.addAll(it)
                                else
                                    it.forEach { matchingIndexes.filter { index -> index == it } }
                            }
                    }
                    .run { matchingIndexes }
            }

            SearchStrategy.NONE -> printMatchingItems(dataSets) {
                val matchingIndexes = mutableSetOf<Int>()
                val nonInclusiveIndexes = mutableSetOf<Int>()
                query
                    .split(" ")
                    .filter { word -> word.isNotBlank() }
                    .forEach {
                        runCatching { invertedIndexMap.getValue(it.lowercase()) }
                            .onSuccess {
                                nonInclusiveIndexes.addAll(it)
                            }
                    }
                    .run {
                        matchingIndexes.addAll(
                            dataSets.indices.filter { i -> !nonInclusiveIndexes.contains(i) }
                        )
                    }
                    .run { matchingIndexes }
            }

        }
    }
}

enum class SearchStrategy {
    ALL, ANY, NONE
}