package com.rustamft.notesft.presentation.model;

import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import com.rustamft.notesft.domain.model.Note;

public class ObservableNote {

    public final ObservableField<Note> note = new ObservableField<>();
    public final ObservableField<String> name = new ObservableField<>("");
    public final ObservableField<String> text = new ObservableField<>("");
    public final ObservableField<Boolean> textChangedByUser = new ObservableField<>(false);

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
                textChangedByUser.set(true);
                text.removeOnPropertyChangedCallback(this);
            }
        });
    }
}
