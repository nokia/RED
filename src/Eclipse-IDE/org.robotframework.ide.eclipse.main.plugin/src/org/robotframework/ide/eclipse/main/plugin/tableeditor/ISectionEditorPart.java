/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;

public interface ISectionEditorPart {

    String SECTION_FILTERING_TOPIC = "red/suite_editor/section/filter/changed";

    String getId();

    void updateOnActivation();

    com.google.common.base.Optional<? extends RobotSuiteFileSection> provideSection(RobotSuiteFile suiteModel);

    boolean isPartFor(RobotSuiteFileSection section);

    void revealElement(RobotElement robotElement);

    void setFocus();

    SelectionLayerAccessor getSelectionLayerAccessor();
    
    com.google.common.base.Optional<TreeLayerAccessor> getTreeLayerAccessor();

    void waitForPendingJobs();
}
