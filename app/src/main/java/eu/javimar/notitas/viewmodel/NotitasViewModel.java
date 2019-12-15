package eu.javimar.notitas.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import eu.javimar.notitas.model.Nota;

public class NotitasViewModel extends AndroidViewModel
{
    private final NotitasRepository repository;
    private final LiveData<List<Nota>> allNotasSorted;

    public NotitasViewModel(@NonNull Application application)
    {
        super(application);
        repository = new NotitasRepository(application);
        allNotasSorted = repository.getAllNotasSorted();
    }

    // GETTERS to be called form the UI
    public LiveData<List<Nota>> getAllNotasSorted()
    {
        return allNotasSorted;
    }

    public Nota findNota(int id)
    {
        return repository.findNota(id);
    }

    public long insertNota(Nota newNota)
    {
        return repository.insertNota(newNota);
    }

    public void deleteNota(int id)
    {
        repository.deleteNota(id);
    }

    public void updateNota(Nota nota)
    {
        repository.updateNota(nota);
    }
}
