package eu.javimar.notitas.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import eu.javimar.notitas.AudioActivity;
import eu.javimar.notitas.R;
import eu.javimar.notitas.model.Nota;
import eu.javimar.notitas.util.BitmapScaler;

import static android.view.View.GONE;
import static eu.javimar.notitas.MainActivity.deviceDensityIndependentPixels;
import static eu.javimar.notitas.util.Utils.isUriPointingToValidResource;

public class FragmentDetail extends Fragment
{
    @BindView(R.id.title) TextView title;
    @BindView(R.id.body) TextView body;
    @BindView(R.id.etiqueta) TextView etiqueta;
    @BindView(R.id.notaImage) ImageView notaImage;
    @BindView(R.id.audioIcon) ImageView notaAudio;
    @BindView(R.id.cardViewNota) CardView notaCard;
    @BindView(R.id.cardViewLabel) CardView labelCard;

    // Activity views
    private Toolbar mToolbarDetail;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ScrollView mScroll;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mToolbarDetail = getActivity().findViewById(R.id.toolbar_detail);
        mCollapsingToolbarLayout = getActivity().findViewById(R.id.collapse_toolbar_detail);
        mScroll = getActivity().findViewById(R.id.scroll_view_detail);
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

            if(nota.getNotaUriImage() == null || nota.getNotaUriImage().isEmpty())
            {
                notaImage.setVisibility(GONE);
            }
            else
            {
                notaImage.setVisibility(View.VISIBLE);
                // check first if resource was deleted
                if(!isUriPointingToValidResource(Uri
                        .parse(nota.getNotaUriImage()), getActivity()))
                {
                    Toasty.error(getActivity(), R.string.err_image_resource_not_valid,
                            Toast.LENGTH_SHORT).show();
                    Glide
                            .with(this)
                            .load(R.drawable.no_image)
                            .into(notaImage);
                }
                else
                {

                    // scale it to fit the width
                    Bitmap scaleImage = BitmapScaler
                            .scaleToFitWidth(
                                    BitmapFactory
                                            .decodeFile(Uri.parse(nota.getNotaUriImage()).getPath()),
                                    deviceDensityIndependentPixels[0]);
                    // Show it in the ImageView
                    Glide
                            .with(this)
                            .load(scaleImage)
                            .error(R.drawable.no_image)
                            .into(notaImage);
                }
            }

            if(nota.getNotaUriAudio() == null || nota.getNotaUriAudio().isEmpty())
            {
                notaAudio.setVisibility(GONE);
            }
            else
            {
                notaAudio.setVisibility(View.VISIBLE);
                notaAudio.setOnClickListener(view ->
                        startActivity(new Intent(getActivity(),
                                AudioActivity.class)
                                .setData(Uri.parse(nota.getNotaUriAudio()))
                        ));
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
        mScroll.setBackgroundColor(color);
        mToolbarDetail.setBackgroundColor(color);
        mCollapsingToolbarLayout.setContentScrimColor(color);
        mCollapsingToolbarLayout.setStatusBarScrimColor(color);
    }
}
