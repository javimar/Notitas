package eu.javimar.notitas;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
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
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import eu.javimar.notitas.model.Nota;
import eu.javimar.notitas.synch.ReminderReceiver;
import eu.javimar.notitas.util.BitmapScaler;
import eu.javimar.notitas.util.ColorButton;
import eu.javimar.notitas.util.MyBounceInterpolator;
import eu.javimar.notitas.viewmodel.NotitasViewModel;

import static eu.javimar.notitas.MainActivity.deviceDensityIndependentPixels;
import static eu.javimar.notitas.util.HelperUtils.bool2Int;
import static eu.javimar.notitas.util.HelperUtils.cancelReminder;
import static eu.javimar.notitas.util.HelperUtils.getKeyFromStr;
import static eu.javimar.notitas.util.HelperUtils.int2Bool;
import static eu.javimar.notitas.util.HelperUtils.isInternalUriPointingToValidResource;
import static eu.javimar.notitas.util.HelperUtils.refreshWidget;

public class EditNota extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener
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
    @BindView(R.id.cardViewReminder) CardView reminderCard;
    @BindView(R.id.reminder) TextView reminder;

    private TimePickerDialog tpd;

    private static boolean sHaveReadPermission = false;

    private NotitasViewModel mViewModel;
    private boolean mNewNota;
    private Nota mNota;
    private String mColor, mImageUri, mAudioUri, mReminderString;
    private String mPhotoFileName;

    private final static int REQUEST_AUDIO_PERMISSION = 900;
    private final static int CAPTURE_PHOTO_ACTIVITY_REQUEST_CODE = 901;
    private final static int AUDIO_ACTIVITY_REQUEST_CODE = 902;
    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 903;

    private boolean mReminderSet = false, mIsNewReminder = false;
    private int[] mCalendar;
    private LocalDateTime mReminderDateTime;

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
            // get the values to be updated from DetailActivity
            mNota = getIntent().getParcelableExtra("nota");
            setScreenValues();
        }

        mIsNewReminder = false;
    }

    public void buttonColor(View view)
    {
        int id = view.getId();

        int colorId = ColorButton.colorButton(id);
        mColor = "#" + Integer.toHexString(ContextCompat.getColor(this, colorId));
        notaCard.setBackgroundColor(Color.parseColor(mColor));
        setCollapsingBarColor(Color.parseColor(mColor));
        mEdit_container.setBackgroundColor(Color.parseColor(mColor));

        final Animation scale_up_animation =
                AnimationUtils.loadAnimation(this, R.anim.scale_animation);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        scale_up_animation.setInterpolator(interpolator);
        view.startAnimation(scale_up_animation);
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
            mReminderSet = int2Bool(mNota.getNotaReminderOn());

            if(mReminderSet)
            {
                reminderCard.setVisibility(View.VISIBLE);
                reminder.setText(mNota.getNotaReminderDate());
                reminderCard.setCardBackgroundColor(Color.parseColor(mColor));
            }
            else
            {
                reminderCard.setVisibility(View.GONE);
            }

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

        switch(id)
        {
            case R.id.action_save_nota:
                validateNota();
                break;
            case R.id.action_add_reminder:
                addReminder();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addReminder()
    {
        setDateTimePicker();
    }

    public void deleteReminder(View view)
    {
        mReminderSet = false;
        mIsNewReminder = false;
        mReminderString = "";
        reminderCard.setVisibility(View.GONE);

        if (addTitle != null)
            cancelReminder(this, addTitle.getText().toString(), false);
    }

    private void setDateTimePicker()
    {
        LocalDate today = LocalDate.now();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                today.getYear(),
                today.getMonthValue() - 1,
                today.getDayOfMonth()
        );

        dpd.setMinDate(Calendar.getInstance());
        dpd.setOkColor(getResources().getColor(R.color.colorPrimary));
        dpd.setCancelColor(getResources().getColor(R.color.colorAccent));
        dpd.setTitle(getString(R.string.nota_date_picker_title));

        LocalTime now = LocalTime.now();
        tpd = TimePickerDialog.newInstance(
                this,
                now.getHour(),
                now.getMinute(),
                true
        );
        tpd.setOkColor(getResources().getColor(R.color.colorPrimary));
        tpd.setCancelColor(getResources().getColor(R.color.colorAccent));
        tpd.setTitle(getString(R.string.nota_time_picker_title));

        dpd.show(getSupportFragmentManager(), "dpd");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth)
    {
        mCalendar = new int[6];
        mCalendar[0] = year;
        mCalendar[1] = monthOfYear + 1;
        mCalendar[2] = dayOfMonth;

        tpd.show(getSupportFragmentManager(), "tpd");
    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second)
    {
        mCalendar[3] = hourOfDay;
        mCalendar[4] = minute;
        mCalendar[5] = 0; // seconds

        mReminderSet = true;
        reminderCard.setVisibility(View.VISIBLE);

        mReminderDateTime =
                LocalDateTime.of(mCalendar[0], mCalendar[1], mCalendar[2],
                        mCalendar[3], mCalendar[4], mCalendar[5]);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM HH:mm");
        mReminderString = mReminderDateTime.format(formatter);
        reminder.setText(mReminderString);

        // new reminder active, signal it
        mIsNewReminder = true;
    }

    private void validateNota()
    {
        if(mColor == null) mColor = "#FFFFFF";

        if(addTitle.getText().toString().isEmpty())
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
                    mAudioUri,
                    bool2Int(mReminderSet),
                    mReminderString
            ));

            if(row > 0)
            {
                // Insertion was successful
                Toasty.success(this, getString(R.string.add_nota_success),
                        Toast.LENGTH_SHORT).show();

                // Activate Reminder
                if(mReminderSet) startReminder((int)row);
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
            int id = mNota.getNotaId();
            // reminder updated, did we change the reminder date?
            if(mIsNewReminder) startReminder(id);

            mViewModel.updateNota(new Nota(
                    mNota.getNotaId(),
                    addTitle.getText().toString().trim(),
                    addBody.getText().toString().trim(),
                    addLabel.getText().toString().trim(),
                    mColor,
                    mImageUri,
                    mAudioUri,
                    bool2Int(mReminderSet),
                    mReminderString
            ));
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
                    LocalDateTime date = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                    String name = date.format(formatter) + ".jpg";
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
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        mPhotoFileName = date.format(formatter) + ".jpg";

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

    private void startReminder(int id)
    {
        if(mReminderDateTime.compareTo(LocalDateTime.now()) < 0)
        {
            Toasty.error(this, getString(R.string.err_date_has_passed),
                    Toast.LENGTH_LONG).show();
            mReminderSet = false;
            reminderCard.setVisibility(View.GONE);
            return;
        }

        String notaTitle = addTitle.getText().toString().trim();
        int requestCode = getKeyFromStr(notaTitle);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("header", getString(R.string.notification_title));
        intent.putExtra("notaId", id);
        intent.putExtra("msg",
                (String.format(getString(R.string.notification_ows_event_message), notaTitle)));
        intent.putExtra("requestCode", requestCode);
        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Toasty.info(this, getString(R.string.reminder_on), Toast.LENGTH_SHORT).show();

        alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                mReminderDateTime.atZone(ZoneId.of(TimeZone.getDefault()
                        .getID())).toInstant().toEpochMilli(),
                pendingIntent);
        mCalendar = null;
    }
}