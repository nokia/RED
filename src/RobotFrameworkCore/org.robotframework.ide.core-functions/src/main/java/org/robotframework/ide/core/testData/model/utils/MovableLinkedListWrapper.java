package org.robotframework.ide.core.testData.model.utils;

import java.util.LinkedList;

/**
 * Gives possibility to move up and down elements in {@link LinkedList}
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * @param <ElementType>
 *            type of objects in wrapped linked list
 */
public class MovableLinkedListWrapper<ElementType> {

    private final LinkedList<ElementType> wrappedList;

    /**
     * @param wrappedList
     */
    public MovableLinkedListWrapper(LinkedList<ElementType> wrappedList) {
        this.wrappedList = wrappedList;
    }

    /**
     * @param index
     *            current element index to move upper
     * @return true in case element is not 0 and exists
     */
    public boolean moveUp(int index) {
        boolean wasMoved = false;
        if (index >= 1 && wrappedList.size() > index) {
            wrappedList.add(index - 1, wrappedList.get(index));
            wrappedList.remove(index + 1);
            wasMoved = true;
        }
        return wasMoved;
    }

    /**
     * @param index
     *            current element to move down
     * @return true in case element is between 0 and is not last element index
     */
    public boolean moveDown(int index) {
        boolean wasMoved = false;
        if (index >= 0 && wrappedList.size() > index) {
            wasMoved = moveUp(index + 1);
        }
        return wasMoved;
    }
}
