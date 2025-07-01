package com.example.machinenote.models;

public class ListViewItem {
    private String name;
    private boolean completed;
    private boolean visible;
    private int number;
    private String errorType; // New field to identify error type

    public ListViewItem(String name, boolean completed, int number) {
        this.name = name;
        this.completed = completed;
        this.visible = true;
        this.number = number;
        this.errorType = null;
    }

    // Constructor for error items
    public ListViewItem(String name, boolean completed, int number, String errorType) {
        this.name = name;
        this.completed = completed;
        this.visible = false; // Error items start hidden
        this.number = number;
        this.errorType = errorType;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public boolean isErrorItem() {
        return errorType != null;
    }
}