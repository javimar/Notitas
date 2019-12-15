package eu.javimar.notitas.viewmodel;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

import eu.javimar.notitas.database.MiBaseDatosNotas;
import eu.javimar.notitas.database.NotasDao;
import eu.javimar.notitas.model.Nota;

class NotitasRepository
{
    private final NotasDao notasDao;

    private final LiveData<List<Nota>> allNotasSorted;
    // Method that the ViewModel can call to obtain references to the allNotas
    LiveData<List<Nota>> getAllNotasSorted()
    {
        return allNotasSorted;
    }

    NotitasRepository(Application application)
    {
        MiBaseDatosNotas db;
        db = MiBaseDatosNotas.getDatabase(application);
        notasDao = db.notasDao();
        allNotasSorted = notasDao.getAllNotasSorted();
    }

    // FIND A NOTA LOGIC
    Nota findNota(int id)
    {
        FindAsyncTask task = new FindAsyncTask(notasDao);
        try { return task.execute(id).get(); }
        catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
        return null;
    }
    private static class FindAsyncTask extends AsyncTask<Integer, Void, Nota>
    {
        private final NotasDao asyncTaskDao;
        FindAsyncTask(NotasDao dao) { asyncTaskDao = dao; }
        @Override
        protected Nota doInBackground(final Integer... ids)
        {
            return asyncTaskDao.findNota(ids[0]);
        }
    }

    // INSERT LOGIC
    long insertNota(Nota newNota)
    {
        InsertAsyncTask task = new InsertAsyncTask(notasDao);
        try { return task.execute(newNota).get(); }
        catch (ExecutionException | InterruptedException e) { e.printStackTrace(); }
        return -1;
    }
    private static class InsertAsyncTask extends AsyncTask<Nota, Void, Long>
    {
        private final NotasDao asyncTaskDao;
        InsertAsyncTask(NotasDao dao) { asyncTaskDao = dao; }
        @Override
        protected Long doInBackground(final Nota... notas)
        {
            return asyncTaskDao.insert(notas[0]);
        }
    }

    // DELETE LOGIC
    void deleteNota(int id)
    {
        DeleteAsyncTask task = new DeleteAsyncTask(notasDao);
        task.execute(id);
    }
    private static class DeleteAsyncTask extends AsyncTask<Integer, Void, Void>
    {
        private final NotasDao asyncTaskDao;
        DeleteAsyncTask(NotasDao dao) { asyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Integer... ids)
        {
            asyncTaskDao.deleteNota(ids[0]);
            return null;
        }
    }

    // UPDATE LOGIC
    void updateNota(Nota nota)
    {
        UpdateAsyncTask task = new UpdateAsyncTask(notasDao);
        task.execute(nota);
    }
    private static class UpdateAsyncTask extends AsyncTask<Nota, Void, Void>
    {
        private final NotasDao asyncTaskDao;
        UpdateAsyncTask(NotasDao dao) { asyncTaskDao = dao; }
        @Override
        protected Void doInBackground(final Nota... notas)
        {
            asyncTaskDao.updateNote(notas[0]);
            return null;
        }
    }


    /**
     * SEARCH FUNCTIONALITY
     */
    private final MutableLiveData<List<Nota>> searchQueryResults = new MutableLiveData<>();
    private void asyncSearchFinished(List<Nota> queryResults) { searchQueryResults.setValue(queryResults); }
    MutableLiveData<List<Nota>> getSearchQueryResults() { return searchQueryResults; }
    void getSearchResults(String query)
    {
        SearchAsyncTask task = new SearchAsyncTask(notasDao);
        task.delegate = this;
        task.execute(query);
    }
    private static class SearchAsyncTask extends AsyncTask<String, Void, List<Nota>>
    {
        private final NotasDao asyncTaskDao;
        private NotitasRepository delegate = null;
        SearchAsyncTask (NotasDao dao)
        {
            asyncTaskDao = dao;
        }
        @Override
        protected List<Nota> doInBackground(String... queries)
        {
            return asyncTaskDao.getSearchResults(queries[0]);
        }
        @Override
        protected void onPostExecute(List<Nota> nota)
        {
            delegate.asyncSearchFinished(nota);
        }
    }
}
