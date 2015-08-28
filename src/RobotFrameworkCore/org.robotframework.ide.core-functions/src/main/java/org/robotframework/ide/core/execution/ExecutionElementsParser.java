package org.robotframework.ide.core.execution;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.robotframework.ide.core.execution.ExecutionElement.ExecutionElementType;
import org.robotframework.ide.core.executor.ILineHandler;

/**
 * @author mmarzec
 */
public class ExecutionElementsParser implements ILineHandler {

    public static final String ROBOT_EXECUTION_PASS_STATUS = "PASS";
    
    
    private static final String START_SUITE_EVENT = "start_suite";

    private static final String END_SUITE_EVENT = "end_suite";

    private static final String START_TEST_EVENT = "start_test";

    private static final String END_TEST_EVENT = "end_test";
    
    private static final String OUTPUT_FILE_EVENT = "output_file";

    private final ObjectMapper mapper;

    private Map<String, Object> eventMap;

    private final IExecutionHandler executionHandler;

    public ExecutionElementsParser(final IExecutionHandler executionHandler) {
        this.mapper = new ObjectMapper();
        this.eventMap = new HashMap<String, Object>();
        this.executionHandler = executionHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processLine(final String line) {
        try {
            eventMap = mapper.readValue(line, Map.class);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        
        final String eventType = getEventType(eventMap);
        if (eventType == null) {
            return;
        }

        switch (eventType) {
            case START_SUITE_EVENT:
                final List<Object> startSuiteList = (List<Object>) eventMap.get(START_SUITE_EVENT);
                ExecutionElement startSuiteElement = new ExecutionElement((String) startSuiteList.get(0), ExecutionElementType.SUITE);
                final Map<String, String> startSuiteDetails = (Map<String, String>) startSuiteList.get(1);
                startSuiteElement.setSource(startSuiteDetails.get("source"));
                executionHandler.processExecutionElement(startSuiteElement);
                break;
            case END_SUITE_EVENT:
                final List<Object> endSuiteList = (List<Object>) eventMap.get(END_SUITE_EVENT);
                ExecutionElement endSuiteElement = new ExecutionElement((String) endSuiteList.get(0), ExecutionElementType.SUITE);
                final Map<String, Object> endSuiteDetails = (Map<String, Object>) endSuiteList.get(1);
                endSuiteElement.setElapsedTime((Integer)endSuiteDetails.get("elapsedtime"));
                endSuiteElement.setMessage((String)endSuiteDetails.get("message"));
                endSuiteElement.setStatus((String)endSuiteDetails.get("status"));
                executionHandler.processExecutionElement(endSuiteElement);
                break;
            case START_TEST_EVENT:
                final List<Object> testList = (List<Object>) eventMap.get(START_TEST_EVENT);
                ExecutionElement startTestElement = new ExecutionElement((String)testList.get(0), ExecutionElementType.TEST);
                executionHandler.processExecutionElement(startTestElement);
                break;
            case END_TEST_EVENT:
                final List<Object> endTestList = (List<Object>) eventMap.get(END_TEST_EVENT);
                ExecutionElement endTestElement = new ExecutionElement((String) endTestList.get(0), ExecutionElementType.TEST);
                final Map<String, Object> endTestDetails = (Map<String, Object>) endTestList.get(1);
                endTestElement.setElapsedTime((Integer)endTestDetails.get("elapsedtime"));
                endTestElement.setMessage((String)endTestDetails.get("message"));
                endTestElement.setStatus((String)endTestDetails.get("status"));
                executionHandler.processExecutionElement(endTestElement);
                break;
            case OUTPUT_FILE_EVENT:
                final List<Object> outputFileList = (List<Object>) eventMap.get(OUTPUT_FILE_EVENT);
                ExecutionElement outputFilePathElement = new ExecutionElement((String) outputFileList.get(0), ExecutionElementType.OUTPUT_FILE);
                executionHandler.processExecutionElement(outputFilePathElement);
                break;
            default:
                break;
        }
    }

    private String getEventType(final Map<?, ?> eventMap) {
        if (eventMap == null) {
            return null;
        }
        final Set<?> keySet = eventMap.keySet();
        if (!keySet.isEmpty()) {
            return (String) keySet.iterator().next();
        }
        return null;
    }
    
}
