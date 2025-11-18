package com.example.a2048_mobile;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    TextView blockTemplate, blockNoneTemplate;
    GridLayout game_grid;

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

        game_grid = findViewById(R.id.game_grid);

        for (int i = 0; i < 3; i++){
            generate_2048_block(this, blockNoneTemplate);
            generate_2048_block(this, blockNoneTemplate);
            generate_2048_block(this, blockTemplate);
            generate_2048_block(this, blockNoneTemplate);
            generate_2048_block(this, blockTemplate);
        }
//        try {
//            sleep(2000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        game_grid.removeAllViews();

    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case (MotionEvent.ACTION_DOWN):
                //eoeo
                return true;
            case (MotionEvent.ACTION_MOVE):
                //eoeo1
                return true;
            case (MotionEvent.ACTION_UP):
                //eoeo2
                return true;
            case (MotionEvent.ACTION_CANCEL):
                //eoeo3
                return true;
            case (MotionEvent.ACTION_OUTSIDE):
                //eoeo4
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    public void generate_2048_block(Context context, TextView blockTemplate) {
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

        int number = (Math.random() < 0.8) ? 2 : 4;
        block.setText(String.valueOf(number));

        grid.addView(block);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}