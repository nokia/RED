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
