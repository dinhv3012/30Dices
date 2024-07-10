package se.umu.cs.dinhv.a30rounds.controller

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import se.umu.cs.dinhv.a30rounds.R

/**
 * ResultActivity displays the final scores of the game.
 */
class ResultActivity : AppCompatActivity() {

    /**
     * Called when the activity is first created.
     * @param savedInstanceState The saved instance state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val bundle = intent.getBundleExtra("scores")
        val scores = bundle?.let {
            hashMapOf(
                "Low" to it.getInt("Low", 0),
                "4" to it.getInt("4", 0),
                "5" to it.getInt("5", 0),
                "6" to it.getInt("6", 0),
                "7" to it.getInt("7", 0),
                "8" to it.getInt("8", 0),
                "9" to it.getInt("9", 0),
                "10" to it.getInt("10", 0),
                "11" to it.getInt("11", 0),
                "12" to it.getInt("12", 0)
            )
        } ?: hashMapOf()

        // Display scores
        findViewById<TextView>(R.id.score_low).text = "Low Score: ${scores["Low"] ?: 0}"
        findViewById<TextView>(R.id.score_4).text = "Score 4: ${scores["4"] ?: 0}"
        findViewById<TextView>(R.id.score_5).text = "Score 5: ${scores["5"] ?: 0}"
        findViewById<TextView>(R.id.score_6).text = "Score 6: ${scores["6"] ?: 0}"
        findViewById<TextView>(R.id.score_7).text = "Score 7: ${scores["7"] ?: 0}"
        findViewById<TextView>(R.id.score_8).text = "Score 8: ${scores["8"] ?: 0}"
        findViewById<TextView>(R.id.score_9).text = "Score 9: ${scores["9"] ?: 0}"
        findViewById<TextView>(R.id.score_10).text = "Score 10: ${scores["10"] ?: 0}"
        findViewById<TextView>(R.id.score_11).text = "Score 11: ${scores["11"] ?: 0}"
        findViewById<TextView>(R.id.score_12).text = "Score 12: ${scores["12"] ?: 0}"
        findViewById<TextView>(R.id.total_score).text = "Total Score: ${scores.values.sum()}"

        findViewById<Button>(R.id.restartButton).setOnClickListener { restartGame() }
    }

    /**
     * Restarts the game by returning to MainActivity.
     */
    private fun restartGame() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
