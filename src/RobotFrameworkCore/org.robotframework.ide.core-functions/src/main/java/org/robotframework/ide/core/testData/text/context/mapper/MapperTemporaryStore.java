package org.robotframework.ide.core.testData.text.context.mapper;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput;
import org.robotframework.ide.core.testData.text.context.iterator.ContextTokenIterator.SeparationType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;


public class MapperTemporaryStore {

    private ModelOutput model;
    private List<LineElement> currentLineElements;
    private List<RobotToken> tokensWithoutContext = new LinkedList<>();
    private List<IContextElement> nearestContexts = new LinkedList<>();
    private List<IContextElement> normalContexts = new LinkedList<>();
    private List<IContextElement> separatorContexts = new LinkedList<>();
    private final RobotLine currentLine;
    private SeparationType separatorType = SeparationType.WHITESPACES;
    private ElementType lastType;


    public MapperTemporaryStore(final ModelOutput model,
            final List<LineElement> elems, final RobotLine currentLine) {
        this.model = model;
        this.currentLineElements = elems;
        this.currentLine = currentLine;
    }


    public List<IContextElement> getNormalContexts() {
        return normalContexts;
    }


    public void setNormalContexts(List<IContextElement> normalContexts) {
        this.normalContexts = normalContexts;
    }


    public SeparationType getSeparatorType() {
        return separatorType;
    }


    public void setSeparatorType(SeparationType separatorType) {
        this.separatorType = separatorType;
    }


    public List<IContextElement> getSeparatorContexts() {
        return separatorContexts;
    }


    public void setSeparatorContexts(List<IContextElement> separatorContexts) {
        this.separatorContexts = separatorContexts;
    }


    public void setTokensWithoutContext(final List<RobotToken> gapTokens) {
        this.tokensWithoutContext = gapTokens;
    }


    public ModelOutput getModel() {
        return model;
    }


    public void setModel(ModelOutput model) {
        this.model = model;
    }


    public RobotLine getCurrentLine() {
        return currentLine;
    }


    public void appendCurrentLineElements(List<LineElement> elems) {
        currentLineElements.addAll(elems);
    }


    public void appendCurrentLineElements(LineElement elem) {
        currentLineElements.add(elem);
    }


    public List<LineElement> getCurrentLineElements() {
        return currentLineElements;
    }


    public void setCurrentLineElements(List<LineElement> currentLineElements) {
        this.currentLineElements = currentLineElements;
    }


    public List<RobotToken> getTokensWithoutContext() {
        return tokensWithoutContext;
    }


    public ElementType getLastType() {
        return lastType;
    }


    public void setLastType(ElementType lastType) {
        this.lastType = lastType;
    }


    public List<IContextElement> getNearestContexts() {
        return nearestContexts;
    }


    public void setNearestContexts(List<IContextElement> nearestContexts) {
        this.nearestContexts = nearestContexts;
    }


    @Override
    public String toString() {
        return String
                .format("MapperTemporaryStore [model=%s, currentLineElements=%s, tokensWithoutContext=%s, nearestContexts=%s, normalContexts=%s, separatorContexts=%s, separatorType=%s, lastType=%s]",
                        model, currentLineElements, tokensWithoutContext,
                        nearestContexts, normalContexts, separatorContexts,
                        separatorType, lastType);
    }
}