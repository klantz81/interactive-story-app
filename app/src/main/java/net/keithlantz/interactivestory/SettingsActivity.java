package net.keithlantz.interactivestory;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Vector;

/**
 * Created by keith on 12/9/17.
 */

public class SettingsActivity extends AppCompatActivity {

    private ChoicesDbHelper choicesDb;
    private MediaPlayer mediaClick = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaClick.stop();
        mediaClick.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mediaClick.stop();
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        choicesDb = new ChoicesDbHelper(this);
        mediaClick = MediaPlayer.create(this, R.raw.click);

        ImageView settings = (ImageView)findViewById(R.id.back);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaClick.start();
                onBackPressed();
            }
        });

        final Button resetButton = (Button)findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaClick.start();

                AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
                alert.setTitle("Reset Story");
                alert.setMessage("Are you sure you want to reset the story?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reset();
                        resetButton.setText("Story has been reset.");
                    }
                });

                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alert.show();


//                reset();
            }
        });
    }

    private void reset() {
        choicesDb.truncate(choicesDb.getWritableDatabase());
    }
}