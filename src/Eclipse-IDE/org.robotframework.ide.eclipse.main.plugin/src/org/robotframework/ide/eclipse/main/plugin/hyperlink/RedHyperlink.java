/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.hyperlink.IHyperlink;


/**
 * @author Michal Anglart
 *
 */
public interface RedHyperlink extends IHyperlink {

    String getLabelForCompoundHyperlinksDialog();

    ImageDescriptor getImage();

    String additionalLabelDecoration();

}
