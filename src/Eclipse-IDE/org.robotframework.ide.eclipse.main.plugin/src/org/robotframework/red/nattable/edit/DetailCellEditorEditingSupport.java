/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

/**
 * @author Michal Anglart
 *
 */
public interface DetailCellEditorEditingSupport<D> {

    List<D> getInput(final int column, final int row);

    List<D> getDetailElements();

    DetailCellEditorEntry<D> createDetailEntry(final Composite parent, final D detail);

    void addNewDetailElement(String newElementContent);

    void removeDetailElements(List<D> elements);

    void moveLeft(List<D> detailsToMove);

    void moveRight(List<D> detailsToMove);

    void setNewValue(D oldValue, String value);
    
}
