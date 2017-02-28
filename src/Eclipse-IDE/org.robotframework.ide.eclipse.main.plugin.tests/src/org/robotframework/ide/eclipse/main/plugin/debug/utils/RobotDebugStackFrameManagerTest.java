package org.robotframework.ide.eclipse.main.plugin.debug.utils;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotStackFrame;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotThread;

public class RobotDebugStackFrameManagerTest {

    private RobotDebugStackFrameManager stackFrameManager;

    @Before
    public void setUp() {
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.getRobotVariablesManager()).thenReturn(new RobotDebugVariablesManager(target));
        final RobotThread thread = new RobotThread(target);

        stackFrameManager = new RobotDebugStackFrameManager(thread);
    }

    @Test
    public void testGetStackFrames() throws DebugException {
        final Map<String, KeywordContext> currentKeywordsContextMap = new LinkedHashMap<>();

        final KeywordContext keywordContext1 = new KeywordContext("file1", 19, new HashMap<String, Object>());
        currentKeywordsContextMap.put("Main keyword", keywordContext1);
        final KeywordContext keywordContext2 = new KeywordContext("file1", 24, new HashMap<String, Object>());
        currentKeywordsContextMap.put("Nested keyword", keywordContext2);
        final KeywordContext keywordContext3 = new KeywordContext("file1", 27, new HashMap<String, Object>());
        currentKeywordsContextMap.put("Another nested keyword", keywordContext3);
        String[] keywordNames = currentKeywordsContextMap.keySet().toArray(new String[0]);

        final IStackFrame[] stackFrames1 = stackFrameManager.getStackFrames(currentKeywordsContextMap);

        verifyStackFramesIdsAndKeywordNames(currentKeywordsContextMap, stackFrames1, keywordNames);

        final KeywordContext keywordContext4 = new KeywordContext("file2", 3, new HashMap<String, Object>());
        currentKeywordsContextMap.put("Keyword from some library", keywordContext4);
        final KeywordContext keywordContext5 = new KeywordContext("file2", 7, new HashMap<String, Object>());
        currentKeywordsContextMap.put("Nested keyword from library", keywordContext5);
        final KeywordContext keywordContext6 = new KeywordContext("file2", 15, new HashMap<String, Object>());
        currentKeywordsContextMap.put("Another nested keyword from library", keywordContext6);
        keywordNames = currentKeywordsContextMap.keySet().toArray(new String[0]);

        stackFrameManager.setHasStackFramesCreated(false);
        final IStackFrame[] stackFrames2 = stackFrameManager.getStackFrames(currentKeywordsContextMap); // first three stack frames will be copied, new three stack frames will be added at the top of the stack

        verifyStackFramesIdsAndKeywordNames(currentKeywordsContextMap, stackFrames2, keywordNames);
        verifyNewStackFramesHaveCopiedInstances(stackFrames1, stackFrames2);

        currentKeywordsContextMap.remove(keywordNames[keywordNames.length-1]); // remove last keyword from context
        final KeywordContext keywordContext7 = new KeywordContext("file2", 25, new HashMap<String, Object>());
        currentKeywordsContextMap.put("Some other nested keyword from library", keywordContext7);
        keywordNames = currentKeywordsContextMap.keySet().toArray(new String[0]);
        
        stackFrameManager.setHasStackFramesCreated(false);
        final IStackFrame[] stackFrames3 = stackFrameManager.getStackFrames(currentKeywordsContextMap); // all stack frames will be copied, stack frame at the top will be updated
        
        verifyStackFramesIdsAndKeywordNames(currentKeywordsContextMap, stackFrames3, keywordNames);
        verifyNewStackFramesHaveCopiedInstances(stackFrames2, stackFrames3);
        
        for (int i = keywordNames.length - 1; i > 1; i--) { // remove last four keywords from context
            currentKeywordsContextMap.remove(keywordNames[i]);
        }
        keywordNames = currentKeywordsContextMap.keySet().toArray(new String[0]);
        
        stackFrameManager.setHasStackFramesCreated(false);
        final IStackFrame[] stackFrames4 = stackFrameManager.getStackFrames(currentKeywordsContextMap); // lowest stack frame will be copied, stack frame at the top will be replaced
        
        verifyStackFramesIdsAndKeywordNames(currentKeywordsContextMap, stackFrames4, keywordNames);
        assertTrue(stackFrames3[5] == stackFrames4[1]);
    }

    public void verifyNewStackFramesHaveCopiedInstances(final IStackFrame[] oldStackFrames, final IStackFrame[] newStackFrames) {
        final int sizeDiff = newStackFrames.length - oldStackFrames.length;
        for (int i = 0; i < oldStackFrames.length; i++) {
            assertTrue(oldStackFrames[i] == newStackFrames[i + sizeDiff]);
        }
    }

    public void verifyStackFramesIdsAndKeywordNames(final Map<String, KeywordContext> currentKeywordsContextMap,
            final IStackFrame[] currentStackFrames, final String[] currentKeywordNames) throws DebugException {
        assertTrue(stackFrameManager.hasStackFramesCreated());
        assertTrue(currentKeywordsContextMap.size() == currentStackFrames.length);
        for (int i = 0; i < currentStackFrames.length; i++) {
            assertTrue(((RobotStackFrame) currentStackFrames[i]).getStackLevel() == (currentStackFrames.length - i));
            assertTrue(((RobotStackFrame) currentStackFrames[i]).getName()
                    .contains(currentKeywordNames[currentStackFrames.length - i - 1]));
        }
    }

}
