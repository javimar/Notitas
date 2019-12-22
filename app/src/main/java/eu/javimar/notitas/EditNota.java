package eu.javimar.notitas;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import eu.javimar.notitas.model.Nota;
import eu.javimar.notitas.util.BitmapScaler;
import eu.javimar.notitas.util.ColorButton;
import eu.javimar.notitas.viewmodel.NotitasViewModel;

import static eu.javimar.notitas.MainActivity.deviceDensityIndependentPixels;
import static eu.javimar.notitas.util.Utils.isInternalUriPointingToValidResource;
import static eu.javimar.notitas.util.Utils.refreshWidget;

public class EditNota extends AppCompatActivity
{
    @BindView(R.id.addTitle) EditText addTitle;
    @BindView(R.id.addBody) EditText addBody;
    @BindView(R.id.addEtiqueta) EditText addLabel;
    @BindView(R.id.addImage) ImageView addImage;
    @BindView(R.id.addAudio) ImageView addAudio;
    @BindView(R.id.cardViewNota) CardView notaCard;
    @BindView(R.id.toolbar_enter_nota) Toolbar mToolbar;
    @BindView(R.id.toolbar_footer_edit) Toolbar mFooter;
    @BindView(R.id.content) ScrollView mEdit_container;
    @BindView(R.id.collapse_toolbar_enter_nota) CollapsingToolbarLayout mCollapsingToolbarLayout;

    private static boolean sHaveReadPermission = false;

    private NotitasViewModel mViewModel;
    private boolean mNewNota;
    private Nota mNota;
    private String mColor, mImageUri, mAudioUri;
    private String mPhotoFileName;

