package org.robotframework.ide.eclipse.main.plugin.debug.utils;

import java.util.Map;

import org.eclipse.debug.core.model.IStackFrame;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotStackFrame;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotThread;


public class RobotDebugStackFrameManager {

    private boolean hasStackFramesCreated;
    private IStackFrame[] stackFrames;
    private RobotThread thread;
    
    public RobotDebugStackFrameManager(final RobotThread thread) {
        this.thread = thread;
    }

    public IStackFrame[] getStackFrames(final Map<String, KeywordContext> currentKeywordsContextMap) {

        if (!hasStackFramesCreated) {
            
            if (!hasVariablesInitialized(currentKeywordsContextMap)) {
                if (stackFrames != null) {
                    return stackFrames;
                }
                return new IStackFrame[0];
            }

            int numberOfStackFramesToCopy = countNumberOfStackFramesToCopy(currentKeywordsContextMap);
            initStackFrameArray(currentKeywordsContextMap);
            
            final IStackFrame[] newStackFrames = new IStackFrame[currentKeywordsContextMap.size()];
            int currentKeywordsCounter = 1;
            for (final String keywordName : currentKeywordsContextMap.keySet()) {
                final KeywordContext keywordContext = currentKeywordsContextMap.get(keywordName);
                // only the highest level of StackFrames is created, lower levels are copied from
                // previous StackFrames
                final int lowestStackFrameLevel = currentKeywordsContextMap.size() - currentKeywordsCounter;
                if (currentKeywordsCounter > numberOfStackFramesToCopy) {
                    if (shouldSetupExistingStackFrame(currentKeywordsContextMap, lowestStackFrameLevel)) {
                        ((RobotStackFrame) stackFrames[lowestStackFrameLevel]).setStackFrameData(
                                currentKeywordsCounter, keywordName, keywordContext);
                        newStackFrames[lowestStackFrameLevel] = stackFrames[lowestStackFrameLevel];
                    } else {
                        newStackFrames[lowestStackFrameLevel] = new RobotStackFrame(thread, currentKeywordsCounter,
                                keywordName, keywordContext);
                    }
                } else {
                    IStackFrame previousStackFrame = extractPreviousStackFrame(currentKeywordsContextMap,
                            numberOfStackFramesToCopy, currentKeywordsCounter);
                    ((RobotStackFrame) previousStackFrame).setStackFrameData(currentKeywordsCounter, keywordName,
                            keywordContext);
                    newStackFrames[lowestStackFrameLevel] = previousStackFrame;
                }
                currentKeywordsCounter++;
            }
            stackFrames = newStackFrames;
            hasStackFramesCreated = true;
        }

        return stackFrames;
    }

    private boolean hasVariablesInitialized(final Map<String, KeywordContext> currentKeywordsContextMap) {
        if (currentKeywordsContextMap != null && !currentKeywordsContextMap.isEmpty()) {
            final KeywordContext lastKeywordContext = (KeywordContext) 
                    currentKeywordsContextMap.values().toArray()[currentKeywordsContextMap.size() - 1];
            return lastKeywordContext.getVariables() != null;
        }
        return false;
    }
    
    private int countNumberOfStackFramesToCopy(final Map<String, KeywordContext> currentKeywordsContextMap) {
        int numberOfStackTracesToCopy = 0;
        if (stackFrames != null) {
            if (stackFrames.length < currentKeywordsContextMap.size()) {
                numberOfStackTracesToCopy = stackFrames.length;
            } else {
                numberOfStackTracesToCopy = currentKeywordsContextMap.size() - 1;
            }
        }
        return numberOfStackTracesToCopy;
    }
    
    private void initStackFrameArray(final Map<String, KeywordContext> currentKeywordsContextMap) {
        if (stackFrames == null || stackFrames.length == 0) {
            stackFrames = new IStackFrame[currentKeywordsContextMap.size()];
        }
    }
    
    private boolean shouldSetupExistingStackFrame(final Map<String, KeywordContext> currentKeywordsContextMap,
            final int lowestStackFrameLevel) {
        return stackFrames.length == currentKeywordsContextMap.size() && stackFrames[lowestStackFrameLevel] != null;
    }
    
    private IStackFrame extractPreviousStackFrame(final Map<String, KeywordContext> currentKeywordsContextMap,
            int numberOfStackFramesToCopy, int currentKeywordsCounter) {
        IStackFrame previousStackFrame = null;
        if (stackFrames.length < currentKeywordsContextMap.size()) {
            previousStackFrame = stackFrames[numberOfStackFramesToCopy - currentKeywordsCounter];
        } else {
            previousStackFrame = stackFrames[stackFrames.length - currentKeywordsCounter];
        }
        return previousStackFrame;
    }

    public void setHasStackFramesCreated(boolean hasStackFramesCreated) {
        this.hasStackFramesCreated = hasStackFramesCreated;
    }

    public void setStackFrames(IStackFrame[] stackFrames) {
        this.stackFrames = stackFrames;
    }

    public boolean hasStackFramesCreated() {
        return hasStackFramesCreated;
    }

}
