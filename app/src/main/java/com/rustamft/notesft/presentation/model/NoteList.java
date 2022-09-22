package com.rustamft.notesft.presentation.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.stream.Collectors;

public class NoteList {

    public final MutableLiveData<String> filter = new MutableLiveData<>();
    private final LiveData<List<String>> mLiveData;
    private final MutableLiveData<List<String>> mFilteredLiveData = new MutableLiveData<>();
    private final Observer<String> mFilterObserver = new Observer<>() {
        @Override
        public void onChanged(String text) {
            List<String> list = mLiveData.getValue();
            if (list == null) return;
            mFilteredLiveData.postValue(list.stream()
                    .filter(string -> string.contains(text))
                    .collect(Collectors.toList()));
        }
    };
    private final Observer<List<String>> mLiveDataObserver = list -> {
        String filterValue = filter.getValue();
        List<String> filteredList;
        if (filterValue == null) {
            filteredList = list;
        } else {
            filteredList = list.stream()
                    .filter(string -> string.contains(filterValue))
                    .collect(Collectors.toList());
        }
        mFilteredLiveData.postValue(filteredList);
    };

    public NoteList(LiveData<List<String>> initialLiveData) {
        mLiveData = initialLiveData;
        filter.observeForever(mFilterObserver);
        mLiveData.observeForever(mLiveDataObserver);
    }

    public void clear() {
        filter.removeObserver(mFilterObserver);
        mLiveData.removeObserver(mLiveDataObserver);
    }

    public LiveData<List<String>> getFilteredLiveData() {
        return mFilteredLiveData;
    }
}
