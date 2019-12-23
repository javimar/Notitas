package eu.javimar.notitas.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.javimar.notitas.R;
import eu.javimar.notitas.interfaces.ItemTouchHelperAdapter;
import eu.javimar.notitas.interfaces.ItemTouchHelperViewHolder;
import eu.javimar.notitas.interfaces.NotasItemClickListener;
import eu.javimar.notitas.model.Nota;
import eu.javimar.notitas.util.BitmapScaler;
import eu.javimar.notitas.viewmodel.NotitasViewModel;

import static android.view.View.GONE;
import static eu.javimar.notitas.MainActivity.deviceDensityIndependentPixels;
import static eu.javimar.notitas.util.Utils.isInternalUriPointingToValidResource;
import static eu.javimar.notitas.util.Utils.refreshWidget;

public class NotasAdapter extends RecyclerView.Adapter<NotasAdapter.NotasViewHolder>
    implements ItemTouchHelperAdapter
{
    private List<Nota> mNotasList;
    private final NotasItemClickListener mOnClickListener;
    private final Context mContext;
    private final NotitasViewModel mViewModel;

    public NotasAdapter(NotasItemClickListener listener, Context context)
    {
        mContext = context;
        mOnClickListener = listener;
        mViewModel = new ViewModelProvider((FragmentActivity)context)
                .get(NotitasViewModel.class);
    }

    public void setNotasList(List<Nota> notasList)
    {
        mNotasList = notasList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotasViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nota_item, parent, false);
        return new NotasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotasViewHolder holder, int position)
    {
        if(mNotasList != null)
        {
            String aux;

            holder.titulo.setText(mNotasList.get(position).getNotaTitulo());
            holder.body.setText(mNotasList.get(position).getNotaCuerpo());

            aux = mNotasList.get(position).getNotaEtiqueta();
            if(aux == null || aux.equals(""))
            {
                holder.displayTag.setVisibility(GONE);
            }
            else
            {
                holder.displayTag.setVisibility(View.VISIBLE);
                holder.label.setText(mNotasList.get(position).getNotaEtiqueta());
                holder.displayTag
                        .setCardBackgroundColor(Color.parseColor(mNotasList
                                .get(position).getNotaColor()));
            }
            holder.displayNota
                    .setCardBackgroundColor(Color.parseColor(mNotasList
                            .get(position).getNotaColor()));

            aux = mNotasList.get(position).getNotaUriAudio();
            if(aux != null)
            {
                holder.audio.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.audio.setVisibility(GONE);
            }

            aux = mNotasList.get(position).getNotaUriImage();
            if(aux != null)
            {
                holder.image.setVisibility(View.VISIBLE);

                if(!isInternalUriPointingToValidResource(Uri.parse(aux), mContext))
                {
                    Glide
                            .with(mContext)
                            .load(R.drawable.no_image)
                            .into(holder.image);
                }
                else
                {
                    // scale it to fit the width
                    Bitmap scaleImage = BitmapScaler
                            .scaleToFitWidth(
                                    BitmapFactory
                                            .decodeFile(Uri.parse(aux).getPath()),
                                    deviceDensityIndependentPixels[0]);
                    // Show it in the ImageView
                    Glide
                            .with(mContext)
                            .load(scaleImage)
                            .error(R.drawable.no_image)
                            .into(holder.image);
                }
            }
            else
            {
                holder.image.setVisibility(GONE);
            }
        }
    }

    public class NotasViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener,
            ItemTouchHelperViewHolder
    {
        @BindView(R.id.nota_titulo) TextView titulo;
        @BindView(R.id.nota_cuerpo) TextView body;
        @BindView(R.id.nota_etiqueta) TextView label;
        @BindView(R.id.cardNotaDisplay) CardView displayNota;
        @BindView(R.id.cardViewTag) CardView displayTag;
        @BindView(R.id.nota_image) ImageView image;
        @BindView(R.id.nota_audio) ImageView audio;

        private int position;

        NotasViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view)
        {
            mOnClickListener.onNotaItemClick(getNotaId(getAdapterPosition()));
        }

        @Override
        public void onItemSelected()
        {
            displayNota.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
            displayTag.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
            position = getAdapterPosition();
            displayNota.setCardElevation(8);
            displayNota.setRadius(10);
        }

        @Override
        public void onItemClear(int from, int to)
        {
            displayNota.setRadius(10);
            if(position == from)
            {
                // item did not move, just release
                displayNota.setBackgroundColor(Color.parseColor(mNotasList
                        .get(getAdapterPosition()).getNotaColor()));
                displayTag.setBackgroundColor(Color.parseColor(mNotasList
                        .get(getAdapterPosition()).getNotaColor()));
            }
            else
            {
                // item moved
                displayNota.setBackgroundColor(Color.parseColor(mNotasList
                        .get(from).getNotaColor()));
                displayTag.setBackgroundColor(Color.parseColor(mNotasList
                        .get(from).getNotaColor()));
                displayNota.setCardElevation(0);
                // update DB to persit changes of the 2 elements dragged
                mViewModel.swapNotas(getNotaId(from), getNotaId(to));

                // refresh widget
                refreshWidget(mContext);
            }
        }
    }

    private int getNotaId(int position)
    {
        if (mNotasList != null)
        {
            return mNotasList.get(position).getNotaId();
        }
        return -1;
    }

    @Override
    public int getItemCount()
    {
        return mNotasList == null ? 0 : mNotasList.size();
    }

    @Override
    public void onItemDismiss(int position) // won't use for now
    {
        mNotasList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition)
    {
        if (fromPosition < toPosition)
        {
            for (int i = fromPosition; i < toPosition; i++)
            {
                Collections.swap(mNotasList, i, i + 1);
            }
        }
        else
        {
            for (int i = fromPosition; i > toPosition; i--)
            {
                Collections.swap(mNotasList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }
}
