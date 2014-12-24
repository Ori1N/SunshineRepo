package com.example.ori.sunshine;

/**
 * A callback interface that all activities containing this fragment must
 * implement. This mechanism allows activities to be notified of item
 * selections.
 */
public interface Callback {
    /**
     * Callback for when an item has been selected.
     */
    public void onItemSelected(String date);
}