/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.toList;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.Streams;
import com.google.common.collect.TreeRangeMap;

class ShellDocument extends Document {

    static final String SEPARATOR = "    ";
    static final String CATEGORY_MODE_PROMPT = "mode_prompt";
    static final String CATEGORY_PROMPT_CONTINUATION = "prompt_continuation";
    static final String CATEGORY_AWAITING_RESULT_PREFIX = "awaiting_result";
    static final String CATEGORY_RESULT_SUCC = "result";
    static final String CATEGORY_RESULT_ERROR = "error";

    static boolean isModePromptCategory(final String category) {
        return CATEGORY_MODE_PROMPT.equals(category);
    }

    static boolean isPromptContinuationCategory(final String category) {
        return CATEGORY_PROMPT_CONTINUATION.equals(category);
    }

    static boolean isResultPassCategory(final String category) {
        return CATEGORY_RESULT_SUCC.equals(category);
    }

    static boolean isResultFailCategory(final String category) {
        return CATEGORY_RESULT_ERROR.equals(category);
    }

    private final List<ShellDocumentListener> listeners = new ArrayList<>();

    private final String delimiter;
    private ExpressionType type;

    private final List<Expression> expressionsHistory = new ArrayList<>();
    private int currentExpressionInHistory = 1;

    private final Queue<Supplier<CategorizedPosition>> awaitingPositions = new ArrayDeque<>();

    ShellDocument() {
        this(null);
    }

    ShellDocument(final String delimiter) {
        Preconditions.checkArgument(delimiter == null || Arrays.asList(getLegalLineDelimiters()).contains(delimiter));
        this.delimiter = delimiter;
        this.type = ExpressionType.ROBOT;

        addPositionCategory(CATEGORY_MODE_PROMPT);
        addPositionCategory(CATEGORY_PROMPT_CONTINUATION);
        addPositionCategory(CATEGORY_RESULT_SUCC);
        addPositionCategory(CATEGORY_RESULT_ERROR);

        reset();
    }

    void reset() {
        getModePromptPositionsStream().forEach(this::removePosition);
        getPromptContinuationPositionsStream().forEach(this::removePosition);
        getAwaitingResultsPositionsStream().forEach(this::removePosition);
        getResultSuccessPositionsStream().forEach(this::removePosition);
        getResultErrorPositionsStream().forEach(this::removePosition);

        removeEmptyAwaitingResultCategories();
        
        addAwaitingPositionOnWholeLine(CATEGORY_MODE_PROMPT, 0);
        set(type.name() + "> ");

        currentExpressionInHistory = expressionsHistory.size();
    }

    void addListener(final ShellDocumentListener listener) {
        listeners.add(listener);
    }

    void removeListener(final ShellDocumentListener listener) {
        listeners.remove(listener);
    }

    ExpressionType getMode() {
        return type;
    }

    boolean isInEditEnabledRegion(final int caretOffset) {
        return getEditablePositions().stream().anyMatch(pos -> pos.includes(caretOffset));
    }

    private List<Position> getEditablePositions() {
        final List<Position> regions = new ArrayList<>();

        final int lastLine = getNumberOfLines();
        for (int i = lastLine - 1; i >= 0; i--) {
            final String line = getLine(i);
            
            if (!isExpressionPromptLine(line) && !isExpressionContinuationLine(line)) {
                break;
            }
            final IRegion lineRegion = getLineInformation(i);
            final int shift = line.indexOf(' ');
            regions.add(new Position(lineRegion.getOffset() + shift + 1, lineRegion.getLength() - shift));
        }
        return regions;
    }

    private static boolean isExpressionPromptLine(final String line) {
        return EnumSet.allOf(ExpressionType.class)
                .stream()
                .map(t -> t.name() + "> ")
                .anyMatch(prompt -> line.startsWith(prompt));
    }

    private static boolean isExpressionContinuationLine(final String line) {
        return EnumSet.allOf(ExpressionType.class)
                .stream()
                .map(t -> Strings.repeat(".", t.name().length() + 1) + " ")
                .anyMatch(prompt -> line.startsWith(prompt));
    }

    boolean isExpressionPromptLine(final int offset) {
        final IRegion lineInfo = getLineInformationOfOffset(offset);
        return getModePromptPositionsStream().filter(pos -> pos.getOffset() == lineInfo.getOffset())
                .findAny()
                .isPresent();
    }

    void executeExpression(final Function<String, Integer> evaluationRequest) {
        final Expression expression = getExpression();
        final int exprId = evaluationRequest.apply(expression.expression);

        final int nextLineNumber = getNumberOfLines();
        final String resultCategory = CATEGORY_AWAITING_RESULT_PREFIX + "_" + exprId;
        addAwaitingPositionOnWholeLine(resultCategory, nextLineNumber);
        addAwaitingPositionOnWholeLine(CATEGORY_MODE_PROMPT, nextLineNumber + 1);

        appendLines("evaluating...", type.name() + "> ");

        expressionsHistory.add(expression);
        while (expressionsHistory.size() > 50) {
            expressionsHistory.remove(0);
        }
        currentExpressionInHistory = expressionsHistory.size();
    }

    private Expression getExpression() {
        final Range<Integer> currentExpressionLines = getLinesOfExpression(getLength());
        final List<String> lines = new ArrayList<>();
        for (int i = currentExpressionLines.lowerEndpoint(); i <= currentExpressionLines.upperEndpoint(); i++) {
            lines.add(getLine(i).substring(type.name().length() + 2));
        }

        if (type == ExpressionType.ROBOT) {
            return new Expression(type, String.join(SEPARATOR, lines));
        } else if (type == ExpressionType.PYTHON) {
            return new Expression(type, String.join("\n", lines));
        } else if (type == ExpressionType.VARIABLE) {
            return new Expression(type, String.join("", lines));
        } else {
            throw new IllegalStateException();
        }
    }

    void putEvaluationResult(final int exprId, final ExpressionType type, final Optional<String> result,
            final Optional<String> error) {
        final String resultCategory = CATEGORY_AWAITING_RESULT_PREFIX + "_" + exprId;
        final Position[] positions = getPositions(resultCategory);
        final CategorizedPosition awaitingResPosition = getAwaitingResultPositionStream(exprId)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
        final int awaitingResOffset = awaitingResPosition.getOffset();
        final int awaitingResLenght = awaitingResPosition.getLength();
        for (final Position p : positions) {
            removePosition(resultCategory, p);
        }
        removePositionCategory(resultCategory);

        String toPrint;
        CategorizedPosition newResultPosition;
        if (result.isPresent() && type == ExpressionType.ROBOT) {
            final String passPrefix = "PASS: ";

            toPrint = passPrefix + result.get();
            newResultPosition = new CategorizedPosition(CATEGORY_RESULT_SUCC,
                    new Position(awaitingResOffset, passPrefix.length()));

        } else if (result.isPresent()) {
            toPrint = result.get();
            newResultPosition = null;

        } else if (error.isPresent() && type == ExpressionType.ROBOT) {
            final String failPrefix = "FAIL: ";

            toPrint = failPrefix + error.get();
            newResultPosition = new CategorizedPosition(CATEGORY_RESULT_ERROR,
                    new Position(awaitingResOffset, failPrefix.length()));

        } else if (error.isPresent()) {
            toPrint = error.get();
            newResultPosition = new CategorizedPosition(CATEGORY_RESULT_ERROR,
                    new Position(awaitingResOffset, toPrint.length()));

        } else {
            throw new IllegalStateException();
        }

        replace(awaitingResOffset, awaitingResLenght, toPrint);
        addPosition(newResultPosition);
        shiftPositions(awaitingResOffset, toPrint.length() - awaitingResLenght);

        listeners.forEach(ShellDocumentListener::resultWritten);
    }

    void continueExpressionInNewLine() {
        final int nextLineNumber = getNumberOfLines();

        addAwaitingPositionOnWholeLine(CATEGORY_PROMPT_CONTINUATION, nextLineNumber);
        appendLines(Strings.repeat(".", type.name().length() + 1) + " ");
    }

    void switchToMode(final ExpressionType mode) {
        if (mode != null) {
            switchMode(mode);
        } else {
            final ExpressionType[] allTypes = ExpressionType.values();
            final int nextTypeIndex = (newArrayList(allTypes).indexOf(type) + 1) % allTypes.length;
            switchMode(allTypes[nextTypeIndex]);
        }
    }

