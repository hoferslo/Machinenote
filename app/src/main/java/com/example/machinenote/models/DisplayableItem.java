package com.example.machinenote.models;

import java.util.Map;

public interface DisplayableItem {
    /**
     * Returns a map of displayable fields: Label -> Value.
     */
    Map<String, String> getDisplayFields();
}
