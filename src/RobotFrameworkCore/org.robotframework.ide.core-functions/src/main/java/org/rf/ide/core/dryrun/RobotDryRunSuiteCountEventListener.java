package org.rf.ide.core.dryrun;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.codehaus.jackson.type.TypeReference;
import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.MessageEvent;

/**
 * @author bembenek
 */
public class RobotDryRunSuiteCountEventListener extends RobotDefaultAgentEventListener {

    private final Consumer<Integer> suiteCountHandler;

    public RobotDryRunSuiteCountEventListener(final Consumer<Integer> suiteCountHandler) {
        this.suiteCountHandler = suiteCountHandler;
    }

    @Override
    public void handleMessage(final MessageEvent event) {
        if (event.getLevel() == LogLevel.NONE) {
            try {
                getSuiteCount(event).ifPresent(suiteCountHandler::accept);
            } catch (final IOException e) {
                throw new JsonMessageMapper.JsonMessageMapperException("Problem with mapping suite count message", e);
            }
        }
    }

    private Optional<Integer> getSuiteCount(final MessageEvent event) throws IOException {
        return JsonMessageMapper.readValue(event, "suite_count", new TypeReference<Map<String, Integer>>() {
        });
    }
}
