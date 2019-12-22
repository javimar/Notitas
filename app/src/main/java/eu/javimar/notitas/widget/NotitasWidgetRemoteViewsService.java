package eu.javimar.notitas.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

import eu.javimar.notitas.R;
import eu.javimar.notitas.database.MiBaseDatosNotas;
import eu.javimar.notitas.database.NotasDao;
import eu.javimar.notitas.model.Nota;
import eu.javimar.notitas.util.BitmapScaler;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static eu.javimar.notitas.MainActivity.deviceDensityIndependentPixels;
import static eu.javimar.notitas.util.Utils.isInternalUriPointingToValidResource;

public class NotitasWidgetRemoteViewsService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new NotitasRemoteViewsFactory(this.getApplicationContext());
    }

    class NotitasRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory
    {
        private final NotasDao notasDao;
        private List<Nota> allNotas;

        NotitasRemoteViewsFactory(Context context)
        {
            MiBaseDatosNotas db;
            db = MiBaseDatosNotas.getDatabase(context);
            notasDao = db.notasDao();
        }

        @Override
        public void onCreate() { }

        @Override
        public void onDataSetChanged()
        {
            allNotas = notasDao.getAllNotasForWidget();
        }

        @Override
            public int getCount()
            {
                return allNotas == null ? 0 :
                        allNotas.size();
            }

        @Override
        public RemoteViews getViewAt(int position)
        {
            if(position == AdapterView.INVALID_POSITION ||
                    allNotas == null)
            {
                return null;
            }
            RemoteViews remoteViews = new RemoteViews(getPackageName(),
                    R.layout.widget_detail_list_item);

            // set the values
            remoteViews.setTextViewText(R.id.widget_nota_title,
                    allNotas.get(position).getNotaTitulo());
            remoteViews.setTextViewText(R.id.widget_nota_body,
                    allNotas.get(position).getNotaCuerpo());

            // Load image in container via Glide
            String uri = allNotas.get(position).getNotaUriImage();
            if(uri == null)
            {
                remoteViews.setViewVisibility(R.id.widget_nota_image, GONE);
            }
            else
            {
                if(isInternalUriPointingToValidResource(Uri
                        .parse(uri), getApplicationContext()))
                {
                    remoteViews.setViewVisibility(R.id.widget_nota_image, VISIBLE);
                    // scale it to fit the width
                    Bitmap scaleImage = BitmapScaler
                            .scaleToFitWidth(
                                    BitmapFactory
                                            .decodeFile(Uri.parse(uri).getPath()),
                                    deviceDensityIndependentPixels[0]);

                    remoteViews.setImageViewBitmap(R.id.widget_nota_image, scaleImage);
                }
            }

            if(allNotas.get(position).getNotaUriAudio() != null)
                remoteViews.setViewVisibility(R.id.widget_nota_audio, VISIBLE);
            else
                remoteViews.setViewVisibility(R.id.widget_nota_audio, GONE);

            remoteViews.setTextColor(R.id.widget_list, Color.parseColor(
                    allNotas.get(position).getNotaColor()));
            remoteViews.setInt(R.id.widget_nota_title,"setTextColor",
                    R.color.black);
            remoteViews.setInt(R.id.widget_nota_body,"setTextColor",
                    R.color.black);
            remoteViews.setInt(R.id.widget_list_item,"setBackgroundColor",
                    Color.parseColor(allNotas.get(position).getNotaColor()));

            // Fill-in intent for each item in the ListView. This is combined with the
            // PendingIntent template in order to determine the final intent that will
            // be executed when the item is clicked.
            final Intent fillInIntent = new Intent();
            // Pass the id of the nota clicked
            fillInIntent.putExtra("notaId", allNotas.get(position).getNotaId());
            remoteViews.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView()
        {
            return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
        }

        @Override
        public int getViewTypeCount() { return 1; }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public boolean hasStableIds() { return true; }

        @Override
        public void onDestroy() { }
    }
}
