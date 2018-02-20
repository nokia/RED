/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.executor.RedSystemProperties;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.keywords.names.CamelCaseKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal.RedLibraryKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal.RedNotAccessibleLibraryKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal.RedUserKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal.VariableOrigin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.io.Files;

public class AssistProposals {

    static RedSectionProposal createSectionProposal(final String sectionName, final ProposalMatch match) {
        return new RedSectionProposal("*** " + sectionName + " ***", match);
    }

    static RedFileLocationProposal createFileLocationProposal(final String content, final IFile toFile,
            final ProposalMatch match) {
        return new RedFileLocationProposal(content, content, toFile, match);
    }

    static RedFileLocationProposal createFileLocationProposal(final IFile fromFile, final IFile toFile,
            final ProposalMatch match) {
        final String content;
        if (RedSystemProperties.isWindowsPlatform()
                && !fromFile.getLocation().getDevice().equals(toFile.getLocation().getDevice())) {
            content = toFile.getLocation().toString();
        } else {
            content = createCurrentFileRelativePath(fromFile, toFile);
        }
        return new RedFileLocationProposal(content, content, toFile, match);
    }

    private static String createCurrentFileRelativePath(final IFile from, final IFile to) {
        return to.getLocation().makeRelativeTo(from.getLocation()).removeFirstSegments(1).toString();
    }

    static RedLibraryProposal createLibraryProposal(final RobotSuiteFile suiteFile, final LibrarySpecification libSpec,
            final ProposalMatch match) {

        final String libraryName = libSpec.getName();
        final List<String> arguments = new ArrayList<>(libSpec.getDescriptor().getArguments());
        final boolean isImported = suiteFile.getImportedLibraries().containsKey(libSpec);
        return new RedLibraryProposal(libraryName, arguments, isImported, libSpec.getDocumentation(), match);
    }

    static RedKeywordProposal createNotAccessibleLibraryKeywordProposal(final LibrarySpecification spec,
            final KeywordSpecification keyword, final String bddPrefix, final KeywordScope scope,
            final Optional<String> alias, final IPath exposingFilepath,
            final Predicate<RedKeywordProposal> shouldUseQualified, final ProposalMatch match) {

        final ArgumentsDescriptor argsDescriptor = keyword.createArgumentsDescriptor();
        return new RedNotAccessibleLibraryKeywordProposal(spec.getName(), alias, scope, bddPrefix, keyword.getName(),
                argsDescriptor, keyword.getDocumentation(), keyword.isDeprecated(), exposingFilepath,
                shouldUseQualified, match);
    }

    static RedKeywordProposal createLibraryKeywordProposal(final LibrarySpecification spec,
            final KeywordSpecification keyword, final String bddPrefix, final KeywordScope scope,
            final Optional<String> alias, final IPath exposingFilepath,
            final Predicate<RedKeywordProposal> shouldUseQualified, final ProposalMatch match) {

        final ArgumentsDescriptor argsDescriptor = keyword.createArgumentsDescriptor();
        return new RedLibraryKeywordProposal(spec.getName(), alias, scope, bddPrefix,
                keyword.getName(), argsDescriptor, keyword.getDocumentation(), keyword.isDeprecated(), exposingFilepath,
                shouldUseQualified, match);
    }

    static RedKeywordProposal createUserKeywordProposal(final RobotKeywordDefinition userKeyword,
            final String bddPrefix, final KeywordScope scope, final Predicate<RedKeywordProposal> shouldUseQualified,
            final ProposalMatch match) {

        final RobotSuiteFile file = userKeyword.getSuiteFile();
        final ArgumentsDescriptor argsDescriptor = userKeyword.createArgumentsDescriptor();
        return new RedUserKeywordProposal(Files.getNameWithoutExtension(file.getFile().getName()), scope, bddPrefix,
                userKeyword.getName(), argsDescriptor, userKeyword.getDocumentation(), userKeyword.isDeprecated(),
                file.getFile().getFullPath(), shouldUseQualified, match);
    }

    static RedCodeReservedWordProposal createCodeReservedWordProposal(final String word, final ProposalMatch match) {
        return new RedCodeReservedWordProposal(word, match);
    }

    static RedWithNameProposal createWithNameProposal(final String word, final ProposalMatch match) {
        return new RedWithNameProposal(word, match);
    }

    static RedImportProposal createResourceImportInCodeProposal(final String nameToUse, final String bddPrefix,
            final ProposalMatch match) {
        return new RedImportProposal(nameToUse, bddPrefix, ModelType.RESOURCE_IMPORT_SETTING, match);
    }

    static RedImportProposal createLibraryImportInCodeProposal(final String nameToUse, final String bddPrefix,
            final ProposalMatch match) {
        return new RedImportProposal(nameToUse, bddPrefix, ModelType.LIBRARY_IMPORT_SETTING, match);
    }

    static RedSettingProposal createSettingProposal(final String settingName, final SettingTarget target,
            final ProposalMatch match) {
        return new RedSettingProposal(settingName, target, match);
    }