    private void switchMode(final ExpressionType newType) {
        final CategorizedPosition lastModePosition = getModePromptPositionsStream()
                .collect(maxBy(Comparator.comparing(CategorizedPosition::getOffset)))
                .orElseThrow(IllegalStateException::new);
        final int modeOffset = lastModePosition.getOffset();

        final int delta = newType.name().length() - type.name().length();

        replace(modeOffset, lastModePosition.getLength(), newType.name() + "> ");
        extendPositions(modeOffset, delta);
        shiftPositions(modeOffset, delta);

        if (delta != 0) {
            final List<CategorizedPosition> continuations = getPromptContinuationPositionsStream()
                    .filter(p -> p.getOffset() > modeOffset)
                    .sorted((p1, p2) -> Integer.compare(p1.getOffset(), p2.getOffset()))
                    .collect(toList());

            while (!continuations.isEmpty()) {
                final CategorizedPosition p = continuations.remove(0);
                final String dots = Strings.repeat(".", newType.name().length() + 1);

                replace(p.getOffset(), p.getLength(), dots + " ");
                extendPositions(p.getOffset(), delta);
                shiftPositions(p.getOffset(), delta);
            }
        }
        
        this.type = newType;
    }

    void switchToPreviousExpression() {
        if (currentExpressionInHistory == 0 || currentExpressionInHistory == 1 && expressionsHistory.isEmpty()) {
            return;
        }
        currentExpressionInHistory--;
        switchTo(expressionsHistory.get(currentExpressionInHistory));
    }

    void switchToNextExpression() {
        if (currentExpressionInHistory >= expressionsHistory.size() - 1) {
            return;
        }
        currentExpressionInHistory++;
        switchTo(expressionsHistory.get(currentExpressionInHistory));
    }

    private void switchTo(final Expression expression) {
        cleanCurrentExpression();
        if (type != expression.type) {
            switchMode(expression.type);
        }
        insertNewExpression(expression.expression);
    }

    void switchTo(final ExpressionType type, final String expression) {
        switchTo(new Expression(type, expression));
    }

    private void cleanCurrentExpression() {
        final Range<Integer> currentExpressionLines = getLinesOfExpression(getLength());
        final IRegion startLineInfo = getLineInformation(currentExpressionLines.lowerEndpoint());

        final int toRemoveStart = startLineInfo.getOffset() + type.name().length() + 2;

        replace(toRemoveStart, getLength() - toRemoveStart, "");

        getPromptContinuationPositionsStream().filter(p -> p.getOffset() > toRemoveStart).forEach(this::removePosition);
    }

    private void insertNewExpression(final String expression) {
        final String[] lines = expression.split("\\r?\\n");
        
        append(lines[0]);
        for (int i = 1; i < lines.length; i++) {
            final int nextLineNumber = getNumberOfLines();

            addAwaitingPositionOnWholeLine(CATEGORY_PROMPT_CONTINUATION, nextLineNumber);
            appendLines(Strings.repeat(".", type.name().length() + 1) + " ");

            append(lines[i]);
        }
    }

