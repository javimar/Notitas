package eu.javimar.notitas.util;

import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import eu.javimar.notitas.interfaces.ItemTouchHelperAdapter;
import eu.javimar.notitas.interfaces.ItemTouchHelperViewHolder;

public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback
{
    private final ItemTouchHelperAdapter mAdapter;
    private int toPosition;

    public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter)
    {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled()
    {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled()
    {
        return true;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder)
    {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target)
    {
        mAdapter.onItemMove(viewHolder.getAdapterPosition(),
                target.getAdapterPosition());

        // save destination position to use in onItemClear
        toPosition = target.getAdapterPosition();

        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction)
    {
        // mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                  int actionState)
    {
        // We only want the active item
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE)
        {
            if (viewHolder instanceof ItemTouchHelperViewHolder)
            {
                ItemTouchHelperViewHolder itemViewHolder =
                        (ItemTouchHelperViewHolder) viewHolder;
                itemViewHolder.onItemSelected();
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder)
    {
        super.clearView(recyclerView, viewHolder);

        if (viewHolder instanceof ItemTouchHelperViewHolder)
        {
            ItemTouchHelperViewHolder itemViewHolder =
                    (ItemTouchHelperViewHolder) viewHolder;
            itemViewHolder.onItemClear(viewHolder.getAdapterPosition(),
                    toPosition);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive)
    {

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE)
        {
            float width = (float) viewHolder.itemView.getWidth();
            float alpha = 1.0f - Math.abs(dX) / width;
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        }
        else
        {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY,
                    actionState, isCurrentlyActive);
        }
    }
}
