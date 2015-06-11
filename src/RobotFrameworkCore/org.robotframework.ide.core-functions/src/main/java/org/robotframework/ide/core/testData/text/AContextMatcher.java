package org.robotframework.ide.core.testData.text;

import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public abstract class AContextMatcher implements
        Callable<List<RobotTokenContext>> {

    protected final TokenizatorOutput tokenProvider;


    public AContextMatcher(final TokenizatorOutput tokenProvider) {
        this.tokenProvider = tokenProvider;
    }


    protected abstract List<RobotTokenContext> findContexts(
            final TokenizatorOutput tokenProvider) throws Exception;


    @Override
    public List<RobotTokenContext> call() throws Exception {
        List<RobotTokenContext> placesOfContext = findContexts(tokenProvider);

        if (placesOfContext == null) {
            placesOfContext = new LinkedList<>();
        }

        return placesOfContext;
    }


    protected List<List<Integer>> prepareListOfCombinations(
            TokenizatorOutput tokenProvider,
            List<List<RobotTokenType>> combinationOfWordsToGet)
            throws ConcurrentModificationException, InterruptedException,
            ExecutionException {
        List<List<Integer>> possibleCombinations = new LinkedList<>();

        for (List<RobotTokenType> rtts : combinationOfWordsToGet) {
            List<List<Integer>> possibility = generatePossibleCombinations(
                    tokenProvider, rtts);
            possibleCombinations.addAll(possibility);
            possibility.clear();
            possibility = null;
        }

        Collections.sort(possibleCombinations, new ListInListComparator());

        return possibleCombinations;
    }


    private List<List<Integer>> generatePossibleCombinations(
            TokenizatorOutput tokenProvider, List<RobotTokenType> combination) {
        /*
         * The idea how it works is very simple, we are getting list of expected
         * type of elements to appear in pattern. We are go one-by-one over this
         * {@code RobotTokenType} pattern elements with retrieve all occurances
         * of single element. Next we extend current combination list by adding
         * to each currently available element each element position of current
         * token. It means that in example if we have currently in combination
         * list 2 elements after extend it by list of 3 elements we will have 2
         * time 3 elements - 6 exactly.
         */
        List<List<Integer>> allPossibleCombinations = new LinkedList<>();

        Map<RobotTokenType, List<Integer>> indexesOfSpecial = tokenProvider
                .getIndexesOfSpecial();
        for (RobotTokenType currentCombinationItem : combination) {
            List<Integer> elements = indexesOfSpecial
                    .get(currentCombinationItem);
            if (elements != null && !elements.isEmpty()) {
                if (allPossibleCombinations.isEmpty()) {
                    for (Integer element : elements) {
                        List<Integer> current = new LinkedList<>();
                        current.add(element);
                        allPossibleCombinations.add(current);
                    }
                } else {
                    mergeAllOccurancyOfCurrentElementWith(
                            allPossibleCombinations, elements);
                }
            }
        }

        return allPossibleCombinations;
    }


    private void mergeAllOccurancyOfCurrentElementWith(
            List<List<Integer>> allPossibleCombinations,
            List<Integer> occurancyOfCurrentElement) {
        int currentNumberOfCombinations = allPossibleCombinations.size();
        int numberOfElementsInThisItem = occurancyOfCurrentElement.size();
        for (int i = 0; i < currentNumberOfCombinations; i++) {
            int startPoint = i * numberOfElementsInThisItem;
            List<Integer> elementToPopulate = allPossibleCombinations
                    .get(startPoint);
            for (int j = 0; j < numberOfElementsInThisItem; j++) {
                List<Integer> withNewElement = new LinkedList<>(
                        elementToPopulate);
                withNewElement.add(occurancyOfCurrentElement.get(j));
                allPossibleCombinations.add(startPoint, withNewElement);
            }

            allPossibleCombinations.remove(startPoint
                    + numberOfElementsInThisItem);
        }
    }

    private final class ListInListComparator implements
            Comparator<List<Integer>> {

        @Override
        public int compare(List<Integer> o1, List<Integer> o2) {
            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;

            int result = EQUAL;
            if (o1 == null && o2 != null) {
                result = BEFORE;
            } else if (o1 != null && o2 == null) {
                result = AFTER;
            } else if (o1 == o2) {
                result = EQUAL;
            } else if (o1 != null && o2 != null) {
                Collections.sort(o1);
                Collections.sort(o2);

                int o1Size = o1.size();
                int o2Size = o2.size();

                if (o1.isEmpty() && o2.isEmpty()) {
                    result = EQUAL;
                } else if (o1.isEmpty()) {
                    result = BEFORE;
                } else if (o2.isEmpty()) {
                    result = AFTER;
                } else {
                    int sizeToUse = Math.min(o1Size, o2Size);
                    for (int i = 0; i < sizeToUse; i++) {
                        int compareResult = o1.get(i).compareTo(o2.get(i));
                        if (compareResult != EQUAL) {
                            result = compareResult;
                            break;
                        }
                    }

                    if (result == EQUAL) {
                        result = Integer.compare(o1Size, o2Size);
                    }
                }
            }

            return result;
        }
    }
}