    private final static int REQUEST_AUDIO_PERMISSION = 900;
    private final static int CAPTURE_PHOTO_ACTIVITY_REQUEST_CODE = 901;
    private final static int AUDIO_ACTIVITY_REQUEST_CODE = 902;
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 903;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_nota_main);
        ButterKnife.bind(this);

        // Toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCollapsingToolbarLayout.setTitleEnabled(false);

        // Toolbar footer functionality
        mFooter.inflateMenu(R.menu.menu_footer);
        mFooter.setOnMenuItemClickListener(item ->
        {
            int id = item.getItemId();
            switch(id)
            {
                case R.id.action_audio:
                    addAudio();
                    break;
                case R.id.action_image:
                    pickImage();
                    break;
                case R.id.action_photo:
                    takePhoto();
                    break;
            }
            return false;
        });

        mViewModel = new ViewModelProvider(this).get(NotitasViewModel.class);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new notaCard or editing an existing one.
        mNewNota = getIntent().getBooleanExtra("newNota", true);

        if(mNewNota)
        {
            // This is a new notaCard, so change the app bar to say Enter
            setTitle(getString(R.string.title_enter_nota));
            mNewNota = true;
            setCollapsingBarColor(getResources().getColor(R.color.white));
        }
        else
        {
            // Otherwise this is an existing notaCard, so change app bar to say Update
            setTitle(getString(R.string.title_update_nota));
            // get the values to be updated
            mNota = getIntent().getParcelableExtra("nota");
            setScreenValues();
        }
    }

    public void buttonColor(View view)
    {
        int id = view.getId();
        int colorId = ColorButton.colorButton(id);
        mColor = "#" + Integer.toHexString(ContextCompat.getColor(this, colorId));
        notaCard.setBackgroundColor(Color.parseColor(mColor));
        setCollapsingBarColor(Color.parseColor(mColor));
        mEdit_container.setBackgroundColor(Color.parseColor(mColor));
    }

    private void setScreenValues()
    {
        if(mNota != null)
        {
            addTitle.setText(mNota.getNotaTitulo());
            addBody.setText(mNota.getNotaCuerpo());
            addLabel.setText(mNota.getNotaEtiqueta());
            mColor = mNota.getNotaColor();
            notaCard.setCardBackgroundColor(Color.parseColor(mColor));
            setCollapsingBarColor(Color.parseColor(mNota.getNotaColor()));
            mEdit_container.setBackgroundColor(Color.parseColor(mColor));

            mImageUri = mNota.getNotaUriImage();
            if(mImageUri != null)
            {
                addImage.setVisibility(View.VISIBLE);
                // check first if resource was deleted

                if(!isInternalUriPointingToValidResource(Uri.parse(mImageUri), this))
                {
                    Glide
                            .with(this)
                            .load(R.drawable.no_image)
                            .into(addImage);
                }
                else
                {
                    // scale it to fit the width
                    Bitmap scaleImage = BitmapScaler
                            .scaleToFitWidth(
                                    BitmapFactory
                                            .decodeFile(Uri.parse(mImageUri).getPath()),
                                    deviceDensityIndependentPixels[0]);
                    Glide
                            .with(this)
                            .load(scaleImage)
                            .error(R.drawable.no_image)
                            .into(addImage);
                }
            }

            mAudioUri = mNota.getNotaUriAudio();
            if(mAudioUri != null)
            {
                addAudio.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_enter_nota, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.action_save_nota)
        {
            validateNota();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void validateNota()
    {
        if(mColor == null) mColor = "#FFFFFF";

        if(addTitle.getText().toString() == null ||
                addTitle.getText().toString().isEmpty())
        {
            Toasty.error(this, getString(R.string.err_title_missing),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        saveNota();
    }

    private void saveNota()
    {
        if(mNewNota)
        {
            // insert Note
            long row = mViewModel.insertNota(new Nota(
                    addTitle.getText().toString().trim(),
                    addBody.getText().toString().trim(),
                    addLabel.getText().toString().trim(),
                    mColor,
                    mImageUri,
                    mAudioUri));

            if(row > 0)
            {
                // Insertion was successful
                Toasty.success(this, getString(R.string.add_nota_success),
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                // Insertion failed
                Toasty.error(this, getString(R.string.add_nota_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            mViewModel.updateNota(new Nota(
                    mNota.getNotaId(),
                    addTitle.getText().toString().trim(),
                    addBody.getText().toString().trim(),
                    addLabel.getText().toString().trim(),
                    mColor,
                    mImageUri,
                    mAudioUri
            ));
            // Insertion was successful
            Toasty.success(this, getString(R.string.update_nota_success),
                    Toast.LENGTH_SHORT).show();
        }
        // Update the widget with fresh data when adding or updating
        refreshWidget(this);

        finish();
    }

    // gives nota color to the status bar and collapsing bar
    private void setCollapsingBarColor(int color)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                //  set status text dark
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        mToolbar.setBackgroundColor(color);
        mCollapsingToolbarLayout.setContentScrimColor(color);
        mCollapsingToolbarLayout.setStatusBarScrimColor(color);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case CAPTURE_PHOTO_ACTIVITY_REQUEST_CODE:
                    // by this point we have the camera photo on disk
                    Uri takenPhotoUri = Uri.fromFile(getPhotoFileUri(mPhotoFileName));
                    // takenPhotoUri is what we want, something like
                    // file:///storage/emulated/0/Android/data/eu.javimar.notitas/files/Pictures
                    // /Notitas/20191220_205729.jpg
                    Bitmap rawTakenImage = BitmapFactory.decodeFile(takenPhotoUri.getPath());
                    addImage.setVisibility(View.VISIBLE);
                    addImage.setImageBitmap(rawTakenImage);
                    // store photo path for DB persistence
                    mImageUri = takenPhotoUri.toString();
                    break;

                case AUDIO_ACTIVITY_REQUEST_CODE:
                    // store audio path for DB persistence
                    mAudioUri = data.getData().toString();
                    break;

                case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                    InputStream input = null;
                    try
                    {
                        input = getContentResolver().openInputStream(data.getData());
                    }
                    catch (FileNotFoundException e) { e.printStackTrace(); }

                    Bitmap image = BitmapFactory.decodeStream(input);
                    String name = new SimpleDateFormat("yyyyMMdd_HHmmss",
                            Locale.getDefault()).format(new Date()) + ".jpg";
                    File imageFile = getPhotoFileUri(name);
                    FileOutputStream fos = null;
                    try
                    {
                        fos = new FileOutputStream(imageFile);
                        // Use the compress method on the Bitmap object to write image to the OutputStream
                        image.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                    } catch (Exception e) { e.printStackTrace(); }
                    finally
                    {
                        try { fos.close(); } catch (IOException e) { e.printStackTrace(); }
                    }
                    addImage.setVisibility(View.VISIBLE);
                    addImage.setImageBitmap(image);
                    mImageUri = imageFile.getPath();
                    break;
            }
        }
    }

    private void pickImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore
                .Images.Media.INTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null)
        {
            // Start the image intent to pick an image
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private void takePhoto()
    {
        mPhotoFileName = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date()) + ".jpg";

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        File photoFile = getPhotoFileUri(mPhotoFileName);

        // wrap File object into a content provider required for API >= 24
        Uri fileProvider = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID, photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if(intent.resolveActivity(getPackageManager()) != null)
        {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_PHOTO_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    private File getPhotoFileUri(String fileName)
    {
        // Get safe storage directory for photos
        // Use "getExternalFilesDir" on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir =
                new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        getString(R.string.app_name));

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs())
        {
            Log.d(getString(R.string.app_name), "failed to create directory");
        }
        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private void addAudio()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            // true if app asks permission and user rejects request
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                // we show an explanation why it is needed
                Snackbar.make(findViewById(android.R.id.content),
                        R.string.explain_audio_permission,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view ->
                                ActivityCompat.requestPermissions(EditNota.this,
                                        new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                        REQUEST_AUDIO_PERMISSION)).show();
            }
            else
            {
                // no explanation needed, request the permission
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                        REQUEST_AUDIO_PERMISSION);
            }
        }
        else sHaveReadPermission = true;

        if(sHaveReadPermission)
        {
            addAudio.setVisibility(View.VISIBLE);
            Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
            if(intent.resolveActivity(getPackageManager()) != null)
            {
                startActivityForResult(intent, AUDIO_ACTIVITY_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                sHaveReadPermission = true;
            }
            else
            {
                Toasty.warning(this, R.string.need_audio_permission,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}