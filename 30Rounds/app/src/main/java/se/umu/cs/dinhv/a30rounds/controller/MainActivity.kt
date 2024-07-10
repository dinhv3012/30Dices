package se.umu.cs.dinhv.a30rounds.controller

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import se.umu.cs.dinhv.a30rounds.R
import se.umu.cs.dinhv.a30rounds.viewmodel.GameViewModel

/**
 * MainActivity class that handles the main game logic and UI updates.
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: GameViewModel by viewModels()
    private val diceViews = mutableListOf<ImageView>()
    private var selectedDices = mutableListOf(false, false, false, false, false, false)
    private lateinit var roundTextView: TextView
    private lateinit var throwsTextView: TextView
    private lateinit var pointsTextView: TextView
    private lateinit var rollButton: Button
    private lateinit var categoryButton: Button

    private var isCategoryDialogOpen = false
    private var categoryDialog: AlertDialog? = null

    /**
     * Called when the activity is first created.
     * Initializes UI components and sets up observers for ViewModel.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeUIComponents()

        // Set click listeners for buttons
        rollButton.setOnClickListener { onRollClicked() }
        categoryButton.setOnClickListener { showCategoryDialog() }

        // Observe changes in the ViewModel
        observeViewModel()
        updateUI()

        // Set click listeners for dice views
        diceViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener { toggleDiceSelection(index) }
        }

        if (savedInstanceState == null) {
            viewModel.startNewGame()
        }

        handleBackButtonPress()
    }

    /**
     * Initializes the UI components.
     */
    private fun initializeUIComponents() {
        diceViews.apply {
            add(findViewById(R.id.dice1))
            add(findViewById(R.id.dice2))
            add(findViewById(R.id.dice3))
            add(findViewById(R.id.dice4))
            add(findViewById(R.id.dice5))
            add(findViewById(R.id.dice6))
        }
        roundTextView = findViewById(R.id.roundTextView)
        throwsTextView = findViewById(R.id.throwsTextView)
        pointsTextView = findViewById(R.id.pointsTextView)
        rollButton = findViewById(R.id.rollButton)
        categoryButton = findViewById(R.id.categoryButton)
    }

    /**
     * Observes changes in the ViewModel and updates the UI accordingly.
     */
    private fun observeViewModel() {
        viewModel.dice.observe(this, Observer { updateDiceUI(it) })
        viewModel.round.observe(this, Observer { roundTextView.text = "Round: $it" })
        viewModel.throws.observe(this, Observer { throws ->
            throwsTextView.text = "Throws: $throws"
            rollButton.visibility = if (throws == 3) View.GONE else View.VISIBLE
            categoryButton.visibility = if (throws == 3) View.VISIBLE else View.GONE
        })
        viewModel.score.observe(this, Observer { updatePointsUI(it) })
        viewModel.gameOver.observe(this, Observer { if (it) showResults() })
    }

    /**
     * Toggles the selection state of a dice.
     * @param index The index of the dice to toggle.
     */
    private fun toggleDiceSelection(index: Int) {
        selectedDices[index] = !selectedDices[index]
        updateDiceSelectionUI()
    }

    /**
     * Updates the UI to reflect the current selection state of the dice.
     */
    private fun updateDiceSelectionUI() {
        diceViews.forEachIndexed { index, imageView ->
            imageView.alpha = if (selectedDices[index]) 0.5f else 1.0f
        }
    }

    /**
     * Updates the UI to reflect the current state of the game.
     */
    private fun updateUI() {
        viewModel.dice.value?.let { updateDiceUI(it) }
        roundTextView.text = "Round: ${viewModel.round.value}"
        throwsTextView.text = "Throws: ${viewModel.throws.value}"
        updatePointsUI(viewModel.score.value ?: emptyMap())
    }

    /**
     * Handles the roll button click event.
     */
    private fun onRollClicked() {
        viewModel.rollDice(selectedDices)
        updateUI()
    }

    /**
     * Handles the event when a scoring category is selected.
     * @param category The selected scoring category.
     */
    private fun onCategorySelected(category: String) {
        viewModel.calculateScore(category)
        if (viewModel.roundsCompleted()) {
            showResults()
        } else {
            viewModel.nextRound()
            resetSelectedDices()
            updateUI()
        }
    }

    /**
     * Resets the selection state of all dice.
     */
    private fun resetSelectedDices() {
        selectedDices = mutableListOf(false, false, false, false, false, false)
        updateDiceSelectionUI()
    }

    /**
     * Shows the results of the game.
     */
    private fun showResults() {
        val intent = Intent(this, ResultActivity::class.java)
        val scoresBundle = Bundle().apply {
            viewModel.score.value?.forEach { (key, value) -> putInt(key, value) }
        }
        intent.putExtra("scores", scoresBundle)
        startActivity(intent)
        finish()
    }

    /**
     * Shows a dialog for selecting a scoring category.
     */
    private fun showCategoryDialog() {
        val availableCategories = viewModel.getAvailableCategories().toTypedArray()
        if (availableCategories.isEmpty()) {
            Toast.makeText(this, "No categories available.", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this)
            .setTitle("Select Scoring Category")
            .setItems(availableCategories) { _, which ->
                val selectedCategory = availableCategories[which]
                onCategorySelected(selectedCategory)
            }
            .setCancelable(false)
            .setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    Toast.makeText(this, "Please select a category to proceed.", Toast.LENGTH_SHORT).show()
                    true
                } else {
                    false
                }
            }

        categoryDialog = builder.create().apply {
            setOnDismissListener { isCategoryDialogOpen = false }
            show()
        }
        isCategoryDialogOpen = true
    }


    /**
     * Updates the points TextView to reflect the current scores.
     * @param scores A map of category names to scores.
     */
    private fun updatePointsUI(scores: Map<String, Int>) {
        pointsTextView.text = "Points: ${scores.values.sum()}"
    }

    /**
     * Gets the drawable resource ID for a given dice value.
     * @param value The value of the dice.
     * @return The drawable resource ID.
     */
    private fun getDiceDrawable(value: Int): Int {
        return when (value) {
            1 -> R.drawable.grey1
            2 -> R.drawable.grey2
            3 -> R.drawable.grey3
            4 -> R.drawable.grey4
            5 -> R.drawable.grey5
            6 -> R.drawable.grey6
            else -> R.drawable.grey1
        }
    }

    /**
     * Updates the dice UI to reflect the current values of the dice.
     * @param diceValues A list of dice values.
     */
    private fun updateDiceUI(diceValues: List<Int>) {
        diceValues.forEachIndexed { index, value ->
            diceViews[index].setImageResource(getDiceDrawable(value))
        }
    }

    /**
     * Called to save the activity state before it gets destroyed.
     * @param outState The Bundle in which to place the saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBooleanArray("selectedDices", selectedDices.toBooleanArray())
        viewModel.saveGameState() // Ensure state is saved when the activity's state is saved
    }

    /**
     * Called to restore the activity state after it has been recreated.
     * @param savedInstanceState The Bundle containing the saved state.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        selectedDices = savedInstanceState.getBooleanArray("selectedDices")?.toMutableList() ?: mutableListOf(false, false, false, false, false, false)
        updateUI()
    }

    /**
     * Called when the activity is paused.
     */
    override fun onPause() {
        super.onPause()
        viewModel.saveGameState()
    }

    /**
     * Handles the back button press using OnBackPressedDispatcher.
     */
    private fun handleBackButtonPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isCategoryDialogOpen) {
                    categoryDialog?.dismiss()
                } else {
                    viewModel.saveGameState()
                    finish()
                }
            }
        })
    }
}
