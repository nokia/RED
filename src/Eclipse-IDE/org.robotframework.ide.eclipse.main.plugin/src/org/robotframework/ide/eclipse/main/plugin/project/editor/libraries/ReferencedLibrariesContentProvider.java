/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.TreeContentProvider;

public class ReferencedLibrariesContentProvider extends TreeContentProvider {

    @Override
    public Object[] getElements(final Object inputElement) {
        final RobotProjectConfig config = (RobotProjectConfig) inputElement;

        final List<Object> elements = new ArrayList<>();

        elements.add(new RemoteLibraryViewItem(config));
        config.getReferencedLibraries().stream().forEach(elements::add);
        elements.add(new ElementAddingToken("library file", true));
        return elements.toArray();
    }

    @Override
    public boolean hasChildren(final Object element) {
        return element instanceof RemoteLibraryViewItem || element instanceof ReferencedLibrary;
    }

    @Override
    public Object[] getChildren(final Object element) {
        if (element instanceof RemoteLibraryViewItem) {
            final RemoteLibraryViewItem lib = (RemoteLibraryViewItem) element;
            return lib.getLocations().toArray();

        } else if (element instanceof ReferencedLibrary) {
            final ReferencedLibrary lib = (ReferencedLibrary) element;
            return lib.getArgumentsVariants().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(final Object element) {
        return element instanceof ReferencedLibraryArgumentsVariant
                ? ((ReferencedLibraryArgumentsVariant) element).getParent()
                : null;
    }

    public static class RemoteLibraryViewItem {

        private final RobotProjectConfig config;

        public RemoteLibraryViewItem(final RobotProjectConfig config) {
            this.config = config;
        }

        public List<RemoteLocation> getLocations() {
            return new ArrayList<>(config.getRemoteLocations());
        }

        @Override
        public int hashCode() {
            return Objects.hash(config);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            } else if (this == obj) {
                return true;
            } else if (getClass() == obj.getClass()) {
                final RemoteLibraryViewItem that = (RemoteLibraryViewItem) obj;
                return this.config == that.config;
            }
            return false;
        }

    }
}
