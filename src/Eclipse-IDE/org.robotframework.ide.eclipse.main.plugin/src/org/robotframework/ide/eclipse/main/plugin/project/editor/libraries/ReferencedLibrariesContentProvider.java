/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlArgumentsVariant.RedXmlRemoteArgumentsVariant;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlLibrary.RedXmlRemoteLib;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.TreeContentProvider;

class ReferencedLibrariesContentProvider extends TreeContentProvider {

    @Override
    public Object[] getElements(final Object inputElement) {
        final RobotProjectConfig config = (RobotProjectConfig) inputElement;

        final List<Object> elements = new ArrayList<>();

        final RedXmlRemoteLib remoteLib = new RedXmlRemoteLib();
        final List<RedXmlArgumentsVariant> remoteVariants = config.getRemoteLocations()
                .stream()
                .map(r -> new RedXmlRemoteArgumentsVariant(remoteLib, r))
                .collect(toList());
        remoteLib.setVariants(remoteVariants);
        elements.add(remoteLib);

        for (final ReferencedLibrary refLib : config.getReferencedLibraries()) {
            final RedXmlLibrary lib = new RedXmlLibrary(refLib);
            elements.add(lib);

            final List<RedXmlArgumentsVariant> variants = refLib.getArgumentsVariants()
                    .stream()
                    .map(v -> new RedXmlArgumentsVariant(lib, v))
                    .collect(toList());
            lib.setVariants(variants);
        }

        elements.add(new ElementAddingToken("library file", true));
        return elements.toArray();
    }

    @Override
    public boolean hasChildren(final Object element) {
        return element instanceof RedXmlLibrary;
    }

    @Override
    public Object[] getChildren(final Object element) {
        if (element instanceof RedXmlLibrary) {
            final RedXmlLibrary lib = (RedXmlLibrary) element;
            return lib.getVariants().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(final Object element) {
        return element instanceof RedXmlArgumentsVariant ? ((RedXmlArgumentsVariant) element).getParent() : null;
    }
}
