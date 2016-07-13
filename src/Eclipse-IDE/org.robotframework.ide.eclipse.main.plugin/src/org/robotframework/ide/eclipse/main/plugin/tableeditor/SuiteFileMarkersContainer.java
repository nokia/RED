package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause.Severity;

import com.google.common.base.Optional;

public interface SuiteFileMarkersContainer {

    Optional<Severity> getHighestSeverityMarkerFor(Optional<RobotFileInternalElement> element);

    List<String> getMarkersMessagesFor(Optional<RobotFileInternalElement> element);
}
