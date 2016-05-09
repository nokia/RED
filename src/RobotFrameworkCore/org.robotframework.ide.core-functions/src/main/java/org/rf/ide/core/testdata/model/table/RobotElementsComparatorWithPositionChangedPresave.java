/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class RobotElementsComparatorWithPositionChangedPresave implements Comparator<IRobotLineElement> {

    private final Map<IRobotTokenType, Integer> typesToHierarchy = new LinkedHashMap<IRobotTokenType, Integer>();

    private final Multimap<IRobotTokenType, IRobotLineElement> typeToTokens = ArrayListMultimap.create();

    private final RobotTokenPositionComparator posComperator = new RobotTokenPositionComparator();

    public void addPresaveSequenceForType(final IRobotTokenType type, final int hierarchyPoint,
            final List<? extends IRobotLineElement> elems) {
        typesToHierarchy.put(type, hierarchyPoint);
        typeToTokens.putAll(type, elems);
    }

    public Optional<IRobotTokenType> findType(final IRobotLineElement elem) {
        Optional<IRobotTokenType> type = Optional.absent();
        for (final IRobotTokenType currentType : typeToTokens.keySet()) {
            final Collection<IRobotLineElement> list = typeToTokens.get(currentType);
            for (final IRobotLineElement e : list) {
                if (e == elem) {
                    type = Optional.of(currentType);
                    break;
                }
            }

            if (type.isPresent()) {
                break;
            }
        }

        return type;
    }

    public Multimap<IRobotLineElement, Integer> indexesOf(final Collection<IRobotLineElement> list,
            final IRobotLineElement... elements) {
        final List<IRobotLineElement> elems = new ArrayList<>(Arrays.asList(elements));
        final Multimap<IRobotLineElement, Integer> found = ArrayListMultimap.create();
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            for (final IRobotLineElement e : elems) {
                if (e == list.toArray()[i]) {
                    found.put(e, i);
                }
            }
        }

        return found;
    }

    @Override
    public int compare(final IRobotLineElement o1, final IRobotLineElement o2) {
        int result = ECompareResult.EQUAL_TO.getValue();

        final Optional<IRobotTokenType> o1TypeOP = findType(o1);
        final Optional<IRobotTokenType> o2TypeOP = findType(o2);

        int posComperatorResult = posComperator.compare(o1, o2);
        if (o1TypeOP.isPresent() && o2TypeOP.isPresent()) {
            if (o1TypeOP.get() == o2TypeOP.get()) {
                Multimap<IRobotLineElement, Integer> indexes = indexesOf(typeToTokens.get(o1TypeOP.get()), o1, o2);
                Collection<Integer> o1PosInSequence = indexes.get(o1);
                Collection<Integer> o2PosInSequence = indexes.get(o2);

                Integer o1Index = o1PosInSequence.toArray(new Integer[o1PosInSequence.size()])[0];
                Integer o2Index = o2PosInSequence.toArray(new Integer[o2PosInSequence.size()])[0];

                result = Integer.compare(o1Index, o2Index);
            } else {
                Integer typeO1hierarchy = typesToHierarchy.get(o1TypeOP.get());
                Integer typeO2hierarchy = typesToHierarchy.get(o2TypeOP.get());

                result = Integer.compare(typeO1hierarchy, typeO2hierarchy);
                if (posComperatorResult != ECompareResult.EQUAL_TO.getValue()) {
                    if (isBothWithPositionSet(o1, o2)) {
                        result = posComperatorResult;
                    }
                }
            }
        } else if (!o1TypeOP.isPresent() && !o2TypeOP.isPresent()) {
            result = posComperatorResult;
        } else if (!o1TypeOP.isPresent()) {
            result = ECompareResult.LESS_THAN.getValue();
        } else if (!o2TypeOP.isPresent()) {
            result = ECompareResult.GREATER_THAN.getValue();
        }

        return result;
    }

    private boolean isBothWithPositionSet(final IRobotLineElement o1, final IRobotLineElement o2) {
        return !o1.getFilePosition().isNotSet() && !o2.getFilePosition().isNotSet();
    }

    public List<RobotToken> getTokensInElement() {
        final List<RobotToken> tokens = new ArrayList<>(0);

        for (final IRobotLineElement rle : typeToTokens.values()) {
            if (rle instanceof RobotToken) {
                tokens.add((RobotToken) rle);
            }
        }

        return tokens;
    }
}
