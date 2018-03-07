/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.search.ui.text.Match;
import org.rf.ide.core.libraries.LibrarySpecification;


/**
 * @author Michal Anglart
 */
public class LibraryDocumentationMatch extends Match implements DocumentationMatch {

    private final LibrarySpecification librarySpecification;

    public LibraryDocumentationMatch(final IProject project, final LibrarySpecification librarySpecification,
            final int offset, final int length) {
        super(new MatchesGroupingElement(project, librarySpecification), UNIT_CHARACTER, offset, length);
        this.librarySpecification = librarySpecification;
    }

    @Override
    public StyledString getStyledLabel() {
        return new MatchLabelCreator().create(librarySpecification.getDocumentation(),
                new Position(getOffset(), getLength()));
    }
}
