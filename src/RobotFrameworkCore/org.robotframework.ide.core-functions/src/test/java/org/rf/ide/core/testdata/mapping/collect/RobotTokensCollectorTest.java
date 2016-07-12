/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.InOrder;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author wypych
 */
public class RobotTokensCollectorTest {

    @Test
    public void test_extractRobotTokens_logicCheck() {
        // prepare
        final RobotFileOutput tokensHolder = mock(RobotFileOutput.class);

        final List<ITableTokensCollector> collectors = new ArrayList<>();

        final ITableTokensCollector collectorOne = mock(ITableTokensCollector.class);
        final List<RobotToken> colOneToks = new ArrayList<>(0);
        when(collectorOne.collect(tokensHolder)).thenReturn(colOneToks);

        final ITableTokensCollector collectorTwo = mock(ITableTokensCollector.class);
        final List<RobotToken> colTwoToks = new ArrayList<>(0);
        when(collectorTwo.collect(tokensHolder)).thenReturn(colTwoToks);

        collectors.add(collectorOne);
        collectors.add(collectorTwo);

        final RobotTokensCollector genCollector = spy(new RobotTokensCollector(collectors));

        // execute
        final ListMultimap<RobotTokenType, RobotToken> toksExtracted = genCollector.extractRobotTokens(tokensHolder);

        // verify
        assertThat(toksExtracted.asMap()).isEmpty();
        assertThat(toksExtracted).isInstanceOf(ArrayListMultimap.class);
        InOrder order = inOrder(genCollector, collectorOne, collectorTwo);
        order.verify(collectorOne, times(1)).collect(tokensHolder);
        order.verify(genCollector, times(1)).update(colOneToks, toksExtracted);
        order.verify(collectorTwo, times(1)).collect(tokensHolder);
        order.verify(genCollector, times(1)).update(colTwoToks, toksExtracted);
        order.verifyNoMoreInteractions();
    }

    @Test
    public void test_update_forThreeTypesTokensAndThreeOfThem() {
        // prepare
        final RobotTokenType typeOne = RobotTokenType.START_HASH_COMMENT;
        final RobotTokenType typeTwo = RobotTokenType.COMMENT_CONTINUE;
        final RobotTokenType typeThree = RobotTokenType.COMMENT_TOKEN;

        final List<RobotToken> listTypeOne = createTokensOf(typeOne, 3);
        final List<RobotToken> listTypeTwo = createTokensOf(typeTwo, 3);
        final List<RobotToken> listTypeThree = createTokensOf(typeThree, 3);

        @SuppressWarnings("unchecked")
        final List<RobotToken> listWithAllMixed = mixTypes(listTypeOne, listTypeTwo, listTypeThree);
        final RobotTokensCollector genCollector = new RobotTokensCollector();
        final ListMultimap<RobotTokenType, RobotToken> tokensPerType = ArrayListMultimap.create();

        // execute
        genCollector.update(listWithAllMixed, tokensPerType);

        // verify
        final Map<RobotTokenType, Collection<RobotToken>> asMap = tokensPerType.asMap();
        assertThat(asMap).containsOnlyKeys(typeOne, typeTwo, typeThree);
        assertThat(asMap.get(typeOne)).containsExactlyElementsOf(listTypeOne);
        assertThat(asMap.get(typeTwo)).containsExactlyElementsOf(listTypeTwo);
        assertThat(asMap.get(typeThree)).containsExactlyElementsOf(listTypeThree);

    }

    
    private List<RobotToken> mixTypes(@SuppressWarnings("unchecked") final List<RobotToken>... typed) {
        List<RobotToken> joined = new ArrayList<>(0);

        int maxSize = 0;
        for (final List<RobotToken> p : typed) {
            maxSize = Math.max(maxSize, p.size());
        }

        for (int i = 0; i < maxSize; i++) {
            for (final List<RobotToken> t : typed) {
                if (t.size() > i) {
                    joined.add(t.get(i));
                }
            }
        }

        return joined;
    }

    private List<RobotToken> createTokensOf(final RobotTokenType type, int times) {
        final List<RobotToken> product = new ArrayList<>(times);

        for (int i = 0; i < times; i++) {
            RobotToken tok = new RobotToken();
            tok.setType(type);
            product.add(tok);
        }

        return product;
    }
}
