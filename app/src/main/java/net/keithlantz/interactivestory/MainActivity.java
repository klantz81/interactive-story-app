package net.keithlantz.interactivestory;

import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import net.keithlantz.interactivestory.utilities.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    final static private int OPERATION_LOGIN_LOADER = 100;
    final static private String OPERATION_LOGIN_EMAIL = "email";
    final static private String OPERATION_LOGIN_PASSWORD = "password";
    Bundle queryBundle = null;
    private LoaderManager loaderManager = null;
    private MediaPlayer mediaPlayer = null;
    private MediaPlayer mediaClick = null;

    private JSONObject story;
    private LinearLayout c;
    private ScrollView scroll;
    private Typeface tf;
    private ChoicesDbHelper choicesDb;
    private static class ChoiceObject {
        public String choice_id, choice_text;
        public ChoiceObject(String id, String text) {
            choice_id = id;
            choice_text = text;
        }
    }
    private Vector<ChoiceObject> choices;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaClick.release();
        mediaPlayer.release();
        Log.d("STORY", "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mediaClick.pause();
        mediaPlayer.pause();
    }

    /*@Override
    protected void onStart() {
        super.onStart();
//        mediaClick.start();
        mediaPlayer.start();
    }*/

    @Override
    protected void onResume() {
        super.onResume();

        mediaPlayer.start();

        c.removeAllViews();

        choices = new Vector<ChoiceObject>();
        loadChoices();

        loaderManager.restartLoader(OPERATION_LOGIN_LOADER, queryBundle, new FetchLoader());
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = MediaPlayer.create(this, R.raw.theyre_here_looping);
        mediaClick = MediaPlayer.create(this, R.raw.click);

        mediaPlayer.setLooping(true);
        //mediaPlayer.start();

        choicesDb = new ChoicesDbHelper(this);
        choices = new Vector<ChoiceObject>();
        loadChoices();
        for (ChoiceObject o : choices) {
            Log.d("CHOICES", o.choice_id+","+o.choice_text);
        }

        c = (LinearLayout)findViewById(R.id.story_container);
        c.removeAllViews();

        scroll = (ScrollView)findViewById(R.id.scroll);

        tf = Typeface.createFromAsset(getAssets(), "font/crimson_text.ttf");

        queryBundle = new Bundle();
        queryBundle.putString(OPERATION_LOGIN_EMAIL, "");
        queryBundle.putString(OPERATION_LOGIN_PASSWORD, "");

        loaderManager = getSupportLoaderManager();
        Loader<String> loader = loaderManager.getLoader(OPERATION_LOGIN_LOADER);

        if (loader == null) {
            loaderManager.initLoader(OPERATION_LOGIN_LOADER, queryBundle, new FetchLoader());
        } else {
            loaderManager.restartLoader(OPERATION_LOGIN_LOADER, queryBundle, new FetchLoader());
        }

        ImageView settings = (ImageView)findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaClick.start();
                settings();


                /*

                choicesDb.truncate(choicesDb.getWritableDatabase());
                choices = new Vector<ChoiceObject>();
                c.removeAllViews();

                loaderManager.restartLoader(OPERATION_LOGIN_LOADER, queryBundle, new FetchLoader());
                */
            }
        });
    }

    private void init() {

    }

    private void settings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void saveChoice(String id, String text) {
        SQLiteDatabase db = choicesDb.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ChoicesContract.ChoicesEntry.COLUMN_NAME_CHOICE_ID, id);
        values.put(ChoicesContract.ChoicesEntry.COLUMN_NAME_CHOICE_TEXT, text);
        values.put(ChoicesContract.ChoicesEntry.COLUMN_NAME_TIMESTAMP, "CURRENT_TIMESTAMP");

        long newRowId = db.insert(ChoicesContract.ChoicesEntry.TABLE_NAME, null, values);
    }

    private void loadChoices() {
        SQLiteDatabase db = choicesDb.getReadableDatabase();

        String[] projection = {
                ChoicesContract.ChoicesEntry._ID,
                ChoicesContract.ChoicesEntry.COLUMN_NAME_CHOICE_ID,
                ChoicesContract.ChoicesEntry.COLUMN_NAME_CHOICE_TEXT,
                ChoicesContract.ChoicesEntry.COLUMN_NAME_TIMESTAMP
        };

        //String selection = ChoicesContract.ChoicesEntry.COLUMN_NAME_TITLE + " = ?";
        //String[] selectionArgs = { "My Title" };

        String sortOrder = ChoicesContract.ChoicesEntry._ID + " ASC";

        Cursor cursor = db.query(
                ChoicesContract.ChoicesEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while(cursor.moveToNext()) {
            String choice_id = cursor.getString(cursor.getColumnIndexOrThrow(ChoicesContract.ChoicesEntry.COLUMN_NAME_CHOICE_ID));
            String choice_text = cursor.getString(cursor.getColumnIndexOrThrow(ChoicesContract.ChoicesEntry.COLUMN_NAME_CHOICE_TEXT));
            choices.add(new ChoiceObject(choice_id, choice_text));
        }
        cursor.close();

    }

    private void addTitle(String title, String author) {
        View child = getLayoutInflater().inflate(R.layout.title_text, null);

        TextView tv = (TextView)child.findViewById(R.id.title_text);
        TextView av = (TextView)child.findViewById(R.id.author_text);
        tv.setTypeface(tf);
        av.setTypeface(tf);
        tv.setText(title);
        av.setText("by "+author);

        c.addView(child);
    }

    private void renderTitle() {
        try {
            String t = story.getString("title");
            String a = story.getString("author");
            addTitle(t, a);

        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void activate(String id, String text, boolean skip_save, boolean skip_choice, boolean skip_animate) {
        if (!skip_save)
            saveChoice(id, text);

        try {
            JSONObject tt = story.getJSONObject("cards");
            JSONObject t = tt.getJSONObject(id);

            String st = t.getString("text");
            addStoryText(st, skip_animate);

            if (!skip_choice) {
                JSONArray a = t.getJSONArray("choices");
                JSONObject c0 = a.getJSONObject(0);
                String t0 = c0.getString("text");
                String i0 = c0.getString("id");
                JSONObject c1 = a.getJSONObject(1);
                String t1 = c1.getString("text");
                String i1 = c1.getString("id");
                addChoice(t0, i0, t1, i1);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

    }

    private void addStoryText(String text, boolean skip_animate) {
        final int y = c.getHeight();

        View child = getLayoutInflater().inflate(R.layout.story_text, null);
        TextView tv = (TextView) child.findViewById(R.id.story_text);

        tv.setTypeface(tf);

        tv.setText(text);
        if (!skip_animate)
            child.setAlpha(0);
        c.addView(child);

        if (!skip_animate) {
            ObjectAnimator.ofInt(scroll, "scrollY", y - 400).setDuration(500).start();
            ObjectAnimator.ofFloat(child, "alpha", 1).setDuration(1000).start();
        }
    }

    private void addChoiceText(String text) {
        View child = getLayoutInflater().inflate(R.layout.choice_text, null);
        TextView tv = (TextView)child.findViewById(R.id.choice_text);
        tv.setText(text);
        c.addView(child);
    }

    private void addChoice(final String text0, final String id0, final String text1, final String id1) {
        final View child = getLayoutInflater().inflate(R.layout.choice, null);

        Button b0 = (Button) child.findViewById(R.id.choice0);
        b0.setText(text0);
        b0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaClick.start();
                c.removeView(child);
                addChoiceText(text0);
                activate(id0, text0, false, false, false);
            }
        });

        Button b1 = (Button) child.findViewById(R.id.choice1);
        b1.setText(text1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaClick.start();
                c.removeView(child);
                addChoiceText(text1);
                activate(id1, text1, false, false, false);
            }
        });

        if (id0.length() < 1)
            b0.setVisibility(View.INVISIBLE);

        if (id1.length() < 1)
            b1.setVisibility(View.INVISIBLE);

        c.addView(child);
    }



    private static class Data {
        JSONObject obj;
        Bitmap bm;

        public Data() {
            obj = null;
            bm = null;
        }
    };

    private static class FetchTask extends AsyncTaskLoader<Data> {
        Bundle bundle;

        public FetchTask(Context context, Bundle args) {
            super(context);
            bundle = args;
        }

        @Override
        public Data loadInBackground() {
            HashMap<String,String> params = new HashMap<>();
            //params.put("action", "get-story");
            params.put("a", "json");
            //params.put("email", bundle.getString(OPERATION_LOGIN_EMAIL));
            //params.put("password", bundle.getString(OPERATION_LOGIN_PASSWORD));


            Data result = new Data();
            result.obj = Network.post("http://192.168.1.103/story/", params);

            return result;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }

    private class FetchLoader implements LoaderManager.LoaderCallbacks<Data> {

        @Override
        public Loader<Data> onCreateLoader(int id, Bundle args) {
            return new FetchTask(getApplicationContext(), args);
        }

        @Override
        public void onLoadFinished(Loader<Data> loader, Data data) {
            loaderManager.destroyLoader(loader.getId());

            story = data.obj;
            if (story == null) {
                //TODO use stored story
                story = new JSONObject();
            } else {
                //TODO save story
            }

            renderTitle();

            String start = "start";
            try {
                start = story.getString("start");
            } catch(JSONException e) {
                e.printStackTrace();
            }

            if (choices.size() > 0) {
                activate(start, "", true, true, true);

                for (int i = 0; i < choices.size(); i++) {
                    ChoiceObject o = choices.get(i);

                    addChoiceText(o.choice_text);
                    activate(o.choice_id, o.choice_text, true, i != choices.size() - 1, true);
                }

                scroll.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll.fullScroll(View.FOCUS_DOWN);
                    }
                });

            } else {
                activate(start, "", true, false, false);
            }
        }

        @Override
        public void onLoaderReset(Loader<Data> loader) {

        }
    };
}
