package org.robotframework.ide.core.testData.model.listener;

import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotFileOutput;

public interface IRobotModelEventDispatcher {

    void dispatchEvent(final ARobotModelEvent<?> event);


    IRobotFileOutput listener(final RobotFileOutput robotOutput);

}
