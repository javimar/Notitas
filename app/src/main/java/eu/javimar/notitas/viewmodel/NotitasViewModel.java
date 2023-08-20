package eu.javimar.notitas.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import eu.javimar.notitas.model.Nota;

public class NotitasViewModel extends AndroidViewModel {
    private final NotitasRepository repository;
    private final LiveData<List<Nota>> allNotasSorted;
    private final MutableLiveData<List<Nota>> searchQueryResults;

    public NotitasViewModel(@NonNull Application application) {
        super(application);
        repository = new NotitasRepository(application);
        allNotasSorted = repository.getAllNotasSorted();
        searchQueryResults = repository.getSearchQueryResults();
    }

    // GETTERS to be called form the UI
    public MutableLiveData<List<Nota>> getSearchQueryResults() {
        return searchQueryResults;
    }

    public void getSearchResults(String query) {
        repository.getSearchResults(query);
    }

    public LiveData<List<Nota>> getAllNotasSorted() {
        return allNotasSorted;
    }

    public Nota findNota(int id) {
        return repository.findNota(id);
    }

    public long insertNota(Nota newNota) {
        return repository.insertNota(newNota);
    }

    public void deleteNota(int id) {
        repository.deleteNota(id);
    }

    public void updateNota(Nota nota) {
        repository.updateNota(nota);
    }

    public void swapNotas(int current, int newId) {
        repository.swapNotas(current, newId);
    }
}
