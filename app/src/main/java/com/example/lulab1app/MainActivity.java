package com.example.lulab1app;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // shown cat running gif image
        Resources res = getResources();
        Drawable drawable = ResourcesCompat.getDrawable(res, R.drawable.runningcat, null);

        setContentView(R.layout.activity_main);

        // Ref: https://codinginflow.com/tutorials/android/open-activity-on-button-click
        // Start AR Game, switch page to game activity
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });
    }

    public void startGame() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
}