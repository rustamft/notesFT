package com.rustamft.notesft.presentation.model;

import android.view.View;

import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import com.rustamft.notesft.domain.model.Note;

public class ObservableNote {

    public final ObservableField<Note> note = new ObservableField<>();
    public final ObservableField<String> name = new ObservableField<>("");
    public final ObservableField<String> text = new ObservableField<>("");
    public final ObservableField<Integer> fabVisibility = new ObservableField<>(View.GONE);
    private String mInitialText = "";
    private final Observable.OnPropertyChangedCallback mOnTextChangedCallback =
            new Observable.OnPropertyChangedCallback() {
                @Override
                public void onPropertyChanged(Observable sender, int propertyId) {
                    String textValue = text.get();
                    if (textValue != null && !textValue.equals(mInitialText)) {
                        fabVisibility.set(View.VISIBLE);
                        text.removeOnPropertyChangedCallback(this);
                    }
                }
            };

    public ObservableNote() {
        note.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                setFieldsFromNote();
            }
        });
    }

    private void setFieldsFromNote() {
        Note note = this.note.get();
        if (note == null) return;
        name.set(note.name);
        if (mInitialText.isEmpty()) {
            mInitialText = note.text;
        }
        String textValue = text.get();
        if (textValue != null && textValue.isEmpty()) {
            text.set(note.text);
            text.addOnPropertyChangedCallback(mOnTextChangedCallback);
        }
    }
}
