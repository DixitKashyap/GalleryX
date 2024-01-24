package com.dixitkumar.galleryxapp.PhotosFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dixitkumar.galleryxapp.ImageViewerActivity;
import com.dixitkumar.galleryxapp.MainActivity;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.ActivityTextTranslatorBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;

public class TextTranslatorActivity extends AppCompatActivity {
    private static boolean IS_EDITABLE = false;
    private Uri imageUri;
    private Uri uri;
    private ActivityTextTranslatorBinding textTranslatorBinding;

    private static String LANGUAGE_CODE = "";
    //For Recognition of Text From Picture
    private TextRecognizer textRecognizer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textTranslatorBinding = ActivityTextTranslatorBinding.inflate(getLayoutInflater());
        setTheme(R.style.CameraTheme);
        setContentView(textTranslatorBinding.getRoot());

        //Receiving The Image Uri
        Intent i = getIntent();
        String str = i.getStringExtra("IMAGE_URI");
        imageUri = Uri.fromFile(new File(str));
        CropImage.activity(imageUri).start(TextTranslatorActivity.this);

        textRecognizer = TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build());


        //Translate Text
        textTranslatorBinding.translateButton.setOnClickListener(view -> {
            translateText();
        });


        //Clear Text
        textTranslatorBinding.clearAllButton.setOnClickListener(view ->{
            textTranslatorBinding.extractedTextInfo.setText("");
        });

        //Copy Text
        textTranslatorBinding.copyButton.setOnClickListener(view -> {
            ClipboardManager  clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if(TextUtils.isEmpty(textTranslatorBinding.extractedTextInfo.getText().toString())){
                Toast.makeText(this, "Empty Field", Toast.LENGTH_SHORT).show();
            }else{
                String content = "";
                content = textTranslatorBinding.extractedTextInfo.getText().toString();

                ClipData clip = ClipData.newPlainText("Copied Text",content);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Text Copied", Toast.LENGTH_SHORT).show();
            }
        });

        //Make TextFieldEditable
        textTranslatorBinding.editText.setOnClickListener(view -> {
            if(!IS_EDITABLE){
                textTranslatorBinding.editText.setImageResource(R.drawable.edit_off_icon);
                textTranslatorBinding.extractedTextInfo.setEnabled(true);
                IS_EDITABLE = true;
            }else{
                textTranslatorBinding.editText.setImageResource(R.drawable.edit_on_icon);
                textTranslatorBinding.extractedTextInfo.setEnabled(false);
                IS_EDITABLE = false;
            }
        });

    }

    private void translateText(){
        if (TextUtils.isEmpty(textTranslatorBinding.extractedTextInfo.getText().toString())) {
            Toast.makeText(TextTranslatorActivity.this, "Empty Field", Toast.LENGTH_SHORT).show();
        } else {

            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setTargetLanguage(TranslateLanguage.HINDI)
                    .setSourceLanguage(LANGUAGE_CODE!=null?LANGUAGE_CODE:TranslateLanguage.ENGLISH)
                    .build();

            Translator translator = Translation.getClient(options);
            String source = textTranslatorBinding.extractedTextInfo.getText().toString();


            ProgressDialog progressDialog = new ProgressDialog(TextTranslatorActivity.this);
            progressDialog.setMessage("Downloading the translation model....");
            progressDialog.setCancelable(false);
            progressDialog.show();

            translator.downloadModelIfNeeded().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                }
            });
            Task<String> result = translator.translate(textTranslatorBinding.extractedTextInfo.getText().toString()).addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    textTranslatorBinding.extractedTextInfo.setText(s);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(TextTranslatorActivity.this, "Error Occured", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                 uri = result.getUri();
                imageUri = uri;
                recogniseData();
                textTranslatorBinding.croppedImage.setImageURI(uri);
            } else {

            }
        } else {

          }
        }

     //For Identification of Language of Text
    public void identifyLanguage (String s){
        LanguageIdentifier languageIdentifier =
                LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(s)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                switch (languageCode) {
                                    case "af":
                                        Log.d("TAG", "Afrikaans");
                                        LANGUAGE_CODE = "af";
                                        break;
                                    case "nl":
                                        Log.d("TAG", "Dutch");
                                        LANGUAGE_CODE = "nl";
                                        break;
                                    case "en":
                                        Log.d("TAG", "English");
                                        LANGUAGE_CODE = "en";
                                        break;
                                    case "fr":
                                        Log.d("TAG", "French");
                                        LANGUAGE_CODE = "fr";
                                        break;

                                    case "de":
                                        Log.d("TAG", "German");
                                        LANGUAGE_CODE = "de";
                                        break;
                                    case "hi":
                                        Log.d("TAG", "Hindi");
                                        LANGUAGE_CODE = "hi";
                                        break;
                                    case "id":
                                        Log.d("TAG", "Indonesian");
                                        LANGUAGE_CODE = "id";
                                        break;
                                    case "it":
                                        Log.d("TAG", "Italian");
                                        LANGUAGE_CODE = "it";
                                        break;
                                    case "mr":
                                        Log.d("TAG", "Marathi");
                                        LANGUAGE_CODE = "mr";
                                        break;
                                    case "ne":
                                        Log.d("TAG", "Nepali");
                                        LANGUAGE_CODE = "ne";
                                        break;
                                    case "bn":
                                        Log.d("TAG", "Bengali");
                                        LANGUAGE_CODE = "bn";
                                    default:
                                        Log.d("TAG", languageCode);
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(TextTranslatorActivity.this, "The Enter Language is Currently not available..", Toast.LENGTH_SHORT).show();
                            }
                        });


    }


    //For Recognition of Text From Image
    private void recogniseData() {
        if (imageUri != null) {
            try {
                InputImage inputImage = InputImage.fromFilePath(TextTranslatorActivity.this, imageUri);

                Task<Text> result = textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                String recognizeText = text.getText();

                                textTranslatorBinding.extractedTextInfo.setText(recognizeText);
                                Log.d("TAG", recognizeText);
                                identifyLanguage(textTranslatorBinding.extractedTextInfo.getText().toString());

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(TextTranslatorActivity.this, "Couldn't Identify Text", Toast.LENGTH_SHORT).show();
                            }
                        });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
