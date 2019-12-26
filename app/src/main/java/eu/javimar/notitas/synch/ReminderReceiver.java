package eu.javimar.notitas.synch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import androidx.core.app.NotificationCompat;

import eu.javimar.notitas.database.MiBaseDatosNotas;
import eu.javimar.notitas.database.NotasDao;
import eu.javimar.notitas.util.NotificationHelper;

public class ReminderReceiver extends BroadcastReceiver
{
    // Called when reminder is fired
    @Override
    public void onReceive(Context context, Intent intent)
    {
        NotificationHelper mNotificationHelper = new NotificationHelper(context);

        String header = intent.getStringExtra("header");
        int notaId = intent.getIntExtra("notaId", 0);
        String msg = intent.getStringExtra("msg");
        int requestCode = intent.getIntExtra("requestCode", 0);

        NotificationCompat.Builder nb = mNotificationHelper
                .getChannelNotification(header, notaId, msg, requestCode);
        mNotificationHelper.getManager().notify(requestCode, nb.build());

        resetDatabaseAfterReminder(context, notaId);
    }

    private void resetDatabaseAfterReminder(Context context, int id)
    {
        MiBaseDatosNotas db;
        db = MiBaseDatosNotas.getDatabase(context);
        final NotasDao notasDao = db.notasDao();

        UpdateAfterReminderAsyncTask task = new UpdateAfterReminderAsyncTask(notasDao);
        task.execute(id);
    }

    private static class UpdateAfterReminderAsyncTask extends AsyncTask<Integer, Void, Void>
    {
        private final NotasDao asyncTaskDao;
        UpdateAfterReminderAsyncTask(NotasDao dao) { asyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Integer... ids)
        {
            asyncTaskDao.resetNotasAfterReminder(ids[0]);
            return null;
        }
    }
}