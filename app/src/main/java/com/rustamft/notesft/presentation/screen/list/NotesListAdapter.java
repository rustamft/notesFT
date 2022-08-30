package com.rustamft.notesft.presentation.screen.list;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rustamft.notesft.R;
import com.rustamft.notesft.databinding.ListItemBinding;

import java.util.List;
import java.util.Objects;

public class NotesListAdapter extends ListAdapter<String, NotesListAdapter.ViewHolder> {

    private ListViewModel mViewModel; // TODO: fix, this leaks viewmodel
    private final LiveData<List<String>> mNoteNameListLiveData; // Cached copy of notes list

    NotesListAdapter(
            LifecycleOwner lifecycleOwner,
            ListViewModel viewModel,
            LiveData<List<String>> noteNameListLiveData
    ) {
        super(new DiffCallback());
        mViewModel = viewModel;
        mNoteNameListLiveData = noteNameListLiveData;
        mNoteNameListLiveData.observe(
                lifecycleOwner,
                this::submitList
        );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemBinding binding = ListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        binding.setViewModel(mViewModel);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String current = getNoteAtPosition(position);
        holder.bind(current);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mViewModel = null;
    }

    @Override
    public int getItemCount() {
        if (mNoteNameListLiveData.getValue() == null) {
            return 0;
        } else {
            return mNoteNameListLiveData.getValue().size();
        }
    }

    String getNoteAtPosition(int position) {
        return Objects.requireNonNull(mNoteNameListLiveData.getValue()).get(position);
    }


    protected static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {

        private final ListItemBinding mBinding;

        ViewHolder(ListItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(), 0, 0, R.string.action_remove);
        }

        void bind(String string) {
            mBinding.textView.setText(string);
            mBinding.textView.setOnCreateContextMenuListener(this);
        }
    }


    private static class DiffCallback extends DiffUtil.ItemCallback<String> {

        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return areItemsTheSame(oldItem, newItem);
        }
    }
}
