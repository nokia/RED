package org.robotframework.ide.core.executor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.OS;

public class RobotExecutor {

    private Path testRunnerAgentFilePath;

    public String[] createCommand(File projectLocation, String executorName, List<String> testArguments,
            String userArguments, boolean isDebugging) {

        List<String> cmdElements = new ArrayList<String>();

        String robotExecutorName = executorName;
        if (OS.isFamilyWindows()) {
            robotExecutorName += ".bat";
        } else {
            robotExecutorName += "";
        }
        cmdElements.add(robotExecutorName);

        String debugEnabled = "False";
        if (isDebugging) {
            debugEnabled = "True";
        }
        cmdElements.add("--listener");
        cmdElements.add(testRunnerAgentFilePath.toString() + ":54470:" + debugEnabled);

        for (String suite : testArguments) {
            cmdElements.add("--suite");
            cmdElements.add(suite);
        }

        if (!userArguments.equals("") && (userArguments.contains("--") || userArguments.contains("-"))) {
            List<String> userArgsList = new ArrayList<String>();
            this.extractArguments(userArgsList, userArguments);
            for (String arg : userArgsList) {
                if (!arg.equals(" ")) {
                    cmdElements.add(arg);
                }
            }
        }

        cmdElements.add(projectLocation.getAbsolutePath());

        return cmdElements.toArray(new String[cmdElements.size()]);
    }

    public void createTestRunnerAgentFile() {
        Path tempDir = null;
        File agentFile = new File("");
        try {
            tempDir = Files.createTempDirectory("RobotTempDir");
            agentFile = new File(tempDir.toString() + File.separator + "TestRunnerAgent.py");
            Files.copy(RobotExecutor.class.getResourceAsStream("TestRunnerAgent.py"), agentFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        testRunnerAgentFilePath = agentFile.toPath();
    }

    public void removeTestRunnerAgentFile() {
        Path dir = testRunnerAgentFilePath.getParent();
        File[] files = new File(dir.toString()).listFiles();
        try {
            for (int i = 0; i < files.length; i++) {
                Files.delete(files[i].toPath());
            }
            Files.delete(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void startTestRunnerAgentHandler(IRobotOutputListener messageLogListener) {
        MessageLogParser messageLogParser = new MessageLogParser();
        messageLogParser.setMessageLogListener(messageLogListener);
        TestRunnerAgentHandler testRunnerAgentHandler = new TestRunnerAgentHandler();
        testRunnerAgentHandler.addListener(messageLogParser);
        Thread handler = new Thread(testRunnerAgentHandler);
        handler.start();
    }

    private void extractArguments(List<String> arguments, String str) {

        String argName = "";
        String argValue = "";
        int end = 0;
        if (str.substring(0, 2).equals("--")) {
            int argNameEnd = str.indexOf(' ');
            if (argNameEnd < 0) {
                argName = str.substring(2, str.length());
                arguments.add("--" + argName);
                return;
            }
            argName = str.substring(2, argNameEnd);
            int argValueEnd1 = str.indexOf(" -", argNameEnd);
            int argValueEnd2 = str.indexOf(" --", argNameEnd);
            if (argValueEnd2 > 0 || argValueEnd1 > 0) {
                if (argValueEnd2 < argValueEnd1 && argValueEnd2 > 0) {
                    if (argNameEnd < argValueEnd2)
                        argValue = str.substring(argNameEnd + 1, argValueEnd2);
                    end = argValueEnd2 + 1;
                } else {
                    if (argNameEnd < argValueEnd1)
                        argValue = str.substring(argNameEnd + 1, argValueEnd1);
                    end = argValueEnd1 + 1;
                }
            } else {
                argValue = str.substring(argNameEnd + 1);
                end = str.length();
            }
            arguments.add("--" + argName);
        } else if (str.substring(0, 1).equals("-")) {
            int argNameEnd = str.indexOf(' ');
            if (argNameEnd < 0) {
                argName = str.substring(1, str.length());
                arguments.add("-" + argName);
                return;
            }
            argName = str.substring(1, argNameEnd);
            int argValueEnd1 = str.indexOf(" -", argNameEnd);
            int argValueEnd2 = str.indexOf(" --", argNameEnd);
            if (argValueEnd2 > 0 || argValueEnd1 > 0) {
                if (argValueEnd2 < argValueEnd1 && argValueEnd2 > 0) {
                    if (argNameEnd < argValueEnd2)
                        argValue = str.substring(argNameEnd + 1, argValueEnd2);
                    end = argValueEnd2 + 1;
                } else {
                    if (argNameEnd < argValueEnd1)
                        argValue = str.substring(argNameEnd + 1, argValueEnd1);
                    end = argValueEnd1 + 1;
                }
            } else {
                argValue = str.substring(argNameEnd + 1);
                end = str.length();
            }
            arguments.add("-" + argName);
        } else {
            return;
        }

        if (!argValue.equals(""))
            arguments.add(argValue);
        if (end != str.length())
            extractArguments(arguments, str.substring(end));
    }
}
