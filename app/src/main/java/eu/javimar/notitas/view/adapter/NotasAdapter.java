package eu.javimar.notitas.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import eu.javimar.notitas.R;
import eu.javimar.notitas.listeners.NotasItemClickListener;
import eu.javimar.notitas.model.Nota;
import eu.javimar.notitas.viewmodel.NotitasViewModel;

import static android.view.View.GONE;

public class NotasAdapter extends RecyclerView.Adapter<NotasAdapter.NotasViewHolder>
{
    private List<Nota> mNotasList;
    private final NotasItemClickListener mOnClickListener;
    private final NotitasViewModel mViewModel;
    private final Context mContext;

    private final List<Integer> itemsPendingRemoval;
    private static final int PENDING_REMOVAL_TIMEOUT = 3000;
    private final Handler handler = new Handler();
    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, Runnable> pendingRunnables = new HashMap<>();

    public NotasAdapter(NotasItemClickListener listener, Context context)
    {
        mContext = context;
        mViewModel = new ViewModelProvider((FragmentActivity)context)
                .get(NotitasViewModel.class);
        mOnClickListener = listener;
        itemsPendingRemoval = new ArrayList<>();
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
        int id = getNotaId(position);

        // get necessary information to compare if item is already waiting to be removed
        if (itemsPendingRemoval.contains(id))
        {
            // show swipe layout and hide the regular layout
            holder.clListContainer.setVisibility(GONE);
            holder.llSwipeLayout.setVisibility(View.VISIBLE);
            holder.tvUndo.setOnClickListener(v -> undoOption(id, position));
        }
        else
        {
            // Proceed normally with the regular layout and hide the swipe layout
            holder.clListContainer.setVisibility(View.VISIBLE);
            holder.llSwipeLayout.setVisibility(GONE);

            if(mNotasList != null)
            {
                holder.titulo.setText(mNotasList.get(position).getNotaTitulo());
                holder.body.setText(mNotasList.get(position).getNotaCuerpo());

                String tag = mNotasList.get(position).getNotaEtiqueta();
                if(tag == null || tag.equals(""))
                {
                    holder.label.setVisibility(GONE);
                }
                else
                {
                    holder.label.setVisibility(View.VISIBLE);
                    holder.label.setText(mNotasList.get(position).getNotaEtiqueta());

                }
                holder.displayNota
                        .setCardBackgroundColor(Color.parseColor(mNotasList
                                .get(position).getNotaColor()));
            }
        }
    }

    public class NotasViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        @BindView(R.id.undo) TextView tvUndo;
        @BindView(R.id.swipeLayout) LinearLayout llSwipeLayout;
        @BindView(R.id.notas_container) LinearLayout clListContainer;
        @BindView(R.id.nota_titulo) TextView titulo;
        @BindView(R.id.nota_cuerpo) TextView body;
        @BindView(R.id.nota_etiqueta) TextView label;
        @BindView(R.id.cardNotaDisplay) CardView displayNota;
        @BindView(R.id.cardViewTag) CardView displayTag;

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

    private void undoOption(int id, int position)
    {
        Runnable pendingRemovalRunnable = pendingRunnables.get(id);
        pendingRunnables.remove(id);
        if (pendingRemovalRunnable != null)
            handler.removeCallbacks(pendingRemovalRunnable);
        itemsPendingRemoval.remove((Integer) id);
        notifyItemChanged(position);
    }

    /** Called when swipe action is initiated */
    public void pendingRemoval(final int position)
    {
        int id = getNotaId(position);
        if (!itemsPendingRemoval.contains(id))
        {
            itemsPendingRemoval.add(id);
            notifyItemChanged(position);
            Runnable pendingRemovalRunnable = () -> remove(id, position);
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(id, pendingRemovalRunnable);
        }
    }

    public boolean isPendingRemoval(int position)
    {
        return itemsPendingRemoval.contains(getNotaId(position));
    }

    // Delete the nota from database
    private void remove(int id, int position)
    {
        if (itemsPendingRemoval.contains(id))
        {
            // clear element
            itemsPendingRemoval.remove((Integer) id);
        }
        mViewModel.deleteNota(id);
        notifyItemRemoved(position);
        Toasty.info(mContext, R.string.delete_nota_success,
                Toast.LENGTH_SHORT).show();
    }
}
