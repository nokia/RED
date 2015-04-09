package org.robotframework.ide.core.executor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author mmarzec
 *
 */
public class TestRunnerAgentMessageLogParser implements IRobotOutputListener {

    private IRobotOutputListener messageLogListener;

    private ObjectMapper mapper = new ObjectMapper();

    public TestRunnerAgentMessageLogParser() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleLine(String line) {

        Map<String, Object> map = null;
        try {
            map = mapper.readValue(line, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (map.containsKey("log_message")) {
            ArrayList<Object> list = (ArrayList<Object>) map.get("log_message");
            HashMap<String, String> elements = (HashMap<String, String>) list.get(0);
            messageLogListener.handleLine(elements.get("timestamp") + " : " + elements.get("level") + " : "
                    + elements.get("message") + '\n');
        }
        if (map.containsKey("start_test")) {
            ArrayList<Object> list = (ArrayList<Object>) map.get("start_test");
            HashMap<String, String> elements = (HashMap<String, String>) list.get(1);
            messageLogListener.handleLine("Starting test: " + elements.get("longname") + '\n');
        }
        if (map.containsKey("end_test")) {
            ArrayList<Object> list = (ArrayList<Object>) map.get("end_test");
            HashMap<String, String> elements = (HashMap<String, String>) list.get(1);
            messageLogListener.handleLine("Ending test: " + elements.get("longname") + '\n');
        }
    }

    public void setMessageLogListener(IRobotOutputListener messageLogListener) {
        this.messageLogListener = messageLogListener;
    }
}
