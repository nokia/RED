package org.robotframework.ide.core.executor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author mmarzec
 */
public class MessageLogParser implements IRobotOutputListener {

    private static final String LOG_MESSAGE_NAME = "log_message";

    private static final String START_TEST_NAME = "start_test";

    private static final String END_TEST_NAME = "end_test";

    private IRobotOutputListener messageLogListener;

    private ObjectMapper mapper;

    private Map<String, Object> parsedLine;

    public MessageLogParser() {
        mapper = new ObjectMapper();
        parsedLine = new HashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleLine(String line) {

        try {
            parsedLine = mapper.readValue(line, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (parsedLine.containsKey(LOG_MESSAGE_NAME)) {
            Map<String, String> elements = extractElementsFromLine(parsedLine, LOG_MESSAGE_NAME, 0);
            messageLogListener.handleLine(elements.get("timestamp") + " : " + elements.get("level") + " : "
                    + elements.get("message") + '\n');
        }
        if (parsedLine.containsKey(START_TEST_NAME)) {
            Map<String, String> elements = extractElementsFromLine(parsedLine, START_TEST_NAME, 1);
            messageLogListener.handleLine("Starting test: " + elements.get("longname") + '\n');
        }
        if (parsedLine.containsKey(END_TEST_NAME)) {
            Map<String, String> elements = extractElementsFromLine(parsedLine, END_TEST_NAME, 1);
            messageLogListener.handleLine("Ending test: " + elements.get("longname") + '\n');
            messageLogListener.handleLine("\n");
        }
    }

    public void setMessageLogListener(IRobotOutputListener messageLogListener) {
        this.messageLogListener = messageLogListener;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractElementsFromLine(Map<String, Object> map, String lineName, int elementsPosition) {
        List<Object> list = (List<Object>) map.get(lineName);
        return (Map<String, String>) list.get(elementsPosition);
    }
}
