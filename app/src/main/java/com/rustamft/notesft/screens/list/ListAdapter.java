package com.rustamft.notesft.screens.list;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rustamft.notesft.R;

class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private final LayoutInflater mInflater;
    private String[] mNotesArray; // Cached copy of notes list

    ListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        if (mNotesArray != null) {
            String current = mNotesArray[position];
            holder.mNoteItemView.setText(current);
        }
    }

    @Override
    public int getItemCount() {
        if (mNotesArray != null) {
            return mNotesArray.length;
        } else {
            return 0;
        }
    }

    void setNotesList(String[] notesList) {
        mNotesArray = notesList;
        notifyDataSetChanged();
    }

    String getNoteAtPosition(int position) {
        return mNotesArray[position];
    }

    void onItemClick(String itemName) {
    }

    /*
    /////////////////////////// ViewHolder ///////////////////////////
    */

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
}