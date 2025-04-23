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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.project1311.animations.AnimationClasses;

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

public class TestFamily extends AppCompatActivity implements View.OnClickListener {

    String apiUrl = "https://mocki.io/v1/69905a4f-f0b0-47ac-befb-69a2541fd680";

    TextView questionTextView, scoreTextView, questionNumberTextView, countdownTextView, highScoreTextView;
    Button optionButton1, optionButton2, optionButton3, optionButton4, Return;
    View checkMarkView, xMarkView; // Changed to generic Views
    AnimationClasses.CheckMarkView checkMarkAnimation;
    AnimationClasses.XMarkView xMarkAnimation;
    int score = 0;
    int currentQuestionIndex = -1;
    JSONArray questionsArray = null;
    List<JSONObject> remainingQuestions = new ArrayList<>();
    private AnimationClasses.WinningAnimationView winningAnimationView;
    private ViewGroup rootLayout;

    private Handler timerHandler;
    private Runnable countdownRunnable;
    private int countdownTime = 20;

    private class DownloadJson extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_family);

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
        Return = findViewById(R.id.button);
        checkMarkView = findViewById(R.id.checkMarkView);
        xMarkView = findViewById(R.id.xMarkView);
        Return.setOnClickListener(this);

        // Create animation objects
        checkMarkAnimation = new AnimationClasses.CheckMarkView(this);
        xMarkAnimation = new AnimationClasses.XMarkView(this);

        // Replace the placeholder views with our custom animation views
        ViewGroup parent = (ViewGroup) checkMarkView.getParent();
        int checkIndex = parent.indexOfChild(checkMarkView);
        int xIndex = parent.indexOfChild(xMarkView);

        parent.removeView(checkMarkView);
        parent.removeView(xMarkView);

        checkMarkAnimation.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        xMarkAnimation.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        checkMarkAnimation.setVisibility(View.INVISIBLE);
        xMarkAnimation.setVisibility(View.INVISIBLE);

        parent.addView(checkMarkAnimation, checkIndex);
        parent.addView(xMarkAnimation, xIndex);

        // Update references
        checkMarkView = checkMarkAnimation;
        xMarkView = xMarkAnimation;

        // Use the WinningAnimationView from AnimationClasses
        winningAnimationView = new AnimationClasses.WinningAnimationView(this);
        winningAnimationView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        winningAnimationView.setVisibility(View.GONE);
        rootLayout.addView(winningAnimationView);

        loadQuestionsFromAPI();
        loadHighScore();

        optionButton1.setOnClickListener(v -> checkAnswer(optionButton1));
        optionButton2.setOnClickListener(v -> checkAnswer(optionButton2));
        optionButton3.setOnClickListener(v -> checkAnswer(optionButton3));
        optionButton4.setOnClickListener(v -> checkAnswer(optionButton4));
    }

    private void loadHighScore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userScoreRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("scores")
                    .child("family");

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

    private void loadQuestionsFromAPI() {
        DownloadJson downloadJson = new DownloadJson();
        String result = null;

        try {
            result = downloadJson.execute(apiUrl).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null && result.length() > 2) {
            try {
                questionsArray = new JSONArray(result);
                for (int i = 0; i < questionsArray.length(); i++) {
                    remainingQuestions.add(questionsArray.getJSONObject(i));
                }
                loadNewQuestion();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(TestFamily.this, "Error parsing the question data.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(TestFamily.this, "Error: No data found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNewQuestion() {
        if (remainingQuestions.size() > 0) {
            Random rand = new Random();
            int randomIndex = rand.nextInt(remainingQuestions.size());
            JSONObject currentQuestion = remainingQuestions.get(randomIndex);
            remainingQuestions.remove(randomIndex);

            try {
                String question = currentQuestion.getString("question");
                JSONArray optionsArray = currentQuestion.getJSONArray("options");
                int correctAnswerIndex = currentQuestion.getInt("correctAnswer") - 1;

                currentQuestionIndex++;
                questionNumberTextView.setText("Question " + (currentQuestionIndex + 1));
                questionTextView.setText(question);

                optionButton1.setText(optionsArray.getString(0));
                optionButton2.setText(optionsArray.getString(1));
                optionButton3.setText(optionsArray.getString(2));
                optionButton4.setText(optionsArray.getString(3));

                optionButton1.setTag(correctAnswerIndex == 0);
                optionButton2.setTag(correctAnswerIndex == 1);
                optionButton3.setTag(correctAnswerIndex == 2);
                optionButton4.setTag(correctAnswerIndex == 3);

                startCountdownTimer();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(TestFamily.this, "Error loading new question.", Toast.LENGTH_SHORT).show();
            }
        } else {
            disableButtons();
            showWinningAnimation();
        }
    }

    private void startCountdownTimer() {
        countdownTime = 20;
        countdownTextView.setText(String.valueOf(countdownTime));
        if (timerHandler != null) {
            timerHandler.removeCallbacks(countdownRunnable);
        }

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                countdownTime--;
                countdownTextView.setText(String.valueOf(countdownTime));

                if (countdownTime <= 0) {
                    loadNewQuestion();
                } else {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        timerHandler = new Handler();
        timerHandler.postDelayed(countdownRunnable, 1000);
    }

    private void checkAnswer(Button selectedButton) {
        boolean isCorrect = (boolean) selectedButton.getTag();
        int originalColor = selectedButton.getBackgroundTintList().getDefaultColor();

        if (isCorrect) {
            score++;
            scoreTextView.setText("Score: " + score);
            selectedButton.setBackgroundColor(Color.GREEN);

            checkMarkView.setVisibility(View.VISIBLE);
            xMarkView.setVisibility(View.INVISIBLE);
            if (checkMarkView instanceof AnimationClasses.CheckMarkView) {
                ((AnimationClasses.CheckMarkView) checkMarkView).startAnimation();
            }
        } else {
            selectedButton.setBackgroundColor(Color.RED);

            xMarkView.setVisibility(View.VISIBLE);
            checkMarkView.setVisibility(View.INVISIBLE);
            if (xMarkView instanceof AnimationClasses.XMarkView) {
                ((AnimationClasses.XMarkView) xMarkView).startAnimation();
            }
        }

        new Handler().postDelayed(() -> {
            selectedButton.setBackgroundColor(originalColor);
            checkMarkView.setVisibility(View.INVISIBLE);
            xMarkView.setVisibility(View.INVISIBLE);

            if (checkMarkView instanceof AnimationClasses.CheckMarkView) {
                ((AnimationClasses.CheckMarkView) checkMarkView).reset();
            }
            if (xMarkView instanceof AnimationClasses.XMarkView) {
                ((AnimationClasses.XMarkView) xMarkView).reset();
            }

            if (remainingQuestions.isEmpty()) {
                stopCountdownTimer();
                disableButtons();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    DatabaseReference userScoreRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(user.getUid())
                            .child("scores")
                            .child("family");

                    userScoreRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Integer currentHighScore = task.getResult().getValue(Integer.class);
                            if (currentHighScore == null || score > currentHighScore) {
                                userScoreRef.setValue(score);
                                loadHighScore();
                            }
                        }
                    });
                }

                if (score > 5) {
                    showWinningAnimation();
                } else {
                    Toast.makeText(this, "Quiz finished! Your score: " + score, Toast.LENGTH_LONG).show();
                }
            } else {
                loadNewQuestion();
            }
        }, 300);
    }

    private void showWinningAnimation() {
        winningAnimationView.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            winningAnimationView.setVisibility(View.GONE);
            Toast.makeText(this, "Congratulations! You've completed the quiz with a high score!", Toast.LENGTH_LONG).show();
        }, 3000);
    }

    private void disableButtons() {
        optionButton1.setEnabled(false);
        optionButton2.setEnabled(false);
        optionButton3.setEnabled(false);
        optionButton4.setEnabled(false);
    }

    private void stopCountdownTimer() {
        if (timerHandler != null) {
            timerHandler.removeCallbacks(countdownRunnable);
            countdownTime = 0;
            countdownTextView.setText(String.valueOf(countdownTime));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (winningAnimationView != null) {
            winningAnimationView.stopAnimation();
        }
        stopCountdownTimer();
    }
}