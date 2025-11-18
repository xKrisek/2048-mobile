package com.example.a2048_mobile;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    int[][] game_board = {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};

    boolean changed = false;
    TextView blockTemplate, blockNoneTemplate;
    GridLayout game_grid;

    Context context;

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

        blockTemplate = findViewById(R.id.block_template);
        blockNoneTemplate = findViewById(R.id.block_none_template);
        context = this;

        game_grid = findViewById(R.id.game_grid);
        GestureDetector gestureDetector = new GestureDetector(this, new SwipeGestureListener());
        game_grid.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        for (int i = 0; i < 4; i++){
            generate_2048_block(this, blockTemplate, generateRandomTile());
            generate_2048_block(this, blockTemplate, generateRandomTile());
            generate_2048_block(this, blockNoneTemplate, generateRandomTile());
            generate_2048_block(this, blockNoneTemplate, generateRandomTile());
        }
        String boardStr = java.util.Arrays.deepToString(game_board);
        Toast.makeText(context, boardStr, Toast.LENGTH_LONG).show();
//        try {
//            sleep(2000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        game_grid.removeAllViews();

    }
    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            try {
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        return true;
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeDown();
                        } else {
                            onSwipeUp();
                        }
                        return true;
                    }
                }
            } catch (Exception ignored) {}
            return false;
        }
    }

    private int generateRandomTile(){
        int random_number = (Math.random() < 0.8) ? 2 : 4;

        int random_x_max = game_board.length - 1;
        int random_y_max = game_board[0].length - 1;
        int random_x_min = 0;
        int random_y_min = 0;

        int range_x = random_x_max - random_x_min + 1;
        int range_y = random_y_max - random_y_min + 1;

        int random_x;
        int random_y;

        do{
            random_x = (int)(Math.random() * range_x) + random_x_min;
            random_y = (int)(Math.random() * range_y) + random_y_min;
        } while(game_board[random_x][random_y] != 0);

        game_board[random_x][random_y] = random_number;
        return random_number;
    }

    private void onSwipeLeft() {
        for(int i=0; i<game_board.length; i++){
            int[] copy_row = game_board[i];
            List<Integer> compressed_row_list = new ArrayList<Integer>();
            for (int j : copy_row) {
                if (j != 0) {
                    compressed_row_list.add(j);
                }
            }
            while(compressed_row_list.size()<4){
                compressed_row_list.add(0);
            }
            for(int j=0; j<compressed_row_list.size(); j++){
                if(compressed_row_list.get(i) != 0 && Objects.equals(compressed_row_list.get(i), compressed_row_list.get(i + 1))){
                    compressed_row_list.set(i, compressed_row_list.get(i)*2);
                    compressed_row_list.set(i+1, 0);
                }
            }
            for (int j : copy_row) {
                if (j != 0) {
                    compressed_row_list.add(j);
                }
            }
            while(compressed_row_list.size()<4){
                compressed_row_list.add(0);
            }
            game_board[i] = compressed_row_list.stream().mapToInt(Integer::intValue).toArray();
            if(game_board[i] != copy_row) {
                changed = true;
            }
        }
        updateBoard();
        String boardStr = java.util.Arrays.deepToString(game_board);
        Toast.makeText(context, boardStr, Toast.LENGTH_LONG).show();
//        String boardStr = java.util.Arrays.deepToString(game_board);
//        Toast.makeText(context, boardStr, Toast.LENGTH_LONG).show();
    }

    private void onSwipeRight() {
        Log.d("Swipe", "RIGHT");
    }

    private void onSwipeUp() {
        Log.d("Swipe", "UP");
    }

    private void onSwipeDown() {
        Log.d("Swipe", "DOWN");
    }

    public void updateBoard() {
        game_grid.removeAllViews();
        for (int i = 0; i < game_board.length; i++) {
            for (int j = 0; j < game_board[i].length; j++) {
                int value = game_board[i][j];
                if (value != 0) {
                    generate_2048_block(context, blockTemplate, value);
                } else {
                    generate_2048_block(context, blockNoneTemplate, value);
                }
            }
        }
    }

    public void generate_2048_block(Context context, TextView blockTemplate, int value) {
        GridLayout grid = findViewById(R.id.game_grid);

        TextView block = new TextView(context);

        android.view.ViewGroup.LayoutParams origLp = blockTemplate.getLayoutParams();
        GridLayout.LayoutParams newLp;
        if (origLp instanceof GridLayout.LayoutParams) {
            newLp = new GridLayout.LayoutParams((GridLayout.LayoutParams) origLp);
        } else {
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
        block.setText(String.valueOf(value));

        int number = (Math.random() < 0.8) ? 2 : 4;
        block.setText(String.valueOf(number));

        grid.addView(block);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}