package org.robotframework.ide.core.executor;

import java.io.File;

/**
 * @author mmarzec
 *
 */
public interface IExecutor {

    int execute(String testPath, File projectLocation);
    
    void addOutputStreamListener(IRobotLogOutputStreamListener listener);
}
