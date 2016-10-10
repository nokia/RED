/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import java.util.List;

import org.eclipse.jface.text.hyperlink.IHyperlink;

public interface ITableHyperlinksDetector {

    List<IHyperlink> detectHyperlinks(int row, int column, final String label, final int indexInLabel);

}
