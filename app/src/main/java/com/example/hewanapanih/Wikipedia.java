package com.example.hewanapanih;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class Wikipedia extends AppCompatActivity {

    private String animalName; // Animal name
    private TextView textViewDescription; // Animal description
    private ImageView imageDisplay; // Displayed image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wikipedia);

        Intent intent = getIntent();
        Bitmap imageWikipedia = MainActivity.bitmapImage;

        // Receive animal name from MainActivity
        animalName = intent.getExtras().getString("animalname");
        String url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + animalName;

        textViewDescription = findViewById(R.id.textViewWikipedia);
        textViewDescription.setMovementMethod(new ScrollingMovementMethod());
        volleyJsonObjectRequest(url);

        imageDisplay = findViewById(R.id.imageViewWikipedia);
        if (imageWikipedia != null) imageDisplay.setImageBitmap(imageWikipedia);
    }

    // -------------------- wiki --------------------
    public void volleyJsonObjectRequest(String url) {

        String REQUEST_TAG = " com.androidtutorialpoint.volleyJsonObjectRequest";

        JsonObjectRequest jsonObjectReq = new JsonObjectRequest(url, null,
                response -> {
                    //response.toString());
                    String text = null;
                    try {
                        text = response.getString("extract");
                        textViewDescription.setText(text);
                        textViewDescription.append("\n\nPowered by wikipedia.org");
                    } catch (JSONException e) {
                        textViewDescription.setText("Sorry. No information found about " + animalName + " on wikipedia.org");
                    }
                }, error -> {
            VolleyLog.d("Error: " + error.getMessage());
            textViewDescription.setText("Sorry. No information found about " + animalName + " on wikipedia.org");
        });

        // Adding JsonObject request to request queue
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectReq, REQUEST_TAG);

    }
}