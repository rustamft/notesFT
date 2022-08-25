package com.rustamft.notesft.presentation.screen.list;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rustamft.notesft.R;
import com.rustamft.notesft.databinding.ListItemBinding;

import java.util.List;
import java.util.Objects;

public class NotesListAdapter extends ListAdapter<String, NotesListAdapter.ViewHolder> {

    private final ListViewModel mViewModel;
    private final LiveData<List<String>> mNoteNameList; // Cached copy of notes list

    NotesListAdapter(
            Fragment owner,
            ListViewModel viewModel
    ) {
        super(new DiffCallback());
        mViewModel = viewModel;
        mNoteNameList = viewModel.getNoteNameList();
        mNoteNameList.observe(
                owner.getViewLifecycleOwner(),
                o -> submitList(mNoteNameList.getValue())
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
    public int getItemCount() {
        if (mNoteNameList.getValue() == null) {
            return 0;
        } else {
            return mNoteNameList.getValue().size();
        }
    }

    String getNoteAtPosition(int position) {
        return Objects.requireNonNull(mNoteNameList.getValue()).get(position);
    }


    static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {

        private final ListItemBinding binding;

        ViewHolder(ListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(), 0, 0, R.string.action_remove);
        }

        void bind(String string) {
            binding.textView.setText(string);
            binding.textView.setOnCreateContextMenuListener(this);
        }
    }


    static class DiffCallback extends DiffUtil.ItemCallback<String> {

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
