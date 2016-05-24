/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter;

import java.util.Collections;
import java.util.List;

public class MoveElementHelper {

    public static <T> boolean moveLeft(final List<T> list, final T toMoveLeft) {
        return moveUp(list, toMoveLeft);
    }

    public static <T> boolean moveUp(final List<T> list, final T toMoveUp) {
        boolean result = false;
        if (list.size() >= 2) {
            int elemIndex = list.indexOf(toMoveUp);
            if (elemIndex > 0) {
                Collections.swap(list, elemIndex, elemIndex - 1);
                result = true;
            }
        }

        return result;
    }

    public static <T> boolean moveRight(final List<T> list, final T toMoveRight) {
        return moveDown(list, toMoveRight);
    }

    public static <T> boolean moveDown(final List<T> list, final T toMoveDown) {
        boolean result = false;
        if (list.size() >= 2) {
            int elemIndex = list.indexOf(toMoveDown);
            if (elemIndex >= 0 && elemIndex < list.size() - 1) {
                Collections.swap(list, elemIndex, elemIndex + 1);
                result = true;
            }
        }
        return result;
    }
}
