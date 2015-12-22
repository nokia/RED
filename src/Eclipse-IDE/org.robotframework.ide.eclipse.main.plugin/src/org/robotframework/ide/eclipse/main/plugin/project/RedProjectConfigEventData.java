/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import org.eclipse.core.resources.IFile;

/**
 * @author Michal Anglart
 *
 */
public final class RedProjectConfigEventData<T> {

    private final IFile underlyingFile;

    private final T changedElement;

    public RedProjectConfigEventData(final IFile file, final T changedElement) {
        this.underlyingFile = file;
        this.changedElement = changedElement;
    }

    public IFile getUnderlyingFile() {
        return underlyingFile;
    }

    public T getChangedElement() {
        return changedElement;
    }

}
