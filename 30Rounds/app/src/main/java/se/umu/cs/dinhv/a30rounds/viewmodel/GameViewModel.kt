package se.umu.cs.dinhv.a30rounds.viewmodel

import androidx.lifecycle.*
import se.umu.cs.dinhv.a30rounds.model.Game

/**
 * ViewModel class for the game logic. Manages game state and provides data to the UI.
 */
class GameViewModel(state: SavedStateHandle) : ViewModel() {
    private val _dice = MutableLiveData<List<Int>>()
    val dice: LiveData<List<Int>> get() = _dice

    private val _round = MutableLiveData<Int>()
    val round: LiveData<Int> get() = _round

    private val _throws = MutableLiveData<Int>()
    val throws: LiveData<Int> get() = _throws

    private val _score = MutableLiveData<Map<String, Int>>()
    val score: LiveData<Map<String, Int>> get() = _score

    private val _gameOver = MutableLiveData<Boolean>()
    val gameOver: LiveData<Boolean> get() = _gameOver

    private val stateHandle: SavedStateHandle = state
    var game: Game = stateHandle.get<Game>("game_state") ?: Game().apply { startNewGame() }

    init {
        updateState()
    }

    /**
     * Starts a new game by resetting the game state and rolling the dice.
     */
    fun startNewGame() {
        game.startNewGame()
        updateState()
    }

    /**
     * Rolls the dice, updating the LiveData with the new values.
     * Only rolls if there are throws remaining.
     * @param selectedDices A list of booleans indicating which dices are selected to be re-rolled.
     */
    fun rollDice(selectedDices: List<Boolean>) {
        if (_throws.value!! < 3) {
            game.rollDices(selectedDices)
            updateState()
        }
    }

    /**
     * Advances the game to the next round and resets the dice throws.
     */
    fun nextRound() {
        game.nextRound()
        updateState()
    }

    /**
     * Calculates the score for a given category and updates the LiveData.
     * @param category The category for which to calculate the score.
     * @return The calculated score.
     */
    fun calculateScore(category: String): Int {
        val score = game.calculateScore(category)
        updateState()
        return score
    }

    /**
     * Checks if the game is over.
     * @return True if all rounds are completed, false otherwise.
     */
    fun roundsCompleted(): Boolean = game.isGameOver()

    /**
     * Gets the list of available categories that have not been used.
     * @return The list of available categories.
     */
    fun getAvailableCategories(): List<String> {
        val allCategories = listOf("Low", "4", "5", "6", "7", "8", "9", "10", "11", "12")
        return allCategories.filterNot { game.getUsedCategories().contains(it) }
    }

    /**
     * Saves the game state.
     */
    fun saveGameState() {
        stateHandle.set("game_state", game)
    }

    /**
     * Updates the state handle and live data.
     */
    private fun updateState() {
        _dice.value = game.getDices()
        _round.value = game.getRound()
        _throws.value = game.getThrows()
        _score.value = game.getScores()
        saveGameState()
    }
}
