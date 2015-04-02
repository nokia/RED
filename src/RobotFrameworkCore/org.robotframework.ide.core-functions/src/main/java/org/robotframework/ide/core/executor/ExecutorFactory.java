package org.robotframework.ide.core.executor;

/**
 * @author mmarzec
 *
 */
public class ExecutorFactory {

    public IExecutor getExecutor(String executorName) {
        if(executorName.equals("pybot")) {
            return new PybotExecutor();
        }
        
        return null;
    }
}
