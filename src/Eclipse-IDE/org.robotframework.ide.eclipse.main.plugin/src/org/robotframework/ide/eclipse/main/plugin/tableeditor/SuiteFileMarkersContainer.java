/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.List;
import java.util.Optional;

import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;

public interface SuiteFileMarkersContainer {

    Optional<Severity> getHighestSeverityMarkerFor(Optional<? extends RobotFileInternalElement> element);

    boolean hasTaskMarkerFor(Optional<RobotFileInternalElement> rowObject);

    List<String> getMarkersMessagesFor(Optional<? extends RobotFileInternalElement> element);

}
