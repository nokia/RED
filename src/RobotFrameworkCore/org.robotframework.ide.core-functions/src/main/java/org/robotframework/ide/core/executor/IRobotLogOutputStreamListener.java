package org.robotframework.ide.core.executor;

/**
 * @author mmarzec
 *
 */
public interface IRobotLogOutputStreamListener {

    void handleLine(String line);
}
