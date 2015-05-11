package org.robotframework.ide.core.executor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class RobotExecutor {

    private Path testRunnerAgentFilePath;


    public String[] createCommand(File projectLocation, String executorName,
            List<String> testArguments, String userArguments,
            boolean isDebugging) {

        List<String> cmdElements = new ArrayList<String>();

        String robotExecutorName = executorName;
        if (isWindows()) {
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
        cmdElements.add(testRunnerAgentFilePath.toString() + ":54470:"
                + debugEnabled);

        for (String suite : testArguments) {
            cmdElements.add("--suite");
            cmdElements.add(suite);
        }

        if (!userArguments.equals("")) {
            cmdElements
                    .addAll(fromJavaArgsToPythonLike(convertToJavaMainLikeArgs(userArguments)));
        }

        cmdElements.add(projectLocation.getAbsolutePath());

        return cmdElements.toArray(new String[cmdElements.size()]);
    }


    public void createTestRunnerAgentFile() {
        Path tempDir = null;
        File agentFile = new File("");
        try {
            tempDir = Files.createTempDirectory("RobotTempDir");
            agentFile = new File(tempDir.toString() + File.separator
                    + "TestRunnerAgent.py");
            Files.copy(RobotExecutor.class
                    .getResourceAsStream("TestRunnerAgent.py"), agentFile
                    .toPath(), StandardCopyOption.REPLACE_EXISTING);
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


    public void startTestRunnerAgentHandler(
            IRobotOutputListener messageLogListener) {
        MessageLogParser messageLogParser = new MessageLogParser();
        messageLogParser.setMessageLogListener(messageLogListener);
        TestRunnerAgentHandler testRunnerAgentHandler = new TestRunnerAgentHandler();
        testRunnerAgentHandler.addListener(messageLogParser);
        Thread handler = new Thread(testRunnerAgentHandler);
        handler.start();
    }


    private List<String> fromJavaArgsToPythonLike(List<String> javaLikeArgs) {
        List<String> args = new LinkedList<String>();

        StringBuilder current = new StringBuilder();
        for (String arg : javaLikeArgs) {
            if (arg.startsWith("-")) {
                if (current.length() > 0) {
                    args.add(current.toString());
                    current = new StringBuilder();
                }

                args.add(arg);
            } else {
                if (current.length() > 0) {
                    current.append(' ');
                }
                current.append(arg);
            }
        }

        if (current.length() > 0) {
            args.add(current.toString());
        }

        return args;
    }


    private List<String> convertToJavaMainLikeArgs(String text) {
        List<String> args = new LinkedList<String>();
        char chars[] = text.toCharArray();
        char previousToken = ' ';
        boolean wasQuotationMark = false;
        StringBuilder currentToken = new StringBuilder();
        for (char c : chars) {
            if (c == ' ') {
                if (wasQuotationMark) {
                    currentToken.append(c);
                } else {
                    if (currentToken.length() > 0) {
                        args.add(currentToken.toString());
                        currentToken = new StringBuilder();
                    }
                }
            } else if (c == '\"') {
                if (previousToken == '\\') {
                    currentToken.append(c);
                } else {
                    if (wasQuotationMark) {
                        currentToken.append(c);
                        args.add(currentToken.toString());
                        currentToken = new StringBuilder();

                        wasQuotationMark = false;
                    } else {
                        currentToken.append(c);
                        wasQuotationMark = true;
                    }
                }
            } else {
                currentToken.append(c);
            }

            previousToken = c;
        }

        if (currentToken.length() > 0) {
            args.add(currentToken.toString());
        }

        return args;
    }


    public boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }
}
