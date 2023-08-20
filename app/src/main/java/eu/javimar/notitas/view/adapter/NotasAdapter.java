package eu.javimar.notitas.view.adapter;

import static android.view.View.GONE;
import static eu.javimar.notitas.MainActivity.deviceDensityIndependentPixels;
import static eu.javimar.notitas.util.HelperUtils.isInternalUriPointingToValidResource;
import static eu.javimar.notitas.util.HelperUtils.refreshWidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import eu.javimar.notitas.R;
import eu.javimar.notitas.databinding.NotaItemBinding;
import eu.javimar.notitas.interfaces.ItemTouchHelperAdapter;
import eu.javimar.notitas.interfaces.ItemTouchHelperViewHolder;
import eu.javimar.notitas.interfaces.NotasItemClickListener;
import eu.javimar.notitas.model.Nota;
import eu.javimar.notitas.util.BitmapScaler;
import eu.javimar.notitas.viewmodel.NotitasViewModel;

public class NotasAdapter extends RecyclerView.Adapter<NotasAdapter.NotasViewHolder>
        implements ItemTouchHelperAdapter {
    private List<Nota> mNotasList;
    private final NotasItemClickListener mOnClickListener;
    private final Context mContext;
    private final NotitasViewModel mViewModel;

    public NotasAdapter(NotasItemClickListener listener, Context context) {
        mContext = context;
        mOnClickListener = listener;
        mViewModel = new ViewModelProvider((FragmentActivity) context)
                .get(NotitasViewModel.class);
    }

    public void setNotasList(List<Nota> notasList) {
        mNotasList = notasList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotasAdapter.NotasViewHolder(
                NotaItemBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotasViewHolder holder, int position) {
        if (mNotasList != null) {
            String aux_str;

            holder.binding.notaTitulo.setText(mNotasList.get(position).getNotaTitulo());

            aux_str = mNotasList.get(position).getNotaCuerpo();
            if (aux_str == null || aux_str.isEmpty()) {
                holder.binding.notaCuerpo.setVisibility(GONE);
            } else {
                holder.binding.notaCuerpo.setVisibility(View.VISIBLE);
                holder.binding.notaCuerpo.setText(aux_str);
            }

            aux_str = mNotasList.get(position).getNotaEtiqueta();
            if (aux_str == null || aux_str.equals("")) {
                
                holder.binding.cardViewTag.setVisibility(GONE);
            } else {
                holder.binding.cardViewTag.setVisibility(View.VISIBLE);
                holder.binding.notaEtiqueta.setText(mNotasList.get(position).getNotaEtiqueta());
                holder.binding.cardViewTag
                        .setCardBackgroundColor(Color.parseColor(mNotasList
                                .get(position).getNotaColor()));
            }
            
            holder.binding.cardNotaDisplay
                    .setCardBackgroundColor(Color.parseColor(mNotasList
                            .get(position).getNotaColor()));
            
            aux_str = mNotasList.get(position).getNotaUriAudio();
            if (aux_str != null) {
                holder.binding.notaAudio.setVisibility(View.VISIBLE);
            } else {
                holder.binding.notaAudio.setVisibility(GONE);
            }
            
            aux_str = mNotasList.get(position).getNotaUriImage();
            if (aux_str != null) {
                holder.binding.notaImage.setVisibility(View.VISIBLE);

                if (!isInternalUriPointingToValidResource(Uri.parse(aux_str), mContext)) {
                    Glide
                            .with(mContext)
                            .load(R.drawable.no_image)
                            .into(holder.binding.notaImage);
                } else {
                    // scale it to fit the width
                    Bitmap scaleImage = BitmapScaler
                            .scaleToFitWidth(
                                    BitmapFactory
                                            .decodeFile(Uri.parse(aux_str).getPath()),
                                    deviceDensityIndependentPixels[0]);
                    // Show it in the ImageView
                    Glide
                            .with(mContext)
                            .load(scaleImage)
                            .error(R.drawable.no_image)
                            .into(holder.binding.notaImage);
                }
            } else {
                holder.binding.notaImage.setVisibility(GONE);
            }

            if (mNotasList.get(position).getNotaReminderOn() == 1) {
                holder.binding.notaReminderSet.setVisibility(View.VISIBLE);
            } else {
                holder.binding.notaReminderSet.setVisibility(GONE);
            }
        }
    }

    public class NotasViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener,
            ItemTouchHelperViewHolder {

        NotaItemBinding binding;

        private int position;

        NotasViewHolder(NotaItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnClickListener.onNotaItemClick(getNotaId(getAdapterPosition()));
        }

        @Override
        public void onItemSelected() {
            binding.cardNotaDisplay.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            binding.cardViewTag.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            position = getAdapterPosition();
            binding.cardNotaDisplay.setCardElevation(8);
            binding.cardNotaDisplay.setRadius(10);
        }

        @Override
        public void onItemClear(int from, int to) {
            binding.cardNotaDisplay.setRadius(10);
            if (position == from) {
                // item did not move, just release
                binding.cardNotaDisplay.setBackgroundColor(Color.parseColor(mNotasList
                        .get(getAdapterPosition()).getNotaColor()));
                binding.cardViewTag.setBackgroundColor(Color.parseColor(mNotasList
                        .get(getAdapterPosition()).getNotaColor()));
            } else {
                // item moved
                binding.cardNotaDisplay.setBackgroundColor(Color.parseColor(mNotasList
                        .get(from).getNotaColor()));
                binding.cardViewTag.setBackgroundColor(Color.parseColor(mNotasList
                        .get(from).getNotaColor()));
                binding.cardNotaDisplay.setCardElevation(0);
                // update DB to persit changes of the 2 elements dragged
                mViewModel.swapNotas(getNotaId(from), getNotaId(to));

                // refresh widget
                refreshWidget(mContext);
            }
        }
    }

    private int getNotaId(int position) {
        if (mNotasList != null) {
            return mNotasList.get(position).getNotaId();
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return mNotasList == null ? 0 : mNotasList.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mNotasList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mNotasList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }
}
