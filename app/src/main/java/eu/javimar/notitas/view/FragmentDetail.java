package eu.javimar.notitas.view;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.javimar.notitas.R;
import eu.javimar.notitas.model.Nota;

import static android.view.View.GONE;

public class FragmentDetail extends Fragment
{
    @BindView(R.id.title) TextView title;
    @BindView(R.id.body) TextView body;
    @BindView(R.id.etiqueta) TextView etiqueta;
    @BindView(R.id.cardViewNota) CardView notaCard;
    @BindView(R.id.cardViewLabel) CardView labelCard;

    // Activity views
    private Toolbar mToolbarDetail;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);

        mToolbarDetail = getActivity().findViewById(R.id.toolbar_detail);
        mCollapsingToolbarLayout = getActivity().findViewById(R.id.collapse_toolbar_detail);

        return rootView;
    }

    void setScreenValues(Nota nota)
    {
        if(nota != null)
        {
            title.setText(nota.getNotaTitulo());
            body.setText(nota.getNotaCuerpo());

            String label = nota.getNotaEtiqueta();
            if(label == null || label.isEmpty()) etiqueta.setVisibility(GONE);
            else
            {
                etiqueta.setVisibility(View.VISIBLE);
                etiqueta.setText(nota.getNotaEtiqueta());
            }
            notaCard.setCardBackgroundColor(Color.parseColor(nota.getNotaColor()));
            labelCard.setCardBackgroundColor(Color.parseColor(nota.getNotaColor()));
            setCollapsingBarColor(Color.parseColor(nota.getNotaColor()));
        }
    }

    // gives nota color to the status bar and toolbar
    private void setCollapsingBarColor(int color)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if(getActivity() != null)
            {
                Window window = getActivity().getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    //  set status app text dark
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        mToolbarDetail.setBackgroundColor(color);
        mCollapsingToolbarLayout.setContentScrimColor(color);
        mCollapsingToolbarLayout.setStatusBarScrimColor(color);
    }
}
