package net.keithlantz.interactivestory.utilities;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by keith on 12/8/17.
 */

public final class Network {
    private static String getPost(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private static JSONObject doPost(String url_string, HashMap<String, String> params) throws IOException, JSONException {
        String post_string = getPost(params);
        Log.d("POST", post_string);

        URL url = new URL(url_string);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(post_string.getBytes().length));
        //conn.setReadTimeout(10000);
        //conn.setConnectTimeout(15000);
        conn.setUseCaches(false);

        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
        os.write(post_string.getBytes());//StandardCharsets.UTF_8));
        //os.flush();
        os.close();

//        conn.connect();

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();
        //System.out.println("JSON: " + jsonString);
        Log.d("POST RESULT", jsonString);
        return new JSONObject(jsonString);
    }

    public static JSONObject post(String url_string, HashMap<String, String> params) {
        JSONObject res = null;
        try {
            res = doPost(url_string, params);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return res;
    }
}
