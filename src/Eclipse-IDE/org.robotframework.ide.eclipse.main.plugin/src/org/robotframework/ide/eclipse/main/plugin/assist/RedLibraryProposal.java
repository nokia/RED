/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.LibrarySpecificationInput;

class RedLibraryProposal extends BaseAssistProposal {

    private final RobotProject robotProject;

    private final LibrarySpecification librarySpecification;

    private final boolean isImported;

    RedLibraryProposal(final RobotProject robotProject, final LibrarySpecification librarySpecification,
            final boolean isImported, final ProposalMatch match) {
        super(librarySpecification.getName(), match);
        this.robotProject = robotProject;
        this.librarySpecification = librarySpecification;
        this.isImported = isImported;
    }

    boolean isImported() {
        return isImported;
    }

    @Override
    public List<String> getArguments() {
        return librarySpecification.getDescriptor().getArguments();
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getLibraryImage();
    }

    @Override
    public String getLabel() {
        final List<String> args = getArguments();
        if (args.isEmpty()) {
            return super.getLabel();
        } else {
            return super.getLabel() + " " + String.join(" ", args);
        }
    }

    @Override
    public StyledString getStyledLabel() {
        final StyledString label = super.getStyledLabel();
        if (isImported) {
            label.append(" (already imported)", StyledString.DECORATIONS_STYLER);
        }
        return label;
    }

    @Override
    public boolean isDocumented() {
        return true;
    }

    @Override
    public String getDescription() {
        return librarySpecification.getDocumentation();
    }

    @Override
    public DocumentationViewInput getDocumentationInput() {
        return new LibrarySpecificationInput(robotProject, librarySpecification);
    }
}
