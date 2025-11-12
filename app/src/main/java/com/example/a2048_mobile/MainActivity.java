package com.example.a2048_mobile;

import android.content.Context;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    TextView blockTemplate;
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


//            <TextView
//        android:layout_width="80dp"
//        android:layout_height="80dp"
//        android:layout_gravity="center"
//        android:gravity="center"
//        android:text="0"
//        android:textSize="24sp"
//        android:background="@color/white"
//        android:layout_margin="2dp"/>
        for (int i = 0; i < 4; i++){
            generate_2048_block(this);
        }
    }

    private void generate_2048_block(Context context) {
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
        block.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 24); // z jednostką
        block.setBackground(blockTemplate.getBackground());
        try {
            block.setTextColor(blockTemplate.getTextColors().getDefaultColor());
        } catch (Exception ignored) {}

        int number = (Math.random() < 0.6) ? 2 : 4;
        block.setText(String.valueOf(number));

        grid.addView(block);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}