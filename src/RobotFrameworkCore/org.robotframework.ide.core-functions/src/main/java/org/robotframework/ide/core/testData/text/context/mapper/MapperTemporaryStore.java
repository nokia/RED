package org.robotframework.ide.core.testData.text.context.mapper;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;


public class MapperTemporaryStore {

    private ModelOutput model;
    private List<LineElement> currentLineElements;
    private List<RobotToken> tokensWithoutContext = new LinkedList<>();
    private List<IContextElement> nearestContexts = new LinkedList<>();
    private List<IContextElement> separatorsAndNormalCtxs = new LinkedList<>();
    private ElementType lastType;


    public MapperTemporaryStore(final ModelOutput model,
            final List<LineElement> elems) {
        this.model = model;
        this.currentLineElements = elems;
    }


    public void setTokensWithoutContext(final List<RobotToken> gapTokens) {
        this.tokensWithoutContext = gapTokens;
    }


    public void setSeparatorAndNormalContexts(
            final List<IContextElement> separatorsAndNormalCtxs) {
        this.separatorsAndNormalCtxs = separatorsAndNormalCtxs;
    }


    public ModelOutput getModel() {
        return model;
    }


    public void setModel(ModelOutput model) {
        this.model = model;
    }


    public List<LineElement> getCurrentLineElements() {
        return currentLineElements;
    }


    public void setCurrentLineElements(List<LineElement> currentLineElements) {
        this.currentLineElements = currentLineElements;
    }


    public List<IContextElement> getSeparatorsAndNormalCtxs() {
        return separatorsAndNormalCtxs;
    }


    public void setSeparatorsAndNormalCtxs(
            List<IContextElement> separatorsAndNormalCtxs) {
        this.separatorsAndNormalCtxs = separatorsAndNormalCtxs;
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
                .format("MapperTemporaryStore [model=%s, currentLineElements=%s, tokensWithoutContext=%s, nearestContexts=%s, separatorsAndNormalCtxs=%s, lastType=%s]",
                        model, currentLineElements, tokensWithoutContext,
                        nearestContexts, separatorsAndNormalCtxs, lastType);
    }
}