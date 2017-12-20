package org.rf.ide.core.dryrun;

import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.function.Consumer;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.event.MessageEvent;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class RobotDryRunSuiteCountEventListenerTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Mock
    private Consumer<Integer> suiteCountHandler;

    @Test
    public void suiteCountEventMappingExceptionIsHandled() throws Exception {
        thrown.expect(JsonMessageMapper.JsonMessageMapperException.class);
        thrown.expectMessage("Problem with mapping suite count message");
        thrown.expectCause(isA(JsonMappingException.class));

        final RobotDryRunSuiteCountEventListener listener = new RobotDryRunSuiteCountEventListener(suiteCountHandler);
        listener.handleMessage(new MessageEvent("{\"suite_count\":\"incorrect\"}", LogLevel.NONE, null));
    }

    @Test
    public void suiteCountIsHandled() throws Exception {
        final RobotDryRunSuiteCountEventListener listener = new RobotDryRunSuiteCountEventListener(suiteCountHandler);
        listener.handleMessage(createSuiteCountMessageEvent(10));

        verify(suiteCountHandler).accept(10);
        verifyNoMoreInteractions(suiteCountHandler);
    }

    @Test
    public void unsupportedLevelMessageEventsAreIgnored() throws Exception {
        final RobotDryRunSuiteCountEventListener listener = new RobotDryRunSuiteCountEventListener(suiteCountHandler);

        listener.handleMessage(new MessageEvent("msg", LogLevel.TRACE, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.DEBUG, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.INFO, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.WARN, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.ERROR, null));
        listener.handleMessage(new MessageEvent("msg", LogLevel.FAIL, null));

        verifyZeroInteractions(suiteCountHandler);
    }

    private static MessageEvent createSuiteCountMessageEvent(final int count) throws Exception {
        final String message = new ObjectMapper().writeValueAsString((ImmutableMap.of("suite_count", count)));
        return new MessageEvent(message, LogLevel.NONE, null);
    }
}
