package search

import kotlin.system.measureTimeMillis

class SearchEngine {
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

    fun printSearchResults(
        query: String,
        dataSets: MutableList<Set<String>>,
        invertedIndexMap: Map<String, Set<Int>>,
        strategy: SearchStrategy
    ) {
        val perf = measureTimeMillis {
            when (strategy) {
                SearchStrategy.ANY -> printAnyMatches(
                    query = query,
                    dataSets = dataSets,
                    invertedIndexMap = invertedIndexMap
                )

                SearchStrategy.ALL -> printIntersectionMatches(
                    query = query,
                    dataSets = dataSets,
                    invertedIndexMap = invertedIndexMap
                )

                SearchStrategy.NONE -> printNonInclusiveMatches(
                    query = query,
                    dataSets = dataSets,
                    invertedIndexMap = invertedIndexMap
                )
            }
        }
        println("\nSearch executed in $perf ms")
        println("Search query: \"$query\", strategy: $strategy")
    }

    private fun printAnyMatches(
        query: String,
        dataSets: MutableList<Set<String>>,
        invertedIndexMap: Map<String, Set<Int>>,
    ) = printMatchingItems(dataSets)
    {
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

    private fun printIntersectionMatches(
        query: String,
        dataSets: MutableList<Set<String>>,
        invertedIndexMap: Map<String, Set<Int>>,
    ) = printMatchingItems(dataSets)
    {
        var matchingIndexes = mutableSetOf<Int>()
        query
            .split(" ")
            .filter { word -> word.isNotBlank() }
            .run iterativeIntersectionSearch@{
                this.forEach {
                    runCatching { invertedIndexMap.getValue(it.lowercase()) }
                        .onSuccess {
                            if (matchingIndexes.size == 0)
                                matchingIndexes.addAll(it)
                            else
                                matchingIndexes = matchingIndexes.intersect(it).toMutableSet()
                            if (matchingIndexes.size == 0) return@iterativeIntersectionSearch
                        }
                        .onFailure {
                            matchingIndexes.clear()
                            return@iterativeIntersectionSearch
                        }
                }
            }
            .run { matchingIndexes }
    }

    private fun printNonInclusiveMatches(
        query: String,
        dataSets: MutableList<Set<String>>,
        invertedIndexMap: Map<String, Set<Int>>,
    ) = printMatchingItems(dataSets)
    {
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

enum class SearchStrategy(val description: String) {
    ALL("Find results containing all search query words"),
    ANY("Find results containing any word from search query"),
    NONE("Find results containing NONE of the search query words")
}