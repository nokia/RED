package org.robotframework.ide.core.testData.model.util;

import java.util.Iterator;
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
public class MovableLinkedListWrapper<ElementType> implements
        Iterable<ElementType> {

    private final LinkedList<ElementType> wrappedList;


    /**
     * Creates wrapped linked list by itself
     */
    public MovableLinkedListWrapper() {
        wrappedList = new LinkedList<ElementType>();
    }


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


    /**
     * 
     * @param e
     *            element to add
     * @return answer if operation of add was successful
     */
    public boolean add(ElementType e) {
        return wrappedList.add(e);
    }


    /**
     * @param index
     *            of element to get
     * @return
     */
    public ElementType get(int index) {
        return wrappedList.get(index);
    }


    /**
     * @param index
     *            element to remove
     */
    public void delete(int index) {
        wrappedList.remove(index);
    }


    /**
     * @return size of wrapped list
     */
    public int size() {
        return wrappedList.size();
    }


    /**
     * @return iterator from wrapped linked list
     */
    public Iterator<ElementType> iterator() {
        return wrappedList.iterator();
    }
}
