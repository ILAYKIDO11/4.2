package com.example.project1311;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Leaderboards extends AppCompatActivity {

    // The email of the current user
    private String myEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);

        // Get current user's email
        myEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Set up all three leaderboards
        setupLeaderboard("test", R.id.testLeaderboardRecyclerView);
        setupLeaderboard("numbers", R.id.numbersLeaderboardRecyclerView);
        setupLeaderboard("family", R.id.familyLeaderboardRecyclerView);
    }

    // Set up a single leaderboard for a specific category
    private void setupLeaderboard(String category, int viewId) {
        // Create and set up the RecyclerView
        RecyclerView recyclerView = findViewById(viewId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create the adapter
        TopScoresAdapter adapter = new TopScoresAdapter(myEmail);
        recyclerView.setAdapter(adapter);

        // Load the top scores
        loadTopScores(category, adapter);
    }

    // פעולה שמקבלת אחת משלוש הקטגוריות וממיינת את ה-3 הכי גבוהים, יודעים איזה קטגוריה מכיוון שבסטרינג בקריאה מופיע הקטגוריה הנדרשת
    private void loadTopScores(String category, TopScoresAdapter adapter)
    {
        // Get data from Firebase
        FirebaseDatabase.getInstance().getReference("users")
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        List<ScoreEntry> allScores = new ArrayList<>();

                        // Collect all scores for this category
                        for (DataSnapshot user : snapshot.getChildren())
                        {
                            String email = user.child("email").getValue(String.class);
                            Integer score = user.child("scores").child(category).getValue(Integer.class);

                            if (email != null && score != null) {
                                allScores.add(new ScoreEntry(email, score));
                            }
                        }

                        // Sort scores from highest to lowest
                        Collections.sort(allScores, new Comparator<ScoreEntry>()
                        {
                            @Override
                            public int compare(ScoreEntry first, ScoreEntry second)
                            {
                                // Compare the scores
                                if (second.score > first.score) {
                                    return 1;  // second has higher score, put it first
                                } else if (second.score < first.score) {
                                    return -1; // first has higher score, put it first
                                } else {
                                    return 0;  // scores are equal, keep original order
                                }
                            }
                        });

                        // Get only the top 3 scores
                        List<ScoreEntry> topScores;
                        if (allScores.size() > 3) {
                            // If we have more than 3 scores, take only the first 3
                            topScores = allScores.subList(0, 3);
                        } else {
                            // Otherwise use all available scores
                            topScores = allScores;
                        }

                        // Update the display
                        adapter.setScores(topScores);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Leaderboards.this, "Could not load scores", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Simple class to hold a user's score
    static class ScoreEntry {
        String email;
        int score;

        ScoreEntry(String email, int score) {
            this.email = email;
            this.score = score;
        }
    }

    // Adapter to display scores in the RecyclerView
    static class TopScoresAdapter extends RecyclerView.Adapter<TopScoresAdapter.ScoreViewHolder> {

        private List<ScoreEntry> scores = new ArrayList<>();
        private final String myEmail;
        private final String[] MEDAL_COLORS = {"#FFD700", "#C0C0C0", "#CD7F32"}; // Gold, Silver, Bronze

        TopScoresAdapter(String myEmail) {
            this.myEmail = myEmail;
        }

        void setScores(List<ScoreEntry> scores) {
            this.scores = scores;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false);
            return new ScoreViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
            ScoreEntry entry = scores.get(position);

            // Set rank (1st, 2nd, 3rd)
            holder.rank.setText(String.valueOf(position + 1));

            // Set medal color
            String medalColor = position < MEDAL_COLORS.length ? MEDAL_COLORS[position] : "#A9A9A9";
            holder.rank.setBackgroundColor(Color.parseColor(medalColor));

            // Set username (from email)
            String username = entry.email.split("@")[0];
            boolean isMe = entry.email.equals(myEmail);
            holder.username.setText(isMe ? username + " (You)" : username);

            // Set score
            holder.score.setText(String.valueOf(entry.score));

            // Highlight if this is the current user
            if (isMe) {
                holder.itemView.setBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue
                holder.username.setTextColor(Color.parseColor("#1565C0"));       // Darker blue
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor("#F8F9FA")); // Light gray
                holder.username.setTextColor(Color.parseColor("#263238"));       // Dark gray
            }
        }

        @Override
        public int getItemCount() {
            return scores.size();
        }

        // ViewHolder for each score entry
        static class ScoreViewHolder extends RecyclerView.ViewHolder {
            TextView rank, username, score;

            ScoreViewHolder(View view) {
                super(view);
                rank = view.findViewById(R.id.rankTextView);
                username = view.findViewById(R.id.usernameTextView);
                score = view.findViewById(R.id.scoreTextView);
            }
        }
    }
}