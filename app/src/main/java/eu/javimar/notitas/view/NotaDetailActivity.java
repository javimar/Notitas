package eu.javimar.notitas.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.javimar.notitas.EditNota;
import eu.javimar.notitas.R;
import eu.javimar.notitas.model.Nota;
import eu.javimar.notitas.viewmodel.NotitasViewModel;

import static android.view.View.GONE;

public class NotaDetailActivity extends AppCompatActivity
{
    @BindView(R.id.title) TextView title;
    @BindView(R.id.body) TextView body;
    @BindView(R.id.etiqueta) TextView etiqueta;
    @BindView(R.id.cardViewNota) CardView notaCard;

    private NotitasViewModel mViewModel;
    private Nota mNota;

    private int mNotaId;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nota_detail);
        ButterKnife.bind(this);

        mNotaId = getIntent().getIntExtra("notaId", 0);
        mViewModel = new ViewModelProvider(this).get(NotitasViewModel.class);
    }

    @Override
    protected void onStart()
    {
        setScreenValues();
        super.onStart();
    }

    private void setScreenValues()
    {
        mNota = mViewModel.findNota(mNotaId);
        if(mNota != null)
        {
            title.setText(mNota.getNotaTitulo());
            body.setText(mNota.getNotaCuerpo());

            String label = mNota.getNotaEtiqueta();
            if(label == null || label.isEmpty()) etiqueta.setVisibility(GONE);
            else
            {
                etiqueta.setVisibility(View.VISIBLE);
                etiqueta.setText(mNota.getNotaEtiqueta());
            }
            notaCard.setCardBackgroundColor(Color.parseColor(mNota.getNotaColor()));
        }
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
        }
        return super.onOptionsItemSelected(item);
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
