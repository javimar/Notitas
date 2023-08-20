package eu.javimar.notitas.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import eu.javimar.notitas.R;
import eu.javimar.notitas.databinding.FragmentListNotaBinding;
import eu.javimar.notitas.interfaces.NotasItemClickListener;
import eu.javimar.notitas.util.SimpleItemTouchHelperCallback;
import eu.javimar.notitas.view.adapter.NotasAdapter;
import eu.javimar.notitas.viewmodel.NotitasViewModel;

public class FragmentNotaList extends Fragment implements NotasItemClickListener {
    
    private FragmentListNotaBinding binding;

    // Reference to implementation de OnNotaItemSelectedListener by MainActivity
    private OnNotaItemSelectedListener mListener;
    private NotasAdapter mNotasAdapter;
    private NotitasViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentListNotaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        binding.rvNotaRecycler.setHasFixedSize(true);
        binding.rvNotaRecycler.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));
        mNotasAdapter = new NotasAdapter(this, getActivity());
        binding.rvNotaRecycler.setAdapter(mNotasAdapter);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(mNotasAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(binding.rvNotaRecycler);

        observerSetup();
    }
    private void observerSetup() {
        mViewModel = new ViewModelProvider(this).get(NotitasViewModel.class);
        mViewModel.getAllNotasSorted().observe(getViewLifecycleOwner(),
                notas ->
                {
                    mNotasAdapter.setNotasList(notas);
                    if (notas == null || notas.size() == 0) {
                        binding.tvEmptyView.setVisibility(View.VISIBLE);
                        binding.tvEmptyView.setText(R.string.err_no_notes_found);
                    } else {
                        binding.tvEmptyView.setVisibility(View.GONE);
                    }
                });
    }

    public void passSearchResults(String query) {
        // SQL wildcards %
        mViewModel.getSearchResults("%" + query + "%");
        mViewModel.getSearchQueryResults().observe(getViewLifecycleOwner(), notas ->
        {
            mNotasAdapter.setNotasList(notas);
            if (notas == null || notas.size() == 0) {
                binding.tvEmptyView.setVisibility(View.VISIBLE);
                binding.tvEmptyView.setText(R.string.no_notes_found);
            } else {
                binding.tvEmptyView.setVisibility(View.GONE);
            }
        });
    }

    public void resetRecycler() {
        observerSetup();
    }

    @Override
    public void onNotaItemClick(int clickedNotaId) {
        mListener.onNotaItemSelected(clickedNotaId);
    }

    public interface OnNotaItemSelectedListener {
        void onNotaItemSelected(int id);
    }

    public void setNotaItemListener(OnNotaItemSelectedListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
