package org.robotframework.ide.core.executor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
    
    private TestRunnerAgentMessageLogParser testRunnerAgentMessageLogParser;
    
    private String executionArguments = "";
    
    public RobotExecutor() {
        logOutputStream = new RobotLogOutputStream();
        testRunnerAgentMessageLogParser = new TestRunnerAgentMessageLogParser();
    }

    public int execute(File projectLocation, String executorName, String userArguments) {
        String robotExecutorName = executorName;
        if (OS.isFamilyWindows()) {
            robotExecutorName += ".bat";
        } else {
            robotExecutorName += ".sh";
        }
        
        TestRunnerAgentHandler testRunnerAgentHandler = new TestRunnerAgentHandler();
        testRunnerAgentHandler.addListener(testRunnerAgentMessageLogParser);
        Thread handler = new Thread(testRunnerAgentHandler);
        handler.start();
        
        Path testRunnerAgentFilePath = createTestRunnerAgentFile();
        
        if (userArguments != null && !userArguments.equals("")) {
            executionArguments += " " + userArguments;
        }
        if(!executionArguments.equals("")) {
            executionArguments += " ";
        }
        
        String line = robotExecutorName + " --listener " + testRunnerAgentFilePath.toString() + ":54470:False" + " "
                + executionArguments + projectLocation.getAbsolutePath();

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
            executionArguments = "";
        }
        
        removeTempDir(testRunnerAgentFilePath.getParent());
        
        return exitValue;
    }
    
    public void addOutputStreamListener(IRobotOutputListener listener) {
        logOutputStream.addListener(listener);
    }
    
    public void removeOutputStreamListener(IRobotOutputListener listener) {
        logOutputStream.removeListener(listener);
    }
    
    public void setMessageLogListener(IRobotOutputListener listener) {
        testRunnerAgentMessageLogParser.setMessageLogListener(listener);
    }
    
    private String computeTestDuration(long startTime, long endTime) {

        long diff = endTime - startTime;
        
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;

        DecimalFormat df = new DecimalFormat("00");
        return df.format(diffHours) +":"+ df.format(diffMinutes) +":"+ df.format(diffSeconds);
    }
    
    public Path createTestRunnerAgentFile() {
        Path tempDir = null;
        File agentFile = new File("");
        try {
            tempDir = Files.createTempDirectory("RobotTempDir");
            agentFile = new File(tempDir.toString() + "\\TestRunnerAgent.py");
            Files.copy(RobotExecutor.class.getResourceAsStream("TestRunnerAgent.py"), agentFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return agentFile.toPath();
    }
    
    public void removeTempDir(Path dir) {
        File tempDir = new File(dir.toString());
        File[] files = tempDir.listFiles();
        try {
            for (int i = 0; i < files.length; i++) {
                Files.delete(files[i].toPath());
            }
            Files.delete(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void addTest(String suiteName, String testName) {
        String testOption = "--test";
        if (testName != null && !testName.equals("")) {
            if (!executionArguments.equals("")) {
                executionArguments += " ";
            }
            if (suiteName != null && !suiteName.equals("")) {
                executionArguments += testOption + " " + suiteName + "." + testName;
            } else {
                executionArguments += testOption + " " + testName;
            }
        }
    }

    public void addSuite(String parent, String suiteName) {
        String suiteOption = "--suite";
        if (suiteName != null && !suiteName.equals("")) {
            if (!executionArguments.equals("")) {
                executionArguments += " ";
            }
            if (parent != null && !parent.equals("")) {
                executionArguments += suiteOption + " " + parent + "." + suiteName;
            } else {
                executionArguments += suiteOption + " " + suiteName;
            }
        }
    }
}
