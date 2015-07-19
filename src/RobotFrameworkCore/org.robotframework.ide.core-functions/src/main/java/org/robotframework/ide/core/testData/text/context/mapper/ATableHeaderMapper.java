package org.robotframework.ide.core.testData.text.context.mapper;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.model.RobotLine;
import org.robotframework.ide.core.testData.model.RobotTestDataFile;
import org.robotframework.ide.core.testData.model.table.ATableModel;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.text.context.ContextOperationHelper;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput;
import org.robotframework.ide.core.testData.text.context.ModelBuilder.ModelOutput.BuildMessage;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.iterator.ContextTokenIterator.SeparationType;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


public abstract class ATableHeaderMapper implements IContextMapper {

    private final ElementType BUILD_TYPE;
    private final ContextOperationHelper coh;


    protected ATableHeaderMapper(final ElementType type) {
        this.BUILD_TYPE = type;
        this.coh = new ContextOperationHelper();
    }


    @Override
    public MapperOutput map(final MapperTemporaryStore store,
            final IContextElement thisContext) {
        MapperOutput mapOut = new MapperOutput();

        // extract elements from temp store
        final List<LineElement> lineElements = store.getCurrentLineElements();
        final ElementType lastType = store.getLastType();
        final SeparationType separatorType = store.getSeparatorType();
        final ModelOutput model = store.getModel();
        final RobotLine currentLine = store.getCurrentLine();

        OneLineSingleRobotContextPart c = (OneLineSingleRobotContextPart) thisContext;
        List<RobotToken> contextTokens = c.getContextTokens();
        FilePosition start = contextTokens.get(0).getStartPosition();
        FilePosition end = contextTokens.get(contextTokens.size() - 1)
                .getEndPosition();
        if (coh.isSeparatorOnly(lineElements, separatorType)) {
            RobotTestDataFile fileModel = model.getFileModel();
            ATableModel table = fileModel.getSettingsTable();

            LineElement elem = convert(contextTokens);
            lineElements.add(elem);
            if (table.isPresent()) {
                model.addBuildMessage(BuildMessage.buildWarn(
                        "Duplicate declaration of "
                                + table.getTableName()
                                + " table in the same file. Tables will be merged in test data model.",
                        "start " + start + ", end " + end));
            } else {
                table.setHeader(convert(contextTokens, currentLine, elem));
            }

            mapOut.setMappedElementType(BUILD_TYPE);
        } else {
            if (lastType == null || lastType == ElementType.TRASH_DATA) {
                handleTrashData(store, mapOut, contextTokens, model, start, end);
            } else {
                // could be inside test case or comment
            }
        }

        mapOut.setNextPosition(end);

        return mapOut;
    }


    private void handleTrashData(final MapperTemporaryStore store,
            MapperOutput mapOut, final List<RobotToken> tokensWithoutContext,
            final ModelOutput model, FilePosition trashDataBeginPosition,
            FilePosition trashDataEndPosition) {
        addInformationAboutTrashDataFound(model, trashDataBeginPosition,
                trashDataEndPosition);
        store.setCurrentLineElements(convertToTrashData(tokensWithoutContext));
        mapOut.setNextPosition(trashDataEndPosition);
        mapOut.setMappedElementType(ElementType.TRASH_DATA);
    }


    @VisibleForTesting
    protected List<LineElement> convertToTrashData(final List<RobotToken> tokens) {
        List<LineElement> elems = new LinkedList<>();
        for (RobotToken rt : tokens) {
            LineElement le = new LineElement();
            List<ElementType> et = new LinkedList<>();
            et.add(0, ElementType.TRASH_DATA);
            et.add(BUILD_TYPE);
            le.setElementTypes(et);
            le.setValue(rt.getText());
            elems.add(le);
        }

        return elems;
    }


    @VisibleForTesting
    protected void addInformationAboutTrashDataFound(final ModelOutput model,
            final FilePosition trashBegin, final FilePosition trashEnd) {
        model.addBuildMessage(BuildMessage
                .buildWarn(
                        "Garbage data found in file. It is recommand to use hash sign ('#') to comment out some user text without meaning.",
                        "start " + trashBegin + ", end " + trashEnd));
    }


    @VisibleForTesting
    protected LineElement convert(final List<RobotToken> contextTokens) {
        LineElement le = new LineElement();
        for (RobotToken rt : contextTokens) {
            le.appendValue(rt.getText());
        }
        List<ElementType> types = new LinkedList<>();
        types.add(BUILD_TYPE);
        le.setElementTypes(types);

        return le;
    }


    @VisibleForTesting
    protected TableHeader convert(final List<RobotToken> contextTokens,
            final RobotLine containingLine, final LineElement originalElement) {
        TableHeader th = new TableHeader(BUILD_TYPE, containingLine,
                originalElement);

        return th;
    }
}
