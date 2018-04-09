/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;

public interface AssistProposal {

    String getContent();

    List<String> getArguments();

    ImageDescriptor getImage();

    String getLabel();

    StyledString getStyledLabel();

    boolean isDocumented();

    String getDescription();

    DocumentationViewInput getDocumentationInput();
}
