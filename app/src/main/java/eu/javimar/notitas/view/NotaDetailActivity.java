package eu.javimar.notitas.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.javimar.notitas.EditNota;
import eu.javimar.notitas.R;
import eu.javimar.notitas.model.Nota;
import eu.javimar.notitas.viewmodel.NotitasViewModel;

import static eu.javimar.notitas.util.Utils.refreshWidget;

public class NotaDetailActivity extends AppCompatActivity
{
    @BindView(R.id.toolbar_detail) Toolbar mToolbarDetail;
    @BindView(R.id.collapse_toolbar_detail) CollapsingToolbarLayout mCollapsingToolbarLayout;

    private NotitasViewModel mViewModel;
    private Nota mNota;
    private int mNotaId;

    private FragmentDetail mFragmentDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nota_detail);
        ButterKnife.bind(this);

        // Toolbar
        setSupportActionBar(mToolbarDetail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mCollapsingToolbarLayout.setTitleEnabled(false);

        mFragmentDetail = (FragmentDetail) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_detail);

        mNotaId = getIntent().getIntExtra("notaId", 0);
        mViewModel = new ViewModelProvider(this).get(NotitasViewModel.class);
    }

    @Override
    protected void onResume()
    {
        refreshScreen();
        super.onResume();
    }

    private void refreshScreen()
    {
        mNota = mViewModel.findNota(mNotaId);
        mFragmentDetail.setScreenValues(mNota);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_edit_nota, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch(id)
        {
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

    private void shareNota()
    {
        String nota = getString(R.string.share_intro) + "\n" +
                mNota.getNotaTitulo() +
                "\n" + mNota.getNotaCuerpo();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);

        if(mNota.getNotaUriImage() != null) // image nota
        {
            String imageUri = mNota.getNotaUriImage();

            if(imageUri.startsWith("file"))
            {
                imageUri = imageUri.replace("file:///","");
            }

            Uri pictureUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName(), new File(imageUri));

            sendIntent.setType("image/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        else if(mNota.getNotaUriAudio() != null) // audio nota
        {
            Uri audioUri = Uri.parse(mNota.getNotaUriAudio());
            sendIntent.setType("audio/*");
            sendIntent.putExtra(Intent.EXTRA_STREAM, audioUri);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        else sendIntent.setType("text/plain");

        sendIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.share_title);
        sendIntent.putExtra(Intent.EXTRA_TEXT, nota);
        Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_intent));
        startActivity(shareIntent);
    }

    private void showDeleteConfirmationDialog()
    {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_nota_dialog_msg);
        builder.setPositiveButton(android.R.string.ok, (dialog, id) ->
        {
            // User clicked the "Delete" button, so delete the notaCard.
            mViewModel.deleteNota(mNotaId);
            // Update the widget with fresh data when deleting
            refreshWidget(this);

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
