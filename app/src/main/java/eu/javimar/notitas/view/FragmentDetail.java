package eu.javimar.notitas.view;

import static android.view.View.GONE;
import static eu.javimar.notitas.MainActivity.deviceDensityIndependentPixels;
import static eu.javimar.notitas.util.HelperUtils.isInternalUriPointingToValidResource;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import es.dmoral.toasty.Toasty;
import eu.javimar.notitas.AudioActivity;
import eu.javimar.notitas.R;
import eu.javimar.notitas.databinding.FragmentDetailBinding;
import eu.javimar.notitas.model.Nota;
import eu.javimar.notitas.util.BitmapScaler;

public class FragmentDetail extends Fragment {

    private FragmentDetailBinding binding;
    private Toolbar mToolbarDetail;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ScrollView mScroll;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolbarDetail = requireActivity().findViewById(R.id.toolbar_detail);
        mCollapsingToolbarLayout = requireActivity().findViewById(R.id.collapse_toolbar_detail);
        mScroll = requireActivity().findViewById(R.id.scroll_view_detail);
    }

    void setScreenValues(Nota nota) {
        if (nota != null) {
            String aux_str;
            
            binding.title.setText(nota.getNotaTitulo());
            aux_str = nota.getNotaCuerpo();
            if (aux_str == null || aux_str.isEmpty()) {
                binding.body.setVisibility(GONE);
            } else {
                binding.body.setVisibility(View.VISIBLE);
                binding.body.setText(aux_str);
            }
            
            aux_str = nota.getNotaEtiqueta();
            if (aux_str == null || aux_str.isEmpty()) {
                binding.cardViewLabel.setVisibility(GONE);
            } else {
                binding.cardViewLabel.setVisibility(View.VISIBLE);
                binding.etiqueta.setText(aux_str);
            }

            aux_str = nota.getNotaUriImage();
            if (aux_str == null || aux_str.isEmpty()) {
                binding.notaImage.setVisibility(GONE);
            } else {
                binding.notaImage.setVisibility(View.VISIBLE);
                // check first if resource was deleted
                if (!isInternalUriPointingToValidResource(Uri
                        .parse(aux_str), getActivity())) {
                    Toasty.error(requireActivity(), R.string.err_image_resource_not_valid,
                            Toast.LENGTH_SHORT).show();
                    Glide
                            .with(this)
                            .load(R.drawable.no_image)
                            .into(binding.notaImage);
                } else {
                    // scale it to fit the width
                    Bitmap scaleImage = BitmapScaler
                            .scaleToFitWidth(
                                    BitmapFactory
                                            .decodeFile(Uri.parse(aux_str).getPath()),
                                    deviceDensityIndependentPixels[0]);
                    // Show it in the ImageView
                    Glide
                            .with(this)
                            .load(scaleImage)
                            .error(R.drawable.no_image)
                            .into(binding.notaImage);

                    // add onClick to display full image
                    binding.notaImage.setOnClickListener(view ->
                    {
                        String uri = nota.getNotaUriImage();
                        if (uri.startsWith("file"))
                            uri = uri.replace("file:///", "");
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(uri),
                                "image/*");
                        startActivity(intent);
                    });
                }
            }

            aux_str = nota.getNotaUriAudio();
            if (aux_str == null || aux_str.isEmpty()) {
                binding.audioIcon.setVisibility(GONE);
            } else {
                binding.audioIcon.setVisibility(View.VISIBLE);
                binding.audioIcon.setOnClickListener(view ->
                        startActivity(new Intent(getActivity(),
                                AudioActivity.class)
                                .setData(Uri.parse(nota.getNotaUriAudio()))
                        ));
            }

            if (nota.getNotaReminderOn() == 1) {
                binding.cardViewReminderDisplay.setVisibility(View.VISIBLE);
                binding.reminderDisplay.setText(nota.getNotaReminderDate());
            } else binding.cardViewReminderDisplay.setVisibility(GONE);

            binding.cardViewReminderDisplay.setCardBackgroundColor(Color.parseColor(nota.getNotaColor()));
            binding.cardViewNota.setCardBackgroundColor(Color.parseColor(nota.getNotaColor()));
            binding.cardViewLabel.setCardBackgroundColor(Color.parseColor(nota.getNotaColor()));
            setCollapsingBarColor(Color.parseColor(nota.getNotaColor()));
        }
    }

    // gives nota color to the status bar and toolbar
    private void setCollapsingBarColor(int color) {
        if (getActivity() != null) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        mScroll.setBackgroundColor(color);
        mToolbarDetail.setBackgroundColor(color);
        mCollapsingToolbarLayout.setContentScrimColor(color);
        mCollapsingToolbarLayout.setStatusBarScrimColor(color);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
