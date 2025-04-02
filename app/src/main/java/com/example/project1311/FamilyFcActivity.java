package com.example.project1311;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class FamilyFcActivity extends AppCompatActivity implements View.OnClickListener {
    TextView tv1;
    Button btnP, btnRe, btnSpeak;  // Added btnSpeak for speaking the word

    // Unified Collection instance to manage questions
    private Collection collection;
    private Question currentQuestion; // To hold the current question
    private TextToSpeech textToSpeech; // Text-to-Speech instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_fc);

        tv1 = findViewById(R.id.tvQ);
        btnP = findViewById(R.id.btn1);
        btnRe = findViewById(R.id.btnReturn);
        btnSpeak = findViewById(R.id.btnSpeak);  // Initialize the new Speak button

        // Initialize the unified Collection
        collection = new Collection();

        // Set the category to "Family"
        collection.setCategory("Family");

        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.FRENCH); // Set the language to French
            }
        });

        // Set click listeners
        btnP.setOnClickListener(this);
        btnRe.setOnClickListener(this);
        btnSpeak.setOnClickListener(v -> speakWord()); // Set the click listener for the Speak button

        // Display the first question
        putNextQuestion();
    }

    @Override
    public void onClick(View v) {
        if (v == btnP) {
            // Load and display the next question
            putNextQuestion();
        } else if (v == btnRe) {
            // Return to the previous activity
            finish();
        }
    }

    private void putNextQuestion() {
        if (collection.isNotLastQuestion()) {
            // Get the next question from the collection
            currentQuestion = collection.getNextQuestion();

            // Format the question and its translation
            String questionAndAnswer = currentQuestion.getWord() + " - " + currentQuestion.getTranslation();

            // Set the formatted question in the TextView
            tv1.setText(questionAndAnswer);
        } else {
            // When no more questions are available
            tv1.setText("No more FlashCards");
            btnP.setEnabled(false); // Disable the next button
        }
    }

    private void speakWord() {
        if (currentQuestion != null && !tv1.getText().toString().equals("No more FlashCards")) {
            // Speak the word (original word in the Family category)
            String wordToSpeak = currentQuestion.getWord();
            textToSpeech.speak(wordToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
