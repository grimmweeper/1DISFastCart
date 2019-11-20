package com.drant.FastCartMain.ui.scanitem;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScanItemViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ScanItemViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is scan item fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}