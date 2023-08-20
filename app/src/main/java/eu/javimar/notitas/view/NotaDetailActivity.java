package eu.javimar.notitas.view;

import static eu.javimar.notitas.util.HelperUtils.cancelReminder;
import static eu.javimar.notitas.util.HelperUtils.refreshWidget;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import eu.javimar.notitas.EditNota;
import eu.javimar.notitas.R;
import eu.javimar.notitas.databinding.ActivityNotaDetailBinding;
import eu.javimar.notitas.model.Nota;
import eu.javimar.notitas.viewmodel.NotitasViewModel;

public class NotaDetailActivity extends AppCompatActivity {

    private NotitasViewModel mViewModel;
    private Nota mNota;
    private int mNotaId;

    private FragmentDetail mFragmentDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityNotaDetailBinding binding = ActivityNotaDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarDetail.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        binding.collapseToolbarDetail.setTitleEnabled(false);

        mFragmentDetail = (FragmentDetail) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_detail);

        mNotaId = getIntent().getIntExtra("notaId", 0);
        mViewModel = new ViewModelProvider(this).get(NotitasViewModel.class);
    }

    @Override
    protected void onResume() {
        refreshScreen();
        super.onResume();
    }

    private void refreshScreen() {
        mNota = mViewModel.findNota(mNotaId);
        mFragmentDetail.setScreenValues(mNota);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_nota, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit_nota:
                startActivity(new Intent(this, EditNota.class)
                        .putExtra("newNota", false)
                        .putExtra("nota", mNota));
                break;
            case R.id.action_delete_nota:
                showDeleteConfirmationDialog();
                break;
            case R.id.action_share_nota:
                shareNota();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareNota() {
        String nota = getString(R.string.share_intro) + "\n" +
                mNota.getNotaTitulo() +
                "\n" + mNota.getNotaCuerpo();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);

        if (mNota.getNotaUriImage() != null) // image nota
        {
            String imageUri = mNota.getNotaUriImage();

            if (imageUri.startsWith("file")) {
                imageUri = imageUri.replace("file:///", "");
            }

            Uri pictureUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName(), new File(imageUri));

            sendIntent.setType("image/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else if (mNota.getNotaUriAudio() != null) // audio nota
        {
            Uri audioUri = Uri.parse(mNota.getNotaUriAudio());
            sendIntent.setType("audio/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, audioUri);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else sendIntent.setType("text/plain");

        sendIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.share_title);
        sendIntent.putExtra(Intent.EXTRA_TEXT, nota);
        Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_intent));
        startActivity(shareIntent);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_nota_dialog_msg);
        builder.setPositiveButton(android.R.string.ok, (dialog, id) ->
        {
            // User clicked the "Delete" button, so delete the notaCard.
            mViewModel.deleteNota(mNotaId);
            Toasty.info(this, getString(R.string.nota_deleted),
                    Toast.LENGTH_SHORT).show();

            // Update the widget with fresh data when deleting
            refreshWidget(this);

            // delete reminder if there is any
            if (mNota.getNotaReminderOn() == 1) {
                cancelReminder(this, mNota.getNotaTitulo(), true);
            }
            finish();
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, id) ->
        {
            // User clicked the "Cancel" button, so dismiss the dialog
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
