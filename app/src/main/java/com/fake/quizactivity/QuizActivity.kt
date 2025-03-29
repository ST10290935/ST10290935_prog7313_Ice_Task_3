package com.fake.quizactivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fake.quizactivity.R.id.usernameInput
import com.google.firebase.firestore.FirebaseFirestore

class QuizActivity : AppCompatActivity() {
    private lateinit var questionText: TextView
    private lateinit var optionsGroup: RadioGroup
    private lateinit var nextButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var usernameInput: EditText
    private val db = FirebaseFirestore.getInstance()

    private var category: String = ""
    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private val questions = mutableListOf<Question>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        questionText = findViewById(R.id.questionText)
        optionsGroup = findViewById(R.id.optionsGroup)
        nextButton = findViewById(R.id.nextButton)
        progressBar = findViewById(R.id.progressBar)
        usernameInput = findViewById(R.id.usernameInput)

        category = intent.getStringExtra("CATEGORY") ?: ""
        loadQuestions()

        nextButton.setOnClickListener { checkAnswer() }
    }

    private fun loadQuestions() {
        db.collection("questions").whereEqualTo("category", category).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val question = document.toObject(Question::class.java)
                    questions.add(question)
                }
                displayQuestion()
            }
    }

    private fun displayQuestion() {
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            questionText.text = question.text
            optionsGroup.removeAllViews()

            question.options.forEach { option ->
                val radioButton = RadioButton(this)
                radioButton.text = option
                optionsGroup.addView(radioButton)
            }

            progressBar.progress = ((currentQuestionIndex.toFloat() / questions.size) * 100).toInt()
        } else {
            saveScore()
        }
    }

    private fun checkAnswer() {
        val selectedId = optionsGroup.checkedRadioButtonId
        if (selectedId != -1) {
            val selectedOption = findViewById<RadioButton>(selectedId).text.toString()
            if (selectedOption == questions[currentQuestionIndex].correctAnswer) {
                correctAnswers++
            }
            currentQuestionIndex++
            displayQuestion()
        }
    }

    private fun saveScore() {
        val username = usernameInput.text.toString()  // Get user input for username
        if (username.isNotEmpty()) {
            val scoreData = hashMapOf(
                "username" to username,
                "category" to category,
                "score" to correctAnswers
            )
            db.collection("scores").add(scoreData)
                .addOnSuccessListener { finish() }
        } else {
            // Handle case where no username is entered (optional)
        }
    }

    companion object {
        fun newIntent(context: Context, category: String): Intent {
            val intent = Intent(context, QuizActivity::class.java)
            intent.putExtra("CATEGORY", category)
            return intent
        }
    }
}

data class Question(
    val text: String = "",
    val options: List<String> = listOf(),
    val correctAnswer: String = ""
)
