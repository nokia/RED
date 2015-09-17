package org.robotframework.ide.core.execution.context;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.importer.ResourceImportReference;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;

import com.google.common.io.Files;


public class RobotDebugExecutionContext {
    
    private RobotFile currentModel;
    private TestCase currentTestCase;
    private List<UserKeyword> userKeywords;
    private List<ResourceImportReference> resourceImportReferences;
    
    private LinkedList<KeywordContext> currentKeywords;
    
    private int testCaseExecutionRowCounter = 0;
    
    public RobotDebugExecutionContext() {
        currentKeywords = new LinkedList<>();
        userKeywords = new ArrayList<>();
        resourceImportReferences = new ArrayList<>();
    }
    
    public void startSuite(final RobotFileOutput robotFileOutput) {
        currentModel = robotFileOutput.getFileModel();
        userKeywords = currentModel.getKeywordTable().getKeywords();
        resourceImportReferences = robotFileOutput.getResourceImportReferences();
    }

    public void startTest(final String testName) {
        TestCaseTable testCaseTable = currentModel.getTestCaseTable();
        List<TestCase> testCases = testCaseTable.getTestCases();
        for (TestCase testCase : testCases) {
            if (testCase.getTestName().getText().toString().equalsIgnoreCase(testName)) {
                currentTestCase = testCase;
                break;
            }
        }
    }

    public void startKeyword(final String name, final String type, final List<String> arguments) {
        currentKeywords.add(new KeywordContext(name, type, arguments));
    }
    
    public void endKeyword() {
        currentKeywords.removeLast();
    }
    
    public void endTest() {
        testCaseExecutionRowCounter = 0;
    }
    
    public KeywordPosition findKeywordPosition() {
        KeywordContext parentKeywordContext = null;
        RobotExecutableRow<?> executionRow = null;
        if (currentKeywords.size() == 1) { // keyword directly from test case
            executionRow = findTestCaseExecutionRow();
        } else { // keyword from Keywords section or resource file
            parentKeywordContext = currentKeywords.get(currentKeywords.size() - 2);
            // search in keywords from Keywords section
            final UserKeyword userKeyword = findUserKeyword(parentKeywordContext);
            if (userKeyword != null) {
                if (parentKeywordContext.getUserKeyword() == null) {
                    parentKeywordContext.setUserKeyword(userKeyword);
                }
                executionRow = findKeywordExecutionRow(userKeyword, parentKeywordContext);
            } else {
                // search in resources from Settings section
                ResourceImportReference resourceImportReference = findResource(parentKeywordContext);
                if (resourceImportReference == null) {
                    // if keyword is in other keyword in resource file
                    resourceImportReference = findLastResourceImportReferenceInCurrentKeywords();
                }
                if (resourceImportReference != null) {
                    if (parentKeywordContext.getResourceImportReference() == null) {
                        parentKeywordContext.setResourceImportReference(resourceImportReference);
                    }
                    final UserKeyword userResourceKeyword = findResourceKeyword(resourceImportReference,
                            parentKeywordContext); // search in keywords from Keywords section in
                                                   // resource file
                    if (userResourceKeyword != null) {
                        if (parentKeywordContext.getUserKeyword() == null) {
                            parentKeywordContext.setUserKeyword(userResourceKeyword);
                        }
                        executionRow = findKeywordExecutionRow(userResourceKeyword, parentKeywordContext);
                    }
                }
            }
        }
        
        KeywordPosition keywordLine = new KeywordPosition();
        if(parentKeywordContext != null && parentKeywordContext.getResourceImportReference() != null) {
            keywordLine.setFilePath(parentKeywordContext.getResourceImportReference().getReference().getProcessedFile().getAbsolutePath());
        }
        keywordLine.setLineNumber(executionRow != null ? executionRow.getAction().getLineNumber() : -1);

        //System.out.println("line:" + executionRow.getAction().getLineNumber());

        return keywordLine;
    }
    
    private UserKeyword findUserKeyword(final KeywordContext keywordContext) {
        if (keywordContext.getUserKeyword() != null) {
            return keywordContext.getUserKeyword();
        }
        for (final UserKeyword userKeyword : userKeywords) {
            if (userKeyword.getKeywordName().getText().toString().equalsIgnoreCase(keywordContext.getName())) {
                return userKeyword;
            }
        }
        return null;
    }
    
    private ResourceImportReference findResource(final KeywordContext keywordContext) {
        if (keywordContext.getResourceImportReference() != null) {
            return keywordContext.getResourceImportReference();
        }
        final String[] nameElements = keywordContext.getName().split("\\.");
        if (nameElements.length > 0) {
            final String name = nameElements[0];
            return findImportReference(name, resourceImportReferences);
        }
        return null;
    }
    
    private ResourceImportReference findImportReference(final String name,
            final List<ResourceImportReference> references) {
        for (final ResourceImportReference resourceImportReference : references) {
            if (name.equalsIgnoreCase(Files.getNameWithoutExtension(resourceImportReference.getReference()
                    .getProcessedFile()
                    .getAbsolutePath()))) {
                return resourceImportReference;
            } else {
                final ResourceImportReference nestedResource = findImportReference(name,
                        resourceImportReference.getReference().getResourceImportReferences());
                if (nestedResource != null) {
                    return nestedResource;
                }
            }
        }
        return null;
    }
    
    private UserKeyword findResourceKeyword(final ResourceImportReference resourceImportReference,
            final KeywordContext keywordContext) {
        if (keywordContext.getUserKeyword() != null) {
            return keywordContext.getUserKeyword();
        }
        String name = keywordContext.getName();
        final String[] nameElements = keywordContext.getName().split("\\.");
        if (nameElements.length > 1) {
            name = nameElements[1];
        }

        final List<UserKeyword> keywords = resourceImportReference.getReference()
                .getFileModel()
                .getKeywordTable()
                .getKeywords();
        for (final UserKeyword userKeyword : keywords) {
            if (userKeyword.getKeywordName().getText().toString().equalsIgnoreCase(name)) {
                return userKeyword;
            }
        }

        return null;
    }
    
    private ResourceImportReference findLastResourceImportReferenceInCurrentKeywords() {
        for (int i = currentKeywords.size() - 1; i >= 0; i--) {
            if (currentKeywords.get(i).getResourceImportReference() != null) {
                return currentKeywords.get(i).getResourceImportReference();
            }
        }

        return null;
    }
    
    private RobotExecutableRow<TestCase> findTestCaseExecutionRow() {
        final List<RobotExecutableRow<TestCase>> executableRows = currentTestCase.getTestExecutionRows();
        if (testCaseExecutionRowCounter < executableRows.size()) {
            final RobotExecutableRow<TestCase> executableRow = executableRows.get(testCaseExecutionRowCounter);
            testCaseExecutionRowCounter++;
            if (executableRow.isExecutable()) {
                return executableRow;
            } else {
                return findTestCaseExecutionRow();
            }
        }
        return null;
    }
    
    private RobotExecutableRow<UserKeyword> findKeywordExecutionRow(final UserKeyword userKeyword, final KeywordContext parentKeywordContext) {
        final List<RobotExecutableRow<UserKeyword>> executableRows = userKeyword.getKeywordExecutionRows();
        if (parentKeywordContext.getKeywordExecutionRowCounter() < executableRows.size()) {
            final RobotExecutableRow<UserKeyword> executableRow = executableRows.get(parentKeywordContext.getKeywordExecutionRowCounter());
            parentKeywordContext.incrementKeywordExecutionRowCounter();
            if (executableRow.isExecutable()) {
                return executableRow;
            } else {
                return findKeywordExecutionRow(userKeyword, parentKeywordContext);
            }
        }
        return null;
    }
    
    private static class KeywordContext {

        private String name;

        private String type;

        private List<String> arguments;
        
        private int keywordExeRowCounter = 0;
        
        private ResourceImportReference resourceImportReference;
        
        private UserKeyword userKeyword;

        public KeywordContext(final String name, final String type, final List<String> arguments) {
            this.name = name;
            this.type = type;
            this.arguments = arguments;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public List<String> getArguments() {
            return arguments;
        }

        public int getKeywordExecutionRowCounter() {
            return keywordExeRowCounter;
        }

        public void incrementKeywordExecutionRowCounter() {
            keywordExeRowCounter++;
        }

        public ResourceImportReference getResourceImportReference() {
            return resourceImportReference;
        }

        public void setResourceImportReference(final ResourceImportReference resourceImportReference) {
            this.resourceImportReference = resourceImportReference;
        }

        public UserKeyword getUserKeyword() {
            return userKeyword;
        }

        public void setUserKeyword(final UserKeyword userKeyword) {
            this.userKeyword = userKeyword;
        }

    }
    
    public class KeywordPosition {
        private int lineNumber;
        private String filePath;
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }
}
