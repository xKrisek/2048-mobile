package com.example.a2048_mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    int[][] game_board = new int[4][4];
    int moves = 0;

    TextView blockTemplate, blockNoneTemplate, points, best_score, moves_count;
    GridLayout game_grid;

    Context context;

    int score = 0;
    int bestScore = 0;
    private static final String PREFS_NAME = "com.example.a2048_mobile.prefs";
    private static final String PREF_LAST_STATE_JSON = "last_state_json";
    SharedPreferences prefs;

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        for(int i = 0; i < 4; i++)
            for(int j = 0; j < 4; j++)
                game_board[i][j] = 0;

        blockTemplate = findViewById(R.id.block_template);
        blockNoneTemplate = findViewById(R.id.block_none_template);
        points = findViewById(R.id.points);
        best_score = findViewById(R.id.best_score);
        moves_count = findViewById(R.id.moves_count);
        context = this;

        resetScore();

        game_grid = findViewById(R.id.game_grid);
        GestureDetector gestureDetector = new GestureDetector(this, new SwipeGestureListener());
        game_grid.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        boolean loaded = loadLastState();
        if (!loaded) {
            generateRandomTile();
            generateRandomTile();
            updateBoard();
            saveCurrentState();
        } else {
            updateBoard();
            if (isGameOver()) {
                resetGame();
            }
        }
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) { return true; }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            try {
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) onSwipeRight(); else onSwipeLeft();
                        return true;
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) onSwipeDown(); else onSwipeUp();
                        return true;
                    }
                }
            } catch (Exception ignored) {}
            return false;
        }
    }

    private int generateRandomTile(){
        int random_number = (Math.random() < 0.8) ? 2 : 4;
        int range_x = game_board.length;
        int range_y = game_board[0].length;
        int random_x, random_y;
        do {
            random_x = (int)(Math.random() * range_x);
            random_y = (int)(Math.random() * range_y);
        } while(game_board[random_x][random_y] != 0);
        game_board[random_x][random_y] = random_number;
        return random_number;
    }

    private void onSwipeLeft() {
        boolean changed = false;
        for (int i = 0; i < game_board.length; i++) {
            int[] original = java.util.Arrays.copyOf(game_board[i], game_board[i].length);
            List<Integer> compressed = new ArrayList<>();
            for (int value : original) if (value != 0) compressed.add(value);
            for (int j = 0; j < compressed.size() - 1; j++) {
                if (compressed.get(j).equals(compressed.get(j + 1))) {
                    int merged = compressed.get(j) * 2;
                    compressed.set(j, merged);
                    compressed.remove(j + 1);
                    addToScore(merged);
                }
            }
            while (compressed.size() < 4) compressed.add(0);
            int[] newRow = compressed.stream().mapToInt(Integer::intValue).toArray();
            game_board[i] = newRow;
            if (!java.util.Arrays.equals(game_board[i], original)) changed = true;
        }
        if (changed) {
            generateRandomTile();
            updateBoard();
            moves++;
            if (moves_count != null) moves_count.setText(String.valueOf(moves));
            if (isGameOver()) onGameOver(); else {
                saveCurrentState();
                checkWin();
            }
        }
    }

    private void onSwipeRight() {
        boolean changed = false;
        for (int i = 0; i < game_board.length; i++) {
            int[] original = java.util.Arrays.copyOf(game_board[i], game_board[i].length);
            List<Integer> compressed = new ArrayList<>();
            for (int value : original) if (value != 0) compressed.add(value);
            for (int j = compressed.size()-1; j > 0; j--) {
                if (compressed.get(j-1).equals(compressed.get(j))) {
                    int merged = compressed.get(j) * 2;
                    compressed.set(j, merged);
                    compressed.remove(j - 1);
                    addToScore(merged);
                    j-=1;
                }
            }
            while (compressed.size() < 4) compressed.add(0, 0);
            int[] newRow = compressed.stream().mapToInt(Integer::intValue).toArray();
            game_board[i] = newRow;
            if (!java.util.Arrays.equals(game_board[i], original)) changed = true;
        }
        if (changed) {
            generateRandomTile();
            updateBoard();
            moves++;
            if (moves_count != null) moves_count.setText(String.valueOf(moves));
            if (isGameOver()) onGameOver(); else {
                saveCurrentState();
                checkWin();
            }
        }
    }

    private void onSwipeUp() {
        boolean changed = false;
        for (int j = 0; j < game_board.length; j++) {
            int[] originalColumn = new int[game_board.length];
            for(int i = 0; i < game_board.length; i++) originalColumn[i] = game_board[i][j];
            List<Integer> compressed = new ArrayList<>();
            for (int value : originalColumn) if (value != 0) compressed.add(value);
            for (int i = 0; i < compressed.size()-1; i++) {
                if (compressed.get(i).equals(compressed.get(i + 1))) {
                    int merged = compressed.get(i) * 2;
                    compressed.set(i, merged);
                    compressed.remove(i + 1);
                    addToScore(merged);
                }
            }
            while (compressed.size() < 4) compressed.add(0);
            int[] newColumn = compressed.stream().mapToInt(Integer::intValue).toArray();
            for(int i = 0; i < game_board.length; i++) game_board[i][j] = newColumn[i];
            if (!java.util.Arrays.equals(newColumn, originalColumn)) changed = true;
        }
        if (changed) {
            generateRandomTile();
            updateBoard();
            moves++;
            if (moves_count != null) moves_count.setText(String.valueOf(moves));
            if (isGameOver()) onGameOver(); else {
                saveCurrentState();
                checkWin();
            }
        }
    }

    private void onSwipeDown() {
        boolean changed = false;
        for (int j = 0; j < game_board.length; j++) {
            int[] originalColumn = new int[game_board.length];
            for(int i = 0; i < game_board.length; i++) originalColumn[i] = game_board[i][j];
            List<Integer> compressed = new ArrayList<>();
            for (int value : originalColumn) if (value != 0) compressed.add(value);
            for (int i = compressed.size()-1; i > 0; i--) {
                if (compressed.get(i).equals(compressed.get(i - 1))) {
                    int merged = compressed.get(i) * 2;
                    compressed.set(i, merged);
                    compressed.remove(i - 1);
                    addToScore(merged);
                    i--;
                }
            }
            while (compressed.size() < 4) compressed.add(0, 0);
            int[] newColumn = compressed.stream().mapToInt(Integer::intValue).toArray();
            for(int i = 0; i < game_board.length; i++) game_board[i][j] = newColumn[i];
            if (!java.util.Arrays.equals(newColumn, originalColumn)) changed = true;
        }
        if (changed) {
            generateRandomTile();
            updateBoard();
            moves++;
            if (moves_count != null) moves_count.setText(String.valueOf(moves));
            if (isGameOver()) onGameOver(); else {
                saveCurrentState();
                checkWin();
            }
        }
    }

    public void updateBoard() {
        game_grid.removeAllViews();
        game_grid.setColumnCount(4);
        game_grid.setRowCount(4);
        for (int[] rows : game_board) {
            for (int value : rows) {
                if (value != 0) generate_2048_block(context, blockTemplate, value);
                else generate_2048_block(context, blockNoneTemplate, value);
            }
        }
    }

    public void generate_2048_block(Context context, TextView blockTemplate, int value) {
        GridLayout grid = findViewById(R.id.game_grid);
        TextView block = new TextView(context);
        android.view.ViewGroup.LayoutParams origLp = blockTemplate.getLayoutParams();
        GridLayout.LayoutParams newLp;
        if (origLp instanceof GridLayout.LayoutParams) newLp = new GridLayout.LayoutParams((GridLayout.LayoutParams) origLp);
        else {
            int w = (origLp != null && origLp.width > 0) ? origLp.width : dpToPx(80);
            int h = (origLp != null && origLp.height > 0) ? origLp.height : dpToPx(80);
            newLp = new GridLayout.LayoutParams();
            newLp.width = w;
            newLp.height = h;
            if (origLp instanceof android.view.ViewGroup.MarginLayoutParams) {
                android.view.ViewGroup.MarginLayoutParams mlp = (android.view.ViewGroup.MarginLayoutParams) origLp;
                newLp.setMargins(mlp.leftMargin, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
            }
        }
        block.setLayoutParams(newLp);
        block.setGravity(blockTemplate.getGravity());
        block.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 36);
        block.setBackground(blockTemplate.getBackground());
        block.setTextColor(blockTemplate.getTextColors().getDefaultColor());
        block.setTypeface(blockTemplate.getTypeface(), blockTemplate.getTypeface().getStyle());
        block.setText(value==0 ? "" : String.valueOf(value));
        grid.addView(block);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private boolean isGameOver() {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (game_board[i][j] == 0) return false;
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 3; j++)
                if (game_board[i][j] == game_board[i][j+1] || game_board[j][i] == game_board[j+1][i]) return false;
        return true;
    }

    private void onGameOver() {
        prefs.edit().remove(PREF_LAST_STATE_JSON).apply();
        Toast.makeText(context, "Game Over! Score: " + score + " Best: " + bestScore, Toast.LENGTH_LONG).show();
    }

    private boolean loadLastState() {
        String jsonStr = prefs.getString(PREF_LAST_STATE_JSON, null);
        if (jsonStr == null) return false;
        try {
            JSONObject state = new JSONObject(jsonStr);
            JSONArray board = state.getJSONArray("board");
            for (int i = 0; i < 4; i++) {
                JSONArray row = board.getJSONArray(i);
                for (int j = 0; j < 4; j++) game_board[i][j] = row.getInt(j);
            }
            score = state.getInt("score");
            moves = state.getInt("moves");
            bestScore = state.getInt("bestScore");
            if (points != null) points.setText(String.valueOf(score));
            if (moves_count != null) moves_count.setText(String.valueOf(moves));
            if (best_score != null) best_score.setText(String.valueOf(bestScore));
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveCurrentState() {
        try {
            JSONObject state = new JSONObject();
            JSONArray board = new JSONArray();
            for (int i = 0; i < 4; i++) {
                JSONArray row = new JSONArray();
                for (int j = 0; j < 4; j++) row.put(game_board[i][j]);
                board.put(row);
            }
            state.put("board", board);
            state.put("score", score);
            state.put("moves", moves);
            state.put("bestScore", bestScore);
            prefs.edit().putString(PREF_LAST_STATE_JSON, state.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addToScore(int delta) {
        if (delta <= 0) return;
        score += delta;
        if (score > bestScore) {
            bestScore = score;
            if (best_score != null) best_score.setText(String.valueOf(bestScore));
        }
        if (points != null) points.setText(String.valueOf(score));
    }

    private void resetScore() {
        score = 0;
        if (points != null) points.setText(String.valueOf(score));
        moves = 0;
        if (moves_count != null) moves_count.setText(String.valueOf(moves));
    }

    private void resetGame() {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                game_board[i][j] = 0;
        resetScore();
        generateRandomTile();
        generateRandomTile();
        updateBoard();
        saveCurrentState();
    }

    private void checkWin() {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (game_board[i][j] == 2048) {
                    Toast.makeText(context, "Wygrales, stworzyles bloczek o wartosci 2048!", Toast.LENGTH_LONG).show();
                    return;
                }
    }
}
