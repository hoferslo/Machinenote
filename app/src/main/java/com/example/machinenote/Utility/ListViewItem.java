package com.example.machinenote.Utility;

import android.util.Log;

public class ListViewItem {
    private String name;
    private boolean isCompleted;
    private int number;

    public ListViewItem(String name, boolean isCompleted, int number) {
        this.name = name;
        this.number = number;
        this.isCompleted = isCompleted;
    }

    public String getName() {
        return name;
    }

    public int getNumber(){
        return number;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
