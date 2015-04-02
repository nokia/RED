package org.robotframework.ide.core.executor;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * @author mmarzec
 *
 */
public class RobotExecutor {
    
    private RobotLogOutputStream logOutputStream;
    
    public RobotExecutor() {
        logOutputStream = new RobotLogOutputStream();
    }

    public int execute(String testPath, File projectLocation, String executorName) {
        String robotExecutorName = executorName;
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
        
        logOutputStream.processLine("Command: " + line, 1);
        int exitValue = 1;
        long startTime = 0;
        try {
            startTime = System.currentTimeMillis();
            exitValue = executor.execute(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            long endTime = System.currentTimeMillis();
            logOutputStream.processLine("Elapsed time: " + this.computeTestDuration(startTime, endTime) + "\n", 1);
        }
        
        return exitValue;
    }
    
    public void addOutputStreamListener(IRobotLogOutputStreamListener listener) {
        logOutputStream.addListener(listener);
    }
    
    public void removeOutputStreamListener(IRobotLogOutputStreamListener listener) {
        logOutputStream.removeListener(listener);
    }
    
    private String computeTestDuration(long startTime, long endTime) {

        long diff = endTime - startTime;
        
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;

        DecimalFormat df = new DecimalFormat("00");
        return df.format(diffHours) +":"+ df.format(diffMinutes) +":"+ df.format(diffSeconds);
    }
}