    static RedNewVariableProposal createNewVariableProposal(final VariableType type) {
        switch (type) {
            case SCALAR:
                return new RedNewVariableProposal("${newScalar}", type, new ArrayList<String>(),
                        RedImages.getRobotScalarVariableImage(), "Fresh scalar", "Creates fresh scalar variable");
            case LIST:
                return new RedNewVariableProposal("@{newList}", type, newArrayList("item"),
                        RedImages.getRobotListVariableImage(), "Fresh list", "Creates fresh list variable");
            case DICTIONARY:
                return new RedNewVariableProposal("&{newDict}", type, newArrayList("key=value"),
                        RedImages.getRobotDictionaryVariableImage(), "Fresh dictionary",
                        "Creates fresh dictionary variable");
            default:
                throw new IllegalStateException("Variable type not supported");
        }
    }

    static RedVariableProposal createUserVariableProposal(final RobotVariable robotVariable,
            final ProposalMatch match) {
        final String varName = robotVariable.getPrefix() + robotVariable.getName() + robotVariable.getSuffix();
        final IFile file = robotVariable.getSuiteFile().getFile();
        final String sourceName = file != null ? file.getFullPath().toString() : robotVariable.getSuiteFile().getName();
        final String value = robotVariable.getValue();
        final String comment = robotVariable.getComment();
        return new RedVariableProposal(varName, sourceName, value, comment, VariableOrigin.LOCAL, match);
    }

    static RedVariableProposal createLocalVariableProposal(final String name, final String path,
            final ProposalMatch match) {
        final String actualName = name.contains("}=") ? name.substring(0, name.indexOf("}=") + 1) : name;
        return new RedVariableProposal(actualName, path, "", "", VariableOrigin.LOCAL, match);
    }

    static RedVariableProposal createVarFileVariableProposal(final String name, final String value, final String path,
            final ProposalMatch match) {
        return new RedVariableProposal(name, path, value, "", VariableOrigin.IMPORTED, match);
    }

    static RedVariableProposal createBuiltInVariableProposal(final String name, final String value,
            final ProposalMatch match) {
        return new RedVariableProposal(name, "Built-in", value, "", VariableOrigin.BUILTIN, match);
    }

    public static Comparator<AssistProposal> sortedByLabels() {
        return new Comparator<AssistProposal>() {

            @Override
            public int compare(final AssistProposal proposal1, final AssistProposal proposal2) {
                return proposal1.getLabel().compareToIgnoreCase(proposal2.getLabel());
            }
        };
    }

    public static Comparator<AssistProposal> sortedByLabelsPrefixedFirst(final String prefix) {
        return new Comparator<AssistProposal>() {

            @Override
            public int compare(final AssistProposal proposal1, final AssistProposal proposal2) {
                final String lowerCasePrefix = prefix.toLowerCase();
                final boolean isPrefixed1 = proposal1.getLabel().toLowerCase().startsWith(lowerCasePrefix);
                final boolean isPrefixed2 = proposal2.getLabel().toLowerCase().startsWith(lowerCasePrefix);
                final int result = Boolean.compare(isPrefixed2, isPrefixed1);
                if (result != 0) {
                    return result;
                }
                return proposal1.getLabel().compareToIgnoreCase(proposal2.getLabel());
            }
        };
    }

    public static Comparator<RedKeywordProposal> sortedByLabelsCamelCaseAndPrefixedFirst(final String input) {
        return new Comparator<RedKeywordProposal>() {

            @Override
            public int compare(final RedKeywordProposal proposal1, final RedKeywordProposal proposal2) {
                final boolean isCamelCase1 = !CamelCaseKeywordNamesSupport.matches(proposal1.getLabel(), input)
                        .isEmpty();
                final boolean isCamelCase2 = !CamelCaseKeywordNamesSupport.matches(proposal2.getLabel(), input)
                        .isEmpty();
                final int result = Boolean.compare(isCamelCase2, isCamelCase1);
                if (result != 0) {
                    return result;
                }
                final Comparator<AssistProposal> prefixedComparator = sortedByLabelsPrefixedFirst(input);
                return prefixedComparator.compare(proposal1, proposal2);
            }
        };
    }

    public static Comparator<RedVariableProposal> sortedByTypesAndOrigin() {
        final List<Character> typesOrder = newArrayList('$', '@', '&');
        return new Comparator<RedVariableProposal>() {

            @Override
            public int compare(final RedVariableProposal proposal1, final RedVariableProposal proposal2) {
                final char type1 = proposal1.getContent().charAt(0);
                final char type2 = proposal2.getContent().charAt(0);

                if (type1 == type2) {
                    final boolean isBuiltIn1 = proposal1.getOrigin() == VariableOrigin.BUILTIN;
                    final boolean isBuiltIn2 = proposal2.getOrigin() == VariableOrigin.BUILTIN;
                    final int result = Boolean.compare(isBuiltIn1, isBuiltIn2);
                    if (result != 0) {
                        return result;
                    }
                    return proposal1.getLabel().compareToIgnoreCase(proposal2.getLabel());

                } else {
                    return Integer.compare(typesOrder.indexOf(type1), typesOrder.indexOf(type2));
                }
            }
        };
    }

    public static Comparator<RedLibraryProposal> sortedByLabelsNotImportedFirst() {
        return new Comparator<RedLibraryProposal>() {

            @Override
            public int compare(final RedLibraryProposal proposal1, final RedLibraryProposal proposal2) {
                final int result = Boolean.compare(proposal1.isImported(), proposal2.isImported());
                if (result != 0) {
                    return result;
                }
                return proposal1.getLabel().compareToIgnoreCase(proposal2.getLabel());
            }
        };
    }
}
