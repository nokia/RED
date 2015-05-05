package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.services.events.IEventBroker;

public class RobotEventBroker {

    IEventBroker broker;

    public RobotEventBroker(IEventBroker broker) {
        this.broker = broker;
    }

    public void sendHighlightLineEventToTextEditor(String file, int line) {

        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("file", file);
        eventMap.put("line", String.valueOf(line));
        broker.send("TextEditor/HighlightLine", eventMap);
    }

    public void sendClearEventToTextEditor(String file) {

        broker.send("TextEditor/ClearHighlightedLine", file);
    }

    public void sendClearAllEventToTextEditor() {

        broker.send("TextEditor/ClearHighlightedLine", "");
    }

    public void sendAppendLineEventToMessageLogView(String line) {

        broker.send("MessageLogView/AppendLine", line);
    }

    public void sendClearEventToMessageLogView() {

        broker.send("MessageLogView/Clear", "");
    }
}
