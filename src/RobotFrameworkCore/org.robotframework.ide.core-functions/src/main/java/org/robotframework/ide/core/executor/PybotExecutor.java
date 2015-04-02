package org.robotframework.ide.core.executor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * @author mmarzec
 *
 */
public class PybotExecutor implements IExecutor {
    
    private RobotLogOutputStream logOutputStream;
    
    public PybotExecutor() {
        logOutputStream = new RobotLogOutputStream();
    }
    
    @Override
    public int execute(String testPath, File projectLocation) {
        
        String robotExecutorName = "pybot";
        if (OS.isFamilyWindows()) {
            robotExecutorName += ".bat";
        } else {
            robotExecutorName += ".sh";
        }
        
        String line = robotExecutorName + " " + testPath;
        CommandLine cmd = CommandLine.parse(line);
        
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(projectLocation);
        PumpStreamHandler streamHandler = new PumpStreamHandler(logOutputStream);
        executor.setStreamHandler(streamHandler);
        try {
            return executor.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return 1;
    }

    @Override
    public void addOutputStreamListener(IRobotLogOutputStreamListener listener) {
        logOutputStream.addListener(listener);
    }
    
}
