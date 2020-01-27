/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;

public class CodeElementsTreeFormatTest {

    private static Stream<Arguments> provideTestData() throws Exception {
        return Stream.of(Arguments.of(createCases()), Arguments.of(createKeywordDefinitions()));
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void pathForCodeHoldingElement_consistOnlyOfThisElement(final List<RobotCodeHoldingElement<?>> codeHolders) {
        final CodeElementsTreeFormat format = new CodeElementsTreeFormat();

        for (final RobotCodeHoldingElement<?> codeHolder : codeHolders) {
            final List<Object> path = new ArrayList<>();
            format.getPath(path, codeHolder);

            assertThat(path).containsExactly(codeHolder);
        }
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void pathForKeywordCall_consistParentCodeHolderAndKeywordCallItself(
            final List<RobotCodeHoldingElement<?>> codeHolders) {
        final CodeElementsTreeFormat format = new CodeElementsTreeFormat();

        for (final RobotCodeHoldingElement<?> codeHolder : codeHolders) {
            for (final RobotKeywordCall call : codeHolder.getChildren()) {
                final List<Object> path = new ArrayList<>();
                format.getPath(path, call);

                assertThat(path).containsExactly(codeHolder, call);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    public void pathForNestedAddingTokens_consistOfTokenParentAndTokenItself(
            final List<RobotCodeHoldingElement<?>> codeHolders) {
        final CodeElementsTreeFormat format = new CodeElementsTreeFormat();

        for (final RobotCodeHoldingElement<?> codeHolder : codeHolders) {
            final AddingToken token = new AddingToken(codeHolder, mock(TokenState.class));

            final List<Object> path = new ArrayList<>();
            format.getPath(path, token);

            assertThat(path).containsExactly(codeHolder, token);
        }
    }

    @Test
    public void casesPreserveNaturalOrder_whenThereIsNoSortModel() {
        sortAndVerify(null, createCases(), newArrayList(
                "case xyz", "tags", "xyz_1", "xyz_2", "...",
                "case", "a", "b", "...",
                "case abc", "abc_1", "abc_2", "...",
                "case", "c", "d", "..."));
    }

    @Test
    public void casesPreserveNaturalOrder_whenThereIsSortModelButThereIsNoSortingDirection() {
        final ISortModel sortModel = mock(ISortModel.class);
        when(sortModel.getSortDirection(0)).thenReturn(SortDirectionEnum.NONE);

        sortAndVerify(sortModel, createCases(), newArrayList(
                "case xyz", "tags", "xyz_1", "xyz_2", "...",
                "case", "a", "b", "...",
                "case abc", "abc_1", "abc_2", "...",
                "case", "c", "d", "..."));
    }

    @Test
    public void casesAreSortedProperly_whenSortDirectionIsAscending() {
        final ISortModel sortModel = mock(ISortModel.class);
        when(sortModel.getSortDirection(0)).thenReturn(SortDirectionEnum.ASC);

        sortAndVerify(sortModel, createCases(), newArrayList(
                "case", "a", "b", "...",
                "case", "c", "d", "...",
                "case abc", "abc_1", "abc_2", "...",
                "case xyz", "tags", "xyz_1", "xyz_2", "..."));
    }

    @Test
    public void casesAreSortedProperly_whenSortDirectionIsDescending() {
        final ISortModel sortModel = mock(ISortModel.class);
        when(sortModel.getSortDirection(0)).thenReturn(SortDirectionEnum.DESC);

        sortAndVerify(sortModel, createCases(), newArrayList(
                "case xyz", "tags", "xyz_1", "xyz_2", "...",
                "case abc", "abc_1", "abc_2", "...",
                "case", "c", "d", "...",
                "case", "a", "b", "..."));
    }

    @Test
    public void keywordsPreserveNaturalOrder_whenThereIsNoSortModel() {
        sortAndVerify(null, createKeywordDefinitions(), newArrayList(
                "kw xyz", "tags", "xyz_1", "xyz_2", "...",
                "kw", "a", "b", "...",
                "kw abc", "abc_1", "abc_2", "...",
                "kw", "c", "d", "..."));
    }

    @Test
    public void keywordsPreserveNaturalOrder_whenThereIsSortModelButThereIsNoSortingDirection() {
        final ISortModel sortModel = mock(ISortModel.class);
        when(sortModel.getSortDirection(0)).thenReturn(SortDirectionEnum.NONE);

        sortAndVerify(sortModel, createKeywordDefinitions(), newArrayList(
                "kw xyz", "tags", "xyz_1", "xyz_2", "...",
                "kw", "a", "b", "...",
                "kw abc", "abc_1", "abc_2", "...",
                "kw", "c", "d", "..."));
    }

    @Test
    public void keywordsAreSortedProperly_whenSortDirectionIsAscending() {
        final ISortModel sortModel = mock(ISortModel.class);
        when(sortModel.getSortDirection(0)).thenReturn(SortDirectionEnum.ASC);

        sortAndVerify(sortModel, createKeywordDefinitions(), newArrayList(
                "kw", "a", "b", "...",
                "kw", "c", "d", "...",
                "kw abc", "abc_1", "abc_2", "...",
                "kw xyz", "tags", "xyz_1", "xyz_2", "..."));
    }

    @Test
    public void keywordsAreSortedProperly_whenSortDirectionIsDescending() {
        final ISortModel sortModel = mock(ISortModel.class);
        when(sortModel.getSortDirection(0)).thenReturn(SortDirectionEnum.DESC);

        sortAndVerify(sortModel, createKeywordDefinitions(), newArrayList(
                "kw xyz", "tags", "xyz_1", "xyz_2", "...",
                "kw abc", "abc_1", "abc_2", "...",
                "kw", "c", "d", "...",
                "kw", "a", "b", "..."));
    }

    private static void sortAndVerify(final ISortModel model, final List<? extends RobotCodeHoldingElement<?>> holders,
            final List<String> expectedOrderedElements) {
        final CodeElementsTreeFormat format = new CodeElementsTreeFormat();
        format.setSortModel(model);

        final List<Node> nodes = createNodes(holders);
        Collections.sort(nodes, new NodeComparator(format));

        assertThat(nodes).extracting(CodeElementsTreeFormatTest::toName)
                .containsExactlyElementsOf(expectedOrderedElements);
    }

    private static List<Node> createNodes(final List<? extends RobotCodeHoldingElement<?>> holders) {
        final List<Node> nodes = new ArrayList<>();
        for (final RobotCodeHoldingElement<?> holder : holders) {
            nodes.add(new Node(holder));
            for (final RobotKeywordCall call : holder.getChildren()) {
                nodes.add(new Node(holder, call));
            }
            nodes.add(new Node(holder, new AddingToken(holder, mock(TokenState.class))));
        }
        return nodes;
    }

    private static String toName(final Node node) {
        final Object last = node.pathElements[node.pathElements.length - 1];
        return last instanceof AddingToken ? "..." : ((RobotElement) last).getName();
    }

    private static List<RobotCase> createCases() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case xyz")
                .appendLine("  [tags]   t")
                .appendLine("  xyz_1")
                .appendLine("  xyz_2")
                .appendLine("case")
                .appendLine("  a")
                .appendLine("  b")
                .appendLine("case abc")
                .appendLine("  abc_1")
                .appendLine("  abc_2")
                .appendLine("case")
                .appendLine("  c")
                .appendLine("  d")
                .build();
        return model.findSection(RobotCasesSection.class).get().getChildren();
    }

    private static List<RobotKeywordDefinition> createKeywordDefinitions() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("kw xyz")
                .appendLine("  [tags]   t")
                .appendLine("  xyz_1")
                .appendLine("  xyz_2")
                .appendLine("kw")
                .appendLine("  a")
                .appendLine("  b")
                .appendLine("kw abc")
                .appendLine("  abc_1")
                .appendLine("  abc_2")
                .appendLine("kw")
                .appendLine("  c")
                .appendLine("  d")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren();
    }

    private static final class Node {

        private final Object[] pathElements;

        public Node(final Object... elements) {
            this.pathElements = elements;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Node) {
                final Node that = (Node) obj;
                return Arrays.equals(this.pathElements, that.pathElements);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(pathElements);
        }
    }

    private static class NodeComparator implements Comparator<Node> {

        // comparator is based on ca.odell.glazedlists.TreeList.NodeComparator<E> class in order
        // to simulate its behavior

        private final CodeElementsTreeFormat format;

        public NodeComparator(final CodeElementsTreeFormat format) {
            this.format = format;
        }

        @Override
        public int compare(final Node a, final Node b) {
            final int aPathLength = a.pathElements.length;
            final int bPathLength = b.pathElements.length;

            final boolean aAllowsChildren = format.allowsChildren(a.pathElements[aPathLength - 1]);
            final boolean bAllowsChildren = format.allowsChildren(b.pathElements[bPathLength - 1]);
            final int aEffectiveLength = aPathLength + (aAllowsChildren ? 0 : -1);
            final int bEffectiveLength = bPathLength + (bAllowsChildren ? 0 : -1);

            // compare by value first
            for (int d = 0; d < aEffectiveLength && d < bEffectiveLength; d++) {
                final Comparator<Object> comparator = format.getComparator(d);
                if (comparator == null) {
                    return 0;
                }
                final int result = comparator.compare(a.pathElements[d], b.pathElements[d]);
                if (result != 0) {
                    return result;
                }
            }
            // and path length second
            return aEffectiveLength - bEffectiveLength;
        }
    }
}
