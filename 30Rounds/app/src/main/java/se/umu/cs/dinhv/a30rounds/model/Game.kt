package se.umu.cs.dinhv.a30rounds.model

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

/**
 * Game represents the state and logic of the game.
 */
class Game() : Parcelable {
    private val dices = MutableList(6) { Dice() }
    private var round = 1
    private var numThrows = 0
    private val scores = mutableMapOf<String, Int>()
    private val usedCategories = mutableSetOf<String>()
    private var selectedDices = List(6) { false }

    constructor(parcel: Parcel) : this() {
        round = parcel.readInt()
        numThrows = parcel.readInt()
        parcel.readTypedList(dices, Dice.CREATOR)
        val scoresBundle = parcel.readBundle(HashMap<String, Int>().javaClass.classLoader)
        scores.putAll(scoresBundle?.keySet()?.associateWith { scoresBundle.getInt(it) } ?: emptyMap())
        usedCategories.addAll(parcel.createStringArrayList() ?: listOf())
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(round)
        parcel.writeInt(numThrows)
        parcel.writeTypedList(dices)
        val scoresBundle = Bundle().apply {
            scores.forEach { (key, value) -> putInt(key, value) }
        }
        parcel.writeBundle(scoresBundle)
        parcel.writeStringList(usedCategories.toList())
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Game> {
        override fun createFromParcel(parcel: Parcel): Game = Game(parcel)
        override fun newArray(size: Int): Array<Game?> = arrayOfNulls(size)
    }

    /**
     * Starts a new game by resetting the game state and rolling the dice.
     */
    fun startNewGame() {
        round = 1
        numThrows = 0
        scores.clear()
        usedCategories.clear()
        rollDices(List(6) { false }) // Roll all dice initially
        numThrows = 1 // Count the initial roll as the first throw
    }

    /**
     * Advances to the next round and rolls the dice immediately.
     */
    fun nextRound() {
        round++
        numThrows = 0
        rollDices(List(6) { false }) // Roll all dice initially
        numThrows = 1 // Count the initial roll as the first throw
    }

    /**
     * Rolls the dice that are not selected.
     *
     * @param selected A list indicating which dice are selected.
     */
    fun rollDices(selected: List<Boolean>) {
        selectedDices = selected
        for (i in dices.indices) {
            if (!selected[i]) {
                dices[i].roll()
            }
        }
        numThrows++
    }

    /**
     * Gets the current dice values.
     *
     * @return A list of dice values.
     */
    fun getDices(): List<Int> = dices.map { it.value }

    /**
     * Gets the current round number.
     *
     * @return The current round number.
     */
    fun getRound(): Int = round

    /**
     * Gets the current number of throws.
     *
     * @return The current number of throws.
     */
    fun getThrows(): Int = numThrows

    /**
     * Gets the current scores.
     *
     * @return A map of category scores.
     */
    fun getScores(): Map<String, Int> = scores

    /**
     * Gets the used categories.
     *
     * @return A set of used categories.
     */
    fun getUsedCategories(): Set<String> = usedCategories

    /**
     * Checks if the game is over.
     *
     * @return True if the game is over, false otherwise.
     */
    fun isGameOver(): Boolean = round == 10

    /**
     * Calculates the score for a given category.
     *
     * @param category The scoring category.
     * @return The calculated score.
     */
    fun calculateScore(category: String): Int {
        if (category in usedCategories) {
            return 0
        }
        usedCategories.add(category)

        val diceValues = getSelectedDices().sortedDescending()
        val score = when (category) {
            "Low" -> calculateLowScore(diceValues)
            else -> calculateCombinationScore(diceValues, category.toIntOrNull() ?: return 0)
        }
        scores[category] = score
        return score
    }

    /**
     * Calculates the low score.
     *
     * @param diceValues A list of dice values.
     * @return The calculated low score.
     */
    private fun calculateLowScore(diceValues: List<Int>): Int {
        return diceValues.filter { it <= 3 }.sum()
    }

    /**
     * Calculates the score for a combination category.
     *
     * @param diceValues A list of dice values.
     * @param target The target value for the combination.
     * @return The calculated combination score.
     */
    private fun calculateCombinationScore(diceValues: List<Int>, target: Int): Int {
        val validCombinations = mutableListOf<List<Int>>()
        val usedDiceIndices = mutableSetOf<Int>()
        var totalScore = 0

        for (combination in findAllCombinations(diceValues)) {
            if (combination.sum() == target) {
                val tempUsedDiceIndices = mutableSetOf<Int>()
                var isValidCombination = true

                for (value in combination) {
                    val index = indexOfValue(value, diceValues, usedDiceIndices + tempUsedDiceIndices)
                    if (index == -1) {
                        isValidCombination = false
                        break
                    } else {
                        tempUsedDiceIndices.add(index)
                    }
                }

                if (isValidCombination) {
                    validCombinations.add(combination)
                    totalScore += target
                    usedDiceIndices.addAll(tempUsedDiceIndices)
                }
            }
        }
        return totalScore
    }

    /**
     * Finds the index of a value in the list that is not already used.
     *
     * @param value The value to find.
     * @param list The list to search.
     * @param usedIndices The set of used indices.
     * @return The index of the value, or -1 if not found.
     */
    private fun indexOfValue(value: Int, list: List<Int>, usedIndices: Set<Int>): Int {
        return list.withIndex().indexOfFirst { it.value == value && it.index !in usedIndices }
    }

    /**
     * Finds all possible combinations of dice values.
     *
     * @param diceValues A list of dice values.
     * @return A list of all combinations.
     */
    private fun findAllCombinations(diceValues: List<Int>): List<List<Int>> {
        val results = mutableListOf<List<Int>>()
        val currentCombination = mutableListOf<Int>()
        generateCombinations(diceValues, 0, currentCombination, results)
        return results
    }

    /**
     * Generates all combinations of dice values recursively.
     *
     * @param diceValues A list of dice values.
     * @param start The starting index for combinations.
     * @param currentCombination The current combination being built.
     * @param results The list of all found combinations.
     */
    private fun generateCombinations(
        diceValues: List<Int>,
        start: Int,
        currentCombination: MutableList<Int>,
        results: MutableList<List<Int>>
    ) {
        for (i in start until diceValues.size) {
            currentCombination.add(diceValues[i])
            results.add(ArrayList(currentCombination))
            generateCombinations(diceValues, i + 1, currentCombination, results)
            currentCombination.removeAt(currentCombination.size - 1)
        }
    }

    /**
     * Gets the values of the selected dices.
     *
     * @return A list of selected dice values.
     */
    private fun getSelectedDices(): List<Int> {
        return dices.filterIndexed { index, _ -> selectedDices[index] }.map { it.value }
    }
}
