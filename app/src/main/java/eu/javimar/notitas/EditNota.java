package eu.javimar.notitas;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import eu.javimar.notitas.model.Nota;
import eu.javimar.notitas.util.ColorButton;
import eu.javimar.notitas.viewmodel.NotitasViewModel;

import static android.view.View.GONE;

public class EditNota extends AppCompatActivity
{
    @BindView(R.id.addTitle) EditText addTitle;
    @BindView(R.id.addBody) EditText addBody;
    @BindView(R.id.addEtiqueta) EditText addLabel;
    @BindView(R.id.cardViewNota) CardView notaCard;

    private NotitasViewModel mViewModel;
    private boolean mNewNota;
    private Nota mNota;
    private String mColor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_nota);
        ButterKnife.bind(this);

        mViewModel = new ViewModelProvider(this).get(NotitasViewModel.class);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new notaCard or editing an existing one.
        mNewNota = getIntent().getBooleanExtra("newNota", true);

        if(mNewNota)
        {
            // This is a new notaCard, so change the app bar to say Enter
            setTitle(getString(R.string.title_enter_nota));
            mNewNota = true;
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
    }

    private void setScreenValues()
    {
        if(mNota != null)
        {
            addTitle.setText(mNota.getNotaTitulo());
            addBody.setText(mNota.getNotaCuerpo());

            String label = mNota.getNotaEtiqueta();
            if(label == null || label.isEmpty()) addLabel.setVisibility(GONE);
            else addLabel.setText(mNota.getNotaEtiqueta());
            mColor = mNota.getNotaColor();
            notaCard.setCardBackgroundColor(Color.parseColor(mColor));
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
                    mColor));

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
                    mColor
            ));
            // Insertion was successful
            Toasty.success(this, getString(R.string.update_nota_success),
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}