package com.example.lulab1app;

import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.widget.Button;
import android.widget.TextView;

import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;

import android.os.Bundle;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    // Ref: https://github.com/heyletscode/Sceneform-Collision-Sample
    private Scene scene;
    private Camera camera;
    private ModelRenderable bulletRenderable;
    private boolean shouldStartTimer = true;
    private int Score = 0;
    private Point point;
    private TextView targetTxt;
    private SoundPool soundPool;
    private int sound;
    private static final String TAG = "MyModel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        point = new Point();
        display.getRealSize(point);

        setContentView(R.layout.activity_game);

        loadSoundPool();

        targetTxt = findViewById(R.id.target);

        LuFragment arFragment =
                (LuFragment) getSupportFragmentManager().findFragmentById(R.id.luarFragment);

        scene = arFragment.getArSceneView().getScene();
        camera = scene.getCamera();

        addCatsToScene();
        buildField();

        Button shoot = findViewById(R.id.CatchButton);

        shoot.setOnClickListener(v -> {
            if (shouldStartTimer) {
                startTimer();
                shouldStartTimer = false;
            }
            capturing();
        });

    }

    private void loadSoundPool() {

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        // cat_meowing.mp3 from http://soundbible.com/1684-Cat-Meowing-2.html
        sound = soundPool.load(this, R.raw.cat_meowing, 1);

    }

    // Capturing cat
    private void capturing() {

        // set screen point
        Ray ray = camera.screenPointToRay(point.x / 2f, point.y / 2f);
        Node node = new Node();
        node.setRenderable(bulletRenderable);
        scene.addChild(node);

        new Thread(() -> {

            for (int i = 0; i < 200; i++) {

                int finalI = i;
                runOnUiThread(() -> {

                    Vector3 vector3 = ray.getPoint(finalI * 0.1f);
                    node.setWorldPosition(vector3);

                    Node nodeInContact = scene.overlapTest(node);

                    // count Caught number
                    if (nodeInContact != null) {
                        Score++;
                        targetTxt.setText("Caught(Total 10):" + Score);
                        scene.removeChild(nodeInContact);
                        soundPool.play(sound, 1f, 1f, 1, 0
                                , 1f);
                    }

                });

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            // reduce shown cats number
            runOnUiThread(() -> scene.removeChild(node));

        }).start();

    }

    // Start Countdown
    private void startTimer() {

        TextView timer = findViewById(R.id.Timer);

        new Thread(() -> {
            // Game is limited to 2 minutes
            int seconds = 120;

            while ( Score <= 10 && seconds > 0 ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Countdown
                seconds--;

                // Show Time
                int minutesPassed = seconds / 60;
                int secondsPassed = seconds % 60;
                runOnUiThread(() -> timer.setText(minutesPassed + ":" + secondsPassed));

                // Game failed
                if (minutesPassed == 0 && secondsPassed == 0 && Score > 0 ) {
                    runOnUiThread(() -> timer.setText("Game Over"));
                }

                // Game win
                if (Score == 10 ) {
                    runOnUiThread(() -> timer.setText("Congratulations"));
                }
            }
        }).start();
    }

    //  ref:  https://developers.google.com/sceneform/reference/com/google/ar/sceneform/rendering/Texture.Builder
    // build catch field
    private void buildField() {
        Texture.builder()
                .setSource(this, R.drawable.texture)
                .build()
                .thenAccept(texture -> {
                    MaterialFactory
                            .makeOpaqueWithTexture(this, texture)
                            .thenAccept(material -> {

                                bulletRenderable = ShapeFactory
                                        .makeSphere(0.01f,
                                                new Vector3(0f, 0f, 0f),
                                                material);
                            });
                });
    }

    // Show cats to Scene
    private void addCatsToScene() {

        ModelRenderable.builder()
                // Ref: https://developers.google.com/sceneform/develop/import-assets#create_the_renderable
                // To load as an asset from the 'assets' folder
                // cat 3d model from https://www.cgtrader.com/items/769433/download-page
                .setSource(this, Uri.parse("CatMac.sfb"))
                .build()
                .thenAccept(renderable -> {

                    // Ref: https://github.com/heyletscode/Sceneform-Collision-Sample
                    for (int i = 0; i < 10;i++) {

                        Node node = new Node();
                        node.setRenderable(renderable);
                        scene.addChild(node);

                        Random random = new Random();
                        int x = random.nextInt(10);
                        int y = random.nextInt(20);
                        int z = random.nextInt(10);
                        z = -z;

                        node.setWorldPosition(new Vector3(
                                (float) x,
                                y / 10f,
                                (float) z
                        ));
//                       Log.d("Random numbers is : ", i , x, y, z );
                    }})
                .exceptionally(
                        throwable -> {
                            Log.e(TAG, "Unable to load Renderable.", throwable);
                            return null;
                        });
    }

}
