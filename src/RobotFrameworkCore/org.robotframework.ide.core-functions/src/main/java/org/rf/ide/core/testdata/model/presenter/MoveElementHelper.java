/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MoveElementHelper {

    public boolean moveLeft(@SuppressWarnings("rawtypes") final Collection list, final Object toMoveLeft) {
        return moveUp(list, toMoveLeft);
    }

    @SuppressWarnings("unchecked")
    public boolean moveUp(@SuppressWarnings("rawtypes") final Collection list, final Object toMoveUp) {
        boolean result = false;
        if (list.size() >= 2) {
            final List<?> listCopy = new ArrayList<>(list);
            int elemIndex = listCopy.indexOf(toMoveUp);
            if (elemIndex > 0) {
                Collections.swap(listCopy, elemIndex, elemIndex - 1);
                synchronized (list) {
                    list.clear();
                    list.addAll(listCopy);
                }
                result = true;
            }
        }
        return result;
    }

    public boolean moveRight(@SuppressWarnings("rawtypes") final Collection list, final Object toMoveRight) {
        return moveDown(list, toMoveRight);
    }

    @SuppressWarnings("unchecked")
    public boolean moveDown(@SuppressWarnings("rawtypes") final Collection list, final Object toMoveDown) {
        boolean result = false;
        if (list.size() >= 2) {
            final List<?> listCopy = new ArrayList<>(list);
            int elemIndex = listCopy.indexOf(toMoveDown);
            if (elemIndex >= 0 && elemIndex < listCopy.size() - 1) {
                Collections.swap(listCopy, elemIndex, elemIndex + 1);
                synchronized (list) {
                    list.clear();
                    list.addAll(listCopy);
                }
                result = true;
            }
        }
        return result;
    }
}