    String getLine(final int line) {
        try {
            final IRegion lastLineRegion = getLineInformation(line);
            return get(lastLineRegion.getOffset(), lastLineRegion.getLength());

        } catch (final BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    Range<Integer> getLinesOfExpression(final int offset) {
        try {
            final int line = getLineOfOffset(offset);

            int start = line;
            int end = line;
            final Predicate<String> linePredicate = l -> isExpressionPromptLine(l) || isExpressionContinuationLine(l);
            if (linePredicate.test(getLine(line))) {
                for (int i = line - 1; i >= 0 && linePredicate.test(getLine(i)); i--) {
                    start--;
                }
                for (int i = line + 1; i < getNumberOfLines() && linePredicate.test(getLine(i)); i++) {
                    end++;
                }
            } else {
                for (int i = line - 1; i >= 0 && !linePredicate.test(getLine(i)); i--) {
                    start--;
                }
                for (int i = line + 1; i < getNumberOfLines() && !linePredicate.test(getLine(i)); i++) {
                    end++;
                }
            }
            return Range.closed(start, Math.min(end, this.getNumberOfLines() - 1));

        } catch (final BadLocationException e) {
            return Range.closed(0, 0);
        }
    }

    void append(final String toAppend) {
        replace(getLength(), 0, toAppend);
    }

    private void appendLines(final String... lines) {
        appendLines(newArrayList(lines));
    }

    private void appendLines(final List<String> lines) {
        final String delimiter = this.delimiter != null ? this.delimiter : DocumentUtilities.getDelimiter(this);
        final String text = delimiter + String.join(delimiter, lines);
        replace(getLength(), 0, text);
    }

    @Override
    public void replace(final int offset, final int length, final String text) {
        try {
            super.replace(offset, length, text);
        } catch (final BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    private void addPosition(final CategorizedPosition categorizedPosition) {
        if (categorizedPosition != null) {
            addPosition(categorizedPosition.category, categorizedPosition.position);
        }
    }

    @Override
    public void addPosition(final String category, final Position position) {
        try {
            super.addPosition(category, position);
        } catch (BadLocationException | BadPositionCategoryException e) {
            throw new IllegalStateException(e);
        }
    }

    private void removePosition(final CategorizedPosition categorizedPosition) {
        if (categorizedPosition != null) {
            removePosition(categorizedPosition.category, categorizedPosition.position);
        }
    }

    @Override
    public void removePosition(final String category, final Position position) {
        try {
            super.removePosition(category, position);
        } catch (final BadPositionCategoryException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @Override
    public void removePositionCategory(final String category) {
        try {
            super.removePositionCategory(category);
        } catch (final BadPositionCategoryException e) {
            throw new IllegalStateException(e);
        }
    }

    private void shiftPositions(final int startingAfterOffset, final int delta) {
        Streams.concat(getModePromptPositionsStream(),
                       getPromptContinuationPositionsStream(),
                       getAwaitingResultsPositionsStream(),
                       getResultSuccessPositionsStream(),
                       getResultErrorPositionsStream())
                .filter(p -> p.getOffset() > startingAfterOffset)
                .forEach(p -> p.setOffset(p.getOffset() + delta));
    }
    
    private void extendPositions(final int overlappingOffset, final int delta) {
        Streams.concat(getModePromptPositionsStream(),
                       getPromptContinuationPositionsStream(),
                       getAwaitingResultsPositionsStream(),
                       getResultSuccessPositionsStream(),
                       getResultErrorPositionsStream())
                 .filter(p -> p.getOffset() <= overlappingOffset && overlappingOffset < p.getOffset() + p.getLength())
                 .forEach(p -> p.setLength(p.getLength() + delta));
    }

    @Override
    public Position[] getPositions(final String category) {
        try {
            return super.getPositions(category);
        } catch (final BadPositionCategoryException e) {
            throw new IllegalStateException(e);
        }
    }

    RangeMap<Integer, String> getPositionsRanges() {
        final RangeMap<Integer, String> positions = TreeRangeMap.create();
        Streams.concat(getModePromptPositionsStream(), getPromptContinuationPositionsStream(),
                getResultSuccessPositionsStream(), getResultErrorPositionsStream())
                .forEach(p -> positions.put(Range.closedOpen(p.getOffset(), p.getOffset() + p.getLength()),
                        p.getCategory()));
        return positions;
    }

    Stream<CategorizedPosition> getModePromptPositionsStream() {
        return Stream.of(getPositions(CATEGORY_MODE_PROMPT))
                .map(p -> new CategorizedPosition(CATEGORY_MODE_PROMPT, p));
    }

    Stream<CategorizedPosition> getPromptContinuationPositionsStream() {
        return Stream.of(getPositions(CATEGORY_PROMPT_CONTINUATION))
                .map(p -> new CategorizedPosition(CATEGORY_PROMPT_CONTINUATION, p));
    }

    Stream<CategorizedPosition> getResultSuccessPositionsStream() {
        return Stream.of(getPositions(CATEGORY_RESULT_SUCC))
                .map(p -> new CategorizedPosition(CATEGORY_RESULT_SUCC, p));
    }

    Stream<CategorizedPosition> getResultErrorPositionsStream() {
        return Stream.of(getPositions(CATEGORY_RESULT_ERROR))
                .map(p -> new CategorizedPosition(CATEGORY_RESULT_ERROR, p));
    }

    private Stream<CategorizedPosition> getAwaitingResultPositionStream(final int id) {
        final String category = CATEGORY_AWAITING_RESULT_PREFIX + "_" + id;
        return Stream.of(getPositions(category)).map(p -> new CategorizedPosition(category, p));
    }

    private void removeEmptyAwaitingResultCategories() {
        getAwaitingResultCategoriesStream().filter(cat -> getPositions(cat).length == 0)
                .forEach(this::removePositionCategory);
    }

    Stream<CategorizedPosition> getAwaitingResultsPositionsStream() {
        return getAwaitingResultCategoriesStream().filter(cat -> getPositions(cat).length > 0)
                .flatMap(cat -> Stream.of(getPositions(cat)).map(p -> new CategorizedPosition(cat, p)));
    }

    Stream<String> getAwaitingResultCategoriesStream() {
        return Stream.of(getPositionCategories()).filter(cat -> cat.startsWith(CATEGORY_AWAITING_RESULT_PREFIX));
    }

    Optional<Integer> getLineStartOffsetOmittingPrompt(final int offset) {
        return getPromptPosition(offset).map(pos -> pos.getOffset() + pos.getLength());
    }

    Optional<Integer> getPromptLenght(final int offset) {
        return getPromptPosition(offset).map(CategorizedPosition::getLength);
    }

    private Optional<CategorizedPosition> getPromptPosition(final int offset) {
        final IRegion lineInfo = getLineInformationOfOffset(offset);
        return Streams.concat(getModePromptPositionsStream(), getPromptContinuationPositionsStream())
                .filter(pos -> pos.getOffset() == lineInfo.getOffset())
                .findFirst();
    }

    @Override
    public IRegion getLineInformation(final int line) {
        try {
            return super.getLineInformation(line);
        } catch (final BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public IRegion getLineInformationOfOffset(final int offset) {
        try {
            return super.getLineInformationOfOffset(offset);
        } catch (final BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getLineDelimiter(final int line) {
        try {
            return super.getLineDelimiter(line);
        } catch (final BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Position toPosition(final IRegion region) {
        return new Position(region.getOffset(), region.getLength());
    }

    private void addAwaitingPositionOnWholeLine(final String category, final int line) {
        addAwaitingPosition(() -> new CategorizedPosition(category, toPosition(getLineInformation(line))));
    }

    private void addAwaitingPosition(final Supplier<CategorizedPosition> positionSupplier) {
        awaitingPositions.add(positionSupplier);
    }

    @Override
    protected void updateDocumentStructures(final DocumentEvent event) {
        while (!awaitingPositions.isEmpty()) {
            final CategorizedPosition pos = awaitingPositions.poll().get();
            addPositionCategory(pos.category);
            addPosition(pos);
        }
        super.updateDocumentStructures(event);
    }

    @FunctionalInterface
    interface ShellDocumentListener {

        void resultWritten();
    }

    private static class Expression {

        private final ExpressionType type;

        private final String expression;

        private Expression(final ExpressionType type, final String expression) {
            this.type = type;
            this.expression = expression;
        }
    }

    static final class CategorizedPosition {

        private final String category;

        private final Position position;

        CategorizedPosition(final String category, final int offset, final int lenght) {
            this(category, new Position(offset, lenght));
        }

        CategorizedPosition(final String category, final Position position) {
            this.category = category;
            this.position = position;
        }

        String getCategory() {
            return category;
        }

        int getOffset() {
            return position.getOffset();
        }

        void setOffset(final int offset) {
            position.setOffset(offset);
        }

        int getLength() {
            return position.getLength();
        }

        void setLength(final int length) {
            position.setLength(length);
        }

        Position getPosition() {
            return position;
        }

        @Override
        public String toString() {
            return category + ": " + position.toString();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == CategorizedPosition.class) {
                final CategorizedPosition that = (CategorizedPosition) obj;
                return this.category.equals(that.category) && this.position.equals(that.position);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(category, position);
        }
    }
}
