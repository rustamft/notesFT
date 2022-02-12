package com.rustamft.notesft.screens.list;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rustamft.notesft.R;
import com.rustamft.notesft.databinding.ListItemBinding;

import java.util.List;
import java.util.Objects;

public class NotesListAdapter extends ListAdapter<String, NotesListAdapter.ViewHolder> {

    private final ListFragment owner;
    private final LiveData<List<String>> notesList; // Cached copy of notes list

    NotesListAdapter(ListFragment owner, LiveData<List<String>> notesList) {
        super(new DiffCallback());
        this.owner = owner;
        this.notesList = notesList;
        this.notesList.observe(this.owner.getViewLifecycleOwner(), o -> submitList(this.notesList.getValue()));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemBinding binding = ListItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        binding.setFragment(owner);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String current = getNoteAtPosition(position);
        holder.bind(current);
    }

    @Override
    public int getItemCount() {
        if (notesList.getValue() == null) {
            return 0;
        } else {
            return notesList.getValue().size();
        }
    }

    String getNoteAtPosition(int position) {
        return Objects.requireNonNull(notesList.getValue()).get(position);
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