package com.example.project1311;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.project1311.animations.AnimationClasses;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Quiz app that shows number questions, tracks score, and saves high scores
 */
public class TestNumbers extends AppCompatActivity implements View.OnClickListener {

    // Link to get quiz questions
    private String apiUrl = "https://mocki.io/v1/f7c53113-f6da-45bf-be5f-6d7ac95bd72e";

    // Screen elements
    private TextView questionTextView;
    private TextView scoreTextView;
    private TextView questionNumberTextView;
    private TextView countdownTextView;
    private TextView highScoreTextView;
    private Button optionButton1;
    private Button optionButton2;
    private Button optionButton3;
    private Button optionButton4;
    private Button returnButton;
    private AnimationClasses.CheckMarkView checkMarkView;
    private AnimationClasses.XMarkView xMarkView;
    private AnimationClasses.WinningAnimationView winningAnimationView;
    private ViewGroup rootLayout;

    // Quiz variables
    private int score = 0;
    private int currentQuestionIndex = -1;
    private JSONArray questionsArray = null;
    private List<JSONObject> remainingQuestions = new ArrayList<>();

    // Timer stuff
    private Handler timerHandler;
    private Runnable countdownRunnable;
    private int countdownTime = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_numbers);

        // Find all screen elements
        findAllViews();

        // Set up all button clicks
        setupButtons();

        // Add winning animation to screen
        addWinningAnimation();

        // Get questions from internet
        getQuestionsFromInternet();

        // Show high score
        showHighScore();
    }

    // Find all screen elements by ID
    private void findAllViews() {
        rootLayout = findViewById(android.R.id.content);
        questionTextView = findViewById(R.id.questionTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        questionNumberTextView = findViewById(R.id.questionCounterTextView);
        countdownTextView = findViewById(R.id.countdownTextView);
        highScoreTextView = findViewById(R.id.highScoreTextView);
        optionButton1 = findViewById(R.id.optionButton1);
        optionButton2 = findViewById(R.id.optionButton2);
        optionButton3 = findViewById(R.id.optionButton3);
        optionButton4 = findViewById(R.id.optionButton4);
        returnButton = findViewById(R.id.button);
        checkMarkView = findViewById(R.id.checkMarkView);
        xMarkView = findViewById(R.id.xMarkView);
    }

    // Set what happens when buttons are clicked
    private void setupButtons() {
        returnButton.setOnClickListener(this);
        optionButton1.setOnClickListener(v -> checkAnswer(optionButton1));
        optionButton2.setOnClickListener(v -> checkAnswer(optionButton2));
        optionButton3.setOnClickListener(v -> checkAnswer(optionButton3));
        optionButton4.setOnClickListener(v -> checkAnswer(optionButton4));
    }

    // Add animation for winning
    private void addWinningAnimation() {
        winningAnimationView = new AnimationClasses.WinningAnimationView(this);
        winningAnimationView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        winningAnimationView.setVisibility(View.GONE);
        rootLayout.addView(winningAnimationView);
    }

    // Download helper for getting questions
    private class DownloadJson extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                // Connect to the internet
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read what we get back
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return result.toString();
        }
    }

    // Show user's best score from before
    private void showHighScore() {
        // Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Find where the score is stored
            DatabaseReference userScoreRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("scores")
                    .child("numbers");

            // Get the score and show it
            userScoreRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Integer currentHighScore = task.getResult().getValue(Integer.class);
                    if (currentHighScore != null) {
                        highScoreTextView.setText("High Score: " + currentHighScore);
                    }
                }
            });
        }
    }

    // Get questions from the internet
    private void getQuestionsFromInternet() {
        DownloadJson downloadJson = new DownloadJson();
        String result = null;

        try {
            result = downloadJson.execute(apiUrl).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        // Make sure we got something back
        if (result != null && result.length() > 2) {
            try {
                // Turn the text into questions
                questionsArray = new JSONArray(result);

                // Put all questions in our list
                for (int i = 0; i < questionsArray.length(); i++) {
                    remainingQuestions.add(questionsArray.getJSONObject(i));
                }

                // Show first question
                showNextQuestion();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(TestNumbers.this, "Error reading the questions.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(TestNumbers.this, "Couldn't get questions from internet.", Toast.LENGTH_SHORT).show();
        }
    }

    // Pick and show a random question
    private void showNextQuestion() {
        if (remainingQuestions.size() > 0) {
            // Pick a random question
            Random rand = new Random();
            int randomIndex = rand.nextInt(remainingQuestions.size());
            JSONObject currentQuestion = remainingQuestions.get(randomIndex);
            remainingQuestions.remove(randomIndex);

            try {
                // Get the question info
                String question = currentQuestion.getString("question");
                JSONArray optionsArray = currentQuestion.getJSONArray("options");
                int correctAnswerIndex = currentQuestion.getInt("correctAnswer") - 1;

                // Update the question counter
                currentQuestionIndex++;
                questionNumberTextView.setText("Question " + (currentQuestionIndex + 1));

                // Show the question
                questionTextView.setText(question);

                // Set the answer choices
                optionButton1.setText(optionsArray.getString(0));
                optionButton2.setText(optionsArray.getString(1));
                optionButton3.setText(optionsArray.getString(2));
                optionButton4.setText(optionsArray.getString(3));

                // Remember which button has the right answer
                optionButton1.setTag(correctAnswerIndex == 0);
                optionButton2.setTag(correctAnswerIndex == 1);
                optionButton3.setTag(correctAnswerIndex == 2);
                optionButton4.setTag(correctAnswerIndex == 3);

                // Start the timer
                startTimer();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(TestNumbers.this, "Problem showing question.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // No more questions - quiz is done
            turnOffButtons();
            showWinAnimation();
        }
    }

    // Start countdown timer for question
    private void startTimer() {
        countdownTime = 20;
        countdownTextView.setText("" + countdownTime);

        // Clear old timer if there is one
        if (timerHandler != null) {
            timerHandler.removeCallbacks(countdownRunnable);
        }

        // What to do every second
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                countdownTime--;
                countdownTextView.setText("" + countdownTime);

                if (countdownTime <= 0) {
                    // Time's up - next question
                    showNextQuestion();
                } else {
                    // Wait another second
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        // Start the timer
        timerHandler = new Handler();
        timerHandler.postDelayed(countdownRunnable, 1000);
    }

    // Check if answer is right
    private void checkAnswer(Button clickedButton) {
        boolean isCorrect = (boolean) clickedButton.getTag();
        int buttonNormalColor = clickedButton.getBackgroundTintList().getDefaultColor();

        if (isCorrect) {
            // Right answer!
            score++;
            scoreTextView.setText("Score: " + score);
            clickedButton.setBackgroundColor(Color.GREEN);

            // Show checkmark
            checkMarkView.setVisibility(View.VISIBLE);
            xMarkView.setVisibility(View.INVISIBLE);
            checkMarkView.startAnimation();
        } else {
            // Wrong answer
            clickedButton.setBackgroundColor(Color.RED);

            // Show X mark
            xMarkView.setVisibility(View.VISIBLE);
            checkMarkView.setVisibility(View.INVISIBLE);
            xMarkView.startAnimation();
        }

        // Wait a bit before next question
        new Handler().postDelayed(() ->
        {
            // Change button back to normal color
            clickedButton.setBackgroundColor(buttonNormalColor);

            // Hide the marks
            checkMarkView.setVisibility(View.INVISIBLE);
            xMarkView.setVisibility(View.INVISIBLE);
            checkMarkView.reset();
            xMarkView.reset();

            if (remainingQuestions.isEmpty()) {
                // No more questions
                stopTimer();
                turnOffButtons();

                // Check if beat high score
                saveBestScore();

                // Show animation if score is good
                if (score > 5) {
                    showWinAnimation();
                } else {
                    Toast.makeText(this, "Quiz done! Your score: " + score, Toast.LENGTH_LONG).show();
                }
            } else {
                // Show next question
                showNextQuestion();
            }
        }, 300);
    }

    // Save score if it's the best one
    private void saveBestScore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Find where scores are saved
            DatabaseReference userScoreRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("scores")
                    .child("numbers");

            // Check if current score is better than high score
            userScoreRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Integer highScore = task.getResult().getValue(Integer.class);
                    if (highScore == null || score > highScore) {
                        // Save new high score
                        userScoreRef.setValue(score);
                        showHighScore();
                    }
                }
            });
        }
    }

    // Show animation when winning
    private void showWinAnimation() {
        winningAnimationView.setVisibility(View.VISIBLE);

        // Hide animation after 3 seconds
        new Handler().postDelayed(() -> {
            winningAnimationView.setVisibility(View.GONE);
            Toast.makeText(this, "Congratulations! You got a high score!", Toast.LENGTH_LONG).show();
        }, 3000);
    }

    // Turn off answer buttons
    private void turnOffButtons() {
        optionButton1.setEnabled(false);
        optionButton2.setEnabled(false);
        optionButton3.setEnabled(false);
        optionButton4.setEnabled(false);
    }

    // Stop the timer
    private void stopTimer() {
        if (timerHandler != null) {
            timerHandler.removeCallbacks(countdownRunnable);
            countdownTime = 0;
            countdownTextView.setText("" + countdownTime);
        }
    }

    // Handle return button click
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            finish();
        }
    }

    // Clean up when activity closes
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(countdownRunnable);
        }
    }
}