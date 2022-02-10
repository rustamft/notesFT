package com.rustamft.notesft.screens.list;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rustamft.notesft.R;

import java.util.List;
import java.util.Objects;

class NotesListAdapter extends ListAdapter<String, NotesListAdapter.ListViewHolder> {

    private final Fragment mOwner;
    private final LiveData<List<String>> mLiveData; // Cached copy of notes list

    NotesListAdapter(Fragment owner, LiveData<List<String>> liveData) {
        super(new DiffCallback());
        mOwner = owner;
        mLiveData = liveData;
        mLiveData.observe(mOwner.getViewLifecycleOwner(), o -> submitList(mLiveData.getValue()));
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mOwner.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new ListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        String current = getNoteAtPosition(position);
        holder.mNoteItemView.setText(current);
    }

    @Override
    public int getItemCount() {
        if (mLiveData.getValue() == null) {
            return 0;
        } else {
            return mLiveData.getValue().size();
        }
    }

    String getNoteAtPosition(int position) {
        return Objects.requireNonNull(mLiveData.getValue()).get(position);
    }

    void onItemClick(String itemName) {
        // To be overridden by an upper class.
    }



    class ListViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnCreateContextMenuListener {

        final View mView;
        private final TextView mNoteItemView;

        private ListViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mNoteItemView = itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            String noteName = getNoteAtPosition(position);
            onItemClick(noteName);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(), 0, 0, R.string.action_remove);
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