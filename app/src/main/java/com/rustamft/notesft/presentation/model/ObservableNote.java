package com.rustamft.notesft.presentation.model;

import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import com.rustamft.notesft.domain.model.Note;

public class ObservableNote {

    public ObservableField<Note> note = new ObservableField<>();
    public ObservableField<String> name = new ObservableField<>("");
    public ObservableField<String> text = new ObservableField<>("");
    public ObservableField<Boolean> textChanged = new ObservableField<>(false);
    // TODO: fix FAB visibility

    public ObservableNote() {
        note.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                Note noteValue = note.get();
                if (noteValue != null) {
                    setFieldsFromNote(noteValue);
                }
            }
        });
    }

    private void setFieldsFromNote(Note note) {
        name.set(note.name);
        text.set(note.text);
        text.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                textChanged.set(true);
                text.removeOnPropertyChangedCallback(this);
            }
        });
    }
}
