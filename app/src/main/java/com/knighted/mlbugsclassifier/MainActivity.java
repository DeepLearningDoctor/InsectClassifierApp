package com.knighted.mlbugsclassifier;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.knighted.mlbugsclassifier.ml.ClassifierInsect;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    Button loadImg;
    TextView tvResult;
    ImageView ivAddimage;
    private ActivityResultLauncher<String> getContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivAddimage= findViewById(R.id.iv_add_image);
        tvResult = findViewById(R.id.tvload);
        loadImg= findViewById(R.id.load_btn);


        // Register for image selection result
        getContent =registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                ivAddimage.setImageURI(result);
            }

            // Create a Bitmap object from the selected image URI
            Bitmap imgBitmap =null;
            try {
                InputStream inputStream = getContentResolver().openInputStream(result);
                imgBitmap = BitmapFactory.decodeStream(inputStream);
                // Use the imgBitmap as needed
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            generate_output(imgBitmap);


        });

        ivAddimage.setOnClickListener(v -> {
            // Launch image selection using custom method
            selectImageFromGallery();
        });

        //Search and confirm the output in text view
        tvResult.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q="
                    + tvResult.getText().toString()));
            startActivity(intent);
        });

        // Load image button
        loadImg.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q="
                    + tvResult.getText().toString()));
            startActivity(intent);
        });

    }

    private void generate_output(Bitmap imgBitmap) {
        try {
            ClassifierInsect model = ClassifierInsect.newInstance(MainActivity.this);

            // Creates inputs for reference.
            TensorImage image = TensorImage.fromBitmap(imgBitmap);

            // Runs model inference and gets result.
            ClassifierInsect.Outputs outputs = model.process(image);
            List<Category> probability = outputs.getProbabilityAsCategoryList();

            int index=0;
            float max=probability.get(0).getScore();

            // Find the index of category with highest score
            for(int i=0;i<probability.size();i++)
                if (max<probability.get(i).getScore()){
                    max= probability.get(i).getScore();
                    index =i;
                }

            // Find the category object
            Category output = probability.get(index);
            // Set the label of category in the text view
            tvResult.setText(output.getLabel());

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    private void selectImageFromGallery() {
        // Call the image selection contract
        getContent.launch("image/*");
    }






}