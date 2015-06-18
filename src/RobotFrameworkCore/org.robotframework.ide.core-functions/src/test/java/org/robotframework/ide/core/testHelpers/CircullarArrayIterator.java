package org.robotframework.ide.core.testHelpers;

import java.util.Iterator;


/**
 * This class provide functionality for iteration over Arrays.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @param <T>
 *            type of array
 */
public class CircullarArrayIterator<T> implements Iterator<T> {

    private final T[] array;
    private int nextIndex = -1;
    private final boolean[] skipped;


    public CircullarArrayIterator(final T[] array) {
        this.array = array;
        skipped = initSkippedArray(array);
        check();
    }


    private boolean[] initSkippedArray(T[] array) {
        boolean skipped[] = new boolean[0];

        if (array != null) {
            skipped = new boolean[array.length];
            for (int i = 0; i < array.length; i++) {
                skipped[i] = false;
            }
        }

        return skipped;
    }


    private void check() {
        int indexFound = -1;
        if (array != null) {
            int length = array.length;
            if (length > 0) {
                if (nextIndex + 1 < length) {
                    indexFound = nextIndex + 1;
                    if (skipped[indexFound]) {
                        indexFound = getNextFreeIndex(array, indexFound);
                    }
                } else {
                    indexFound = 0;
                    if (skipped[indexFound]) {
                        indexFound = getNextFreeIndex(array, indexFound);
                    }
                }
            }
        }

        this.nextIndex = indexFound;
    }


    private int getNextFreeIndex(T[] array, int indexFound) {
        int index = -1;
        // start search for free index from current position to array length
        for (int i = indexFound; i < array.length; i++) {
            if (!skipped[i]) {
                index = i;
                break;
            }
        }

        // start search for free index from index 0 to current position
        if (index == -1 && indexFound > 0) {
            for (int i = 0; i < indexFound; i++) {
                if (!skipped[i]) {
                    index = i;
                    break;
                }
            }
        }

        return index;
    }


    @Override
    public boolean hasNext() {
        return (nextIndex != -1);
    }


    @Override
    public T next() {
        T element = null;
        if (nextIndex != -1) {
            element = array[nextIndex];
            check();
        }

        return element;
    }


    @Override
    public void remove() {
        if (nextIndex != -1) {
            skipped[nextIndex] = true;
        }

        check();
    }
}
