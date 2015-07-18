package org.robotframework.ide.core.testData.text.context.mapper;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;


public class MapperTemporaryStore {

    private ModelOutput model;
    private List<LineElement> currentLineElements;
    private List<RobotToken> tokensWithoutContext = new LinkedList<>();
    private List<IContextElement> separatorsAndNormalCtxs = new LinkedList<>();


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

}