package com.example.hewanapanih;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageDisplay; // Displayed Image
    private TextView textPrediction, tvNama; // Predicted names


    private final int REQUEST_IMAGE_CAPTURE = 1, REQUEST_IMAGE_GALLERY = 2; // REQUESTS
    private String namaHewan;  // Predicted animal name
    public static Bitmap bitmapImage; // User image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        namaHewan = "animal";
        setViews();
        showDialogNama();
    }

    private void setViews() {
        imageDisplay = findViewById(R.id.imageView);
        findViewById(R.id.imageButtonCamera).setOnClickListener(this);
        findViewById(R.id.imageButtonGallery).setOnClickListener(this);
        findViewById(R.id.imageButtonDelete).setOnClickListener(this);
        findViewById(R.id.imageButtonInfo).setOnClickListener(this);
        textPrediction = findViewById(R.id.textViewPrediction);
        tvNama = findViewById(R.id.tvNama);
    }

    private void showDialogNama() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_form_nama, null);
        dialog.setView(dialogView);

        EditText etNama = dialogView.findViewById(R.id.etNama);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String s = "Hai, " + etNama.getText().toString();
            tvNama.setText(s);
            dialog.dismiss();
        });

        dialog.show();
    }

    // -------------------- onClick Events --------------------
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.imageButtonCamera) {
            Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (iCamera.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(iCamera, REQUEST_IMAGE_CAPTURE);
            }
        } else if (view.getId() == R.id.imageButtonGallery) {
            Intent iGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            iGallery.setType("image/*");
            startActivityForResult(iGallery, REQUEST_IMAGE_GALLERY);
        } else if (view.getId() == R.id.imageButtonDelete) {
            imageDisplay.setImageBitmap(null);
            namaHewan = "animal";
            textPrediction.setText("");
        } else if (view.getId() == R.id.imageButtonInfo) {
            // Wikipedia Support
            // Change Image bitmap to Byte Array (Better format to send between activities
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (bitmapImage != null)
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            Intent intent = new Intent(this, Wikipedia.class);
            intent.putExtra("animalname", namaHewan); // add animal name to new activity
            startActivity(intent);
        }
    }

    // -------------------- onActivity Result --------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Firebase Vision Image
            InputImage inputImage;
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                //PHOTO FROM CAMERA
                bitmapImage = (Bitmap) data.getExtras().get("data");
                imageDisplay.setImageBitmap(bitmapImage);
                bitmapImage = resizeImage(bitmapImage);
                inputImage = InputImage.fromBitmap(bitmapImage, 0);
                textPrediction.setText("Loading...");
                labelImagesCloud(inputImage);
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                //PHOTO FROM GALLERY
                Uri uri = data.getData();
                bitmapImage = null;
                try {
                    bitmapImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imageDisplay.setImageBitmap(bitmapImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageDisplay.setImageBitmap(bitmapImage);
                bitmapImage = resizeImage(bitmapImage);
                inputImage = InputImage.fromBitmap(bitmapImage, 0);
                textPrediction.setText("Loading...");
                labelImagesCloud(inputImage);
            }
        }
    }

    private Bitmap resizeImage(Bitmap image) {
        float aspectRatio = image.getWidth() /
                (float) image.getHeight();
        int width = 480;
        int height = Math.round(width / aspectRatio);

        image = Bitmap.createScaledBitmap(
                image, width, height, false);

        return image;
    }

    private void labelImagesCloud(InputImage image) {
        // [START set_detector_options_cloud]
        ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.8f)
                .build();
        // [END set_detector_options_cloud]

        // [START get_detector_cloud]
        ImageLabeler labeler = ImageLabeling.getClient(options);
        // [END get_detector_cloud]
        // [START run_detector_cloud]
        labeler.process(image)
                .addOnSuccessListener(imageLabels -> {
                    namaHewan = imageLabels.get(0).getText();
                    textPrediction.setText("");
                    for (ImageLabel label : imageLabels) {
                        String text = label.getText();
                        float confidence = label.getConfidence();
                        textPrediction.append(text + " " + String.format(Locale.getDefault(), "%.3f", confidence) + "\n");
                    }
                })
                .addOnFailureListener(e -> {
                    textPrediction.setText("An unsuccessful attempt to connect to the server. Check your Internet connection.");
                });
        // [END run_detector_cloud]
    }
}