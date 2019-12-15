package eu.javimar.notitas.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import eu.javimar.notitas.R;

public abstract class SwipeUtil extends ItemTouchHelper.SimpleCallback
{
    private Drawable background;
    private Drawable deleteIcon;

    private int xMarkMargin;

    private boolean initiated;
    private final Context context;

    private int leftcolorCode;
    private String leftSwipeLabel;

    protected SwipeUtil(int dragDirs, int swipeDirs, Context context)
    {
        super(dragDirs, swipeDirs);
        this.context = context;
    }

    private void init()
    {
        background = new ColorDrawable();
        xMarkMargin = (int) context.getResources().getDimension(R.dimen.ic_clear_margin);
        deleteIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete);
        deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        initiated = true;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target)
    {
        return false;
    }

    @Override
    public abstract void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction);

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder)
    {
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    /**  logic for drawing Canvas */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive)
    {
        View itemView = viewHolder.itemView;
        if (!initiated)
        {
            init();
        }
        int itemHeight = itemView.getBottom() - itemView.getTop();

        // Setting Swipe Background
        ((ColorDrawable) background).setColor(getLeftcolorCode());
        background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),
                itemView.getRight(), itemView.getBottom());
        background.draw(c);

        int intrinsicWidth = deleteIcon.getIntrinsicWidth();
        int intrinsicHeight = deleteIcon.getIntrinsicWidth();

        int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
        int xMarkRight = itemView.getRight() - xMarkMargin;
        int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int xMarkBottom = xMarkTop + intrinsicHeight;

        // Setting Swipe Icon
        deleteIcon.setBounds(xMarkLeft, xMarkTop + 16, xMarkRight, xMarkBottom);
        deleteIcon.draw(c);

        // Setting Swipe Text
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(getLeftSwipeLabel(), xMarkLeft + 40, xMarkTop + 10, paint);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private String getLeftSwipeLabel() {
        return leftSwipeLabel;
    }
    public void setLeftSwipeLabel(String leftSwipeLabel) {
        this.leftSwipeLabel = leftSwipeLabel;
    }

    private int getLeftcolorCode() {
        return leftcolorCode;
    }
    public void setLeftcolorCode(int leftcolorCode)
    {
        this.leftcolorCode = leftcolorCode;
    }
}
