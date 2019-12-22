package eu.javimar.notitas.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.javimar.notitas.R;
import eu.javimar.notitas.interfaces.NotasItemClickListener;
import eu.javimar.notitas.util.SimpleItemTouchHelperCallback;
import eu.javimar.notitas.view.adapter.NotasAdapter;
import eu.javimar.notitas.viewmodel.NotitasViewModel;

public class FragmentNotaList extends Fragment implements NotasItemClickListener
{
    @BindView(R.id.rv_nota_recycler) RecyclerView mRecyclerView;
    @BindView(R.id.tv_empty_view) TextView mEmptyView;

    // Reference to implementation de OnNotaItemSelectedListener by MainActivity
    private OnNotaItemSelectedListener mListener;

    // Adapter
    private NotasAdapter mNotasAdapter;

    private NotitasViewModel mViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_list_nota, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));
        mNotasAdapter = new NotasAdapter(this,getActivity());
        mRecyclerView.setAdapter(mNotasAdapter);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(mNotasAdapter, getActivity());
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        observerSetup();
    }

    private void observerSetup()
    {
        mViewModel = new ViewModelProvider(this).get(NotitasViewModel.class);
        mViewModel.getAllNotasSorted().observe(getViewLifecycleOwner(),
                notas ->
                {
                    mNotasAdapter.setNotasList(notas);
                    if(notas == null || notas.size() <= 0)
                    {
                        mEmptyView.setVisibility(View.VISIBLE);
                        mEmptyView.setText(R.string.err_no_notes_found);
                    }
                    else
                    {
                        mEmptyView.setVisibility(View.GONE);
                    }
                });
    }

    public void passSearchResults(String query)
    {
        // SQL wildcards %
        mViewModel.getSearchResults("%" + query + "%");
        mViewModel.getSearchQueryResults().observe(getViewLifecycleOwner(), notas ->
        {
            mNotasAdapter.setNotasList(notas);
            if(notas == null || notas.size() <= 0)
            {
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setText(R.string.no_notes_found);
            }
            else
            {
                mEmptyView.setVisibility(View.GONE);
            }
        });
    }

    public void resetRecycler()
    {
        observerSetup();
    }

    @Override
    public void onNotaItemClick(int clickedNotaId)
    {
        // Holds the current id for the nota being selected
        // Pass it to MainActivity for the fragment
        mListener.onNotaItemSelected(clickedNotaId);
    }

    // MainActivity must implement this interface
    public interface OnNotaItemSelectedListener
    {
        void onNotaItemSelected(int id);
    }
    public void setNotaItemListener(OnNotaItemSelectedListener listener)
    {
        this.mListener = listener;
    }
}
