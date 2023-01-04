package search

import java.io.File

class InputReader {

    fun getInputSets(filePath: String): MutableList<Set<String>> {
        val inputSets = mutableListOf<Set<String>>()
        File(filePath).forEachLine { inputSets.add(it.split(" ").toSet()) }
        return inputSets
    }

    fun getInvertedIndexMap(filePath: String): Map<String, Set<Int>> = buildMap {
        val map = mutableMapOf<String, MutableSet<Int>>()
        var currentLine = 0
        File(filePath).forEachLine {
            it
                .strip()
                .split(" ")
                .filter { i -> i.isNotBlank() }
                .forEach { word ->
                    if (map.containsKey(word.lowercase()))
                        map[word.lowercase()]!! += currentLine
                    else
                        map[word.lowercase()] = mutableSetOf(currentLine)
                }
            currentLine++
        }
        putAll(map)
    }

}