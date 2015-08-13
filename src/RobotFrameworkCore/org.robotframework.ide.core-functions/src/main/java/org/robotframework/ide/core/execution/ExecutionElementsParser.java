package org.robotframework.ide.core.execution;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.robotframework.ide.core.execution.ExecutionElement.ExecutionElementType;
import org.robotframework.ide.core.executor.ILineHandler;

/**
 * @author mmarzec
 */
public class ExecutionElementsParser implements ILineHandler {

    private static final String START_SUITE = "start_suite";

    private static final String END_SUITE = "end_suite";

    private static final String START_TEST = "start_test";

    private static final String END_TEST = "end_test";

    private final ObjectMapper mapper;

    private Map<String, Object> parsedLine;

    private final IExecutionHandler executionHandler;

    public ExecutionElementsParser(final IExecutionHandler executionHandler) {
        this.mapper = new ObjectMapper();
        this.parsedLine = new HashMap<String, Object>();
        this.executionHandler = executionHandler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processLine(final String line) {
        try {
            parsedLine = mapper.readValue(line, Map.class);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (parsedLine.containsKey(START_SUITE)) {
            final List<Object> list = (List<Object>) parsedLine.get(START_SUITE);
            String name = (String) list.get(0);
            ExecutionElement executionElement = new ExecutionElement(name, ExecutionElementType.SUITE);
            final Map<String, String> elements = (Map<String, String>) list.get(1);
            executionElement.setSource(elements.get("source"));
            executionHandler.processExecutionElement(executionElement);
        }
        if (parsedLine.containsKey(END_SUITE)) {
            final List<Object> list = (List<Object>) parsedLine.get(END_SUITE);
            String name = (String) list.get(0);
            ExecutionElement executionElement = new ExecutionElement(name, ExecutionElementType.SUITE);
            final Map<String, Object> elements = (Map<String, Object>) list.get(1);
            executionElement.setElapsedTime((Integer)elements.get("elapsedtime"));
            executionElement.setMessage((String)elements.get("message"));
            executionElement.setStatus((String)elements.get("status"));
            executionHandler.processExecutionElement(executionElement);
        }
        
        if (parsedLine.containsKey(START_TEST)) {
            final List<Object> list = (List<Object>) parsedLine.get(START_TEST);
            String name = (String) list.get(0);
            ExecutionElement executionElement = new ExecutionElement(name, ExecutionElementType.TEST);
            executionHandler.processExecutionElement(executionElement);
        }
        if (parsedLine.containsKey(END_TEST)) {
            final List<Object> list = (List<Object>) parsedLine.get(END_TEST);
            String name = (String) list.get(0);
            ExecutionElement executionElement = new ExecutionElement(name, ExecutionElementType.TEST);
            final Map<String, Object> elements = (Map<String, Object>) list.get(1);
            executionElement.setElapsedTime((Integer)elements.get("elapsedtime"));
            executionElement.setMessage((String)elements.get("message"));
            executionElement.setStatus((String)elements.get("status"));
            executionHandler.processExecutionElement(executionElement);
        }
    }

}
