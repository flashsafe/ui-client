package ru.flashsafe.client.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 * @author Andrew
 *
 * @param <T> history record type
 */
public class HistoryObject<T> {
    
    private List<T> historyList = new ArrayList<>();
    
    private int currentPosition = -1;
    
    public HistoryObject() {
    }
    
    public boolean hasPrevious() {
        return currentPosition > 0;
    }
    
    public synchronized T previous() {
        if (!hasPrevious()) {
            throw new IllegalStateException("No entries available");
        }
        currentPosition--;
        return historyList.get(currentPosition);
    }
    
    public boolean hasNext() {
        return currentPosition < historyList.size() - 1;
    }
    
    public synchronized T next() {
        if (!hasNext()) {
            throw new IllegalStateException("No entries available");
        }
        currentPosition++;
        return historyList.get(currentPosition);
    }
    
    public synchronized void addObject(T object) {
        currentPosition++;
        historyList = historyList.subList(0, currentPosition);
        historyList.add(object);
    }

}
