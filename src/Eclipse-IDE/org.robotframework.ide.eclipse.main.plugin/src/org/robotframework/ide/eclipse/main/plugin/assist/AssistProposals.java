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
import org.rf.ide.core.RedSystemProperties;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
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
        final String content = createRelativePathIfPossible(fromFile, toFile);
        return new RedFileLocationProposal(content, content, toFile, match);
    }

    static String createRelativePathIfPossible(final IFile fromFile, final IFile toFile) {
        final IPath toLocation = toFile.getLocation();
        final IPath fromLocation = fromFile.getLocation();
        return !RedSystemProperties.isWindowsPlatform() || fromLocation.getDevice().equals(toLocation.getDevice())
                ? toLocation.makeRelativeTo(fromLocation).removeFirstSegments(1).toString()
                : toLocation.toString();
    }

    static RedLibraryProposal createLibraryProposal(final RobotSuiteFile suiteFile, final LibrarySpecification libSpec,
            final ProposalMatch match) {

        final boolean isImported = suiteFile.getImportedLibraries().containsKey(libSpec);
        return new RedLibraryProposal(suiteFile.getRobotProject(), libSpec, isImported, match);
    }

    static RedSitePackagesLibraryProposal createSitePackagesLibraryProposal(final String name,
            final RobotSuiteFile suiteFile, final ProposalMatch match) {

        final boolean isImported = suiteFile.getImportedLibraries()
                .keys()
                .stream()
                .anyMatch(libSpec -> libSpec.getName().equals(name));
        return new RedSitePackagesLibraryProposal(name, isImported, match);
    }

    static RedKeywordProposal createNotAccessibleLibraryKeywordProposal(final LibrarySpecification spec,
            final KeywordSpecification keyword, final String bddPrefix, final KeywordScope scope,
            final Optional<String> alias, final IPath exposingFilepath,
            final Predicate<RedKeywordProposal> shouldUseQualified, final ProposalMatch match) {

        final ArgumentsDescriptor argsDescriptor = keyword.createArgumentsDescriptor();
        return new RedNotAccessibleLibraryKeywordProposal(spec.getName(), alias, scope, bddPrefix, keyword.getName(),
                argsDescriptor, () -> spec.createKeywordDocumentation(keyword.getName()), keyword.isDeprecated(),
                exposingFilepath, shouldUseQualified, match);
    }

    static RedKeywordProposal createLibraryKeywordProposal(final LibrarySpecification spec,
            final KeywordSpecification keyword, final String bddPrefix, final KeywordScope scope,
            final Optional<String> alias, final IPath exposingFilepath,
            final Predicate<RedKeywordProposal> shouldUseQualified, final ProposalMatch match) {

        final ArgumentsDescriptor argsDescriptor = keyword.createArgumentsDescriptor();
        return new RedLibraryKeywordProposal(spec.getName(), alias, scope, bddPrefix, keyword.getName(), argsDescriptor,
                () -> spec.createKeywordDocumentation(keyword.getName()), keyword.isDeprecated(), exposingFilepath,
                shouldUseQualified, match);
    }

    static RedKeywordProposal createUserKeywordProposal(final RobotKeywordDefinition userKeyword,
            final String bddPrefix, final KeywordScope scope, final Predicate<RedKeywordProposal> shouldUseQualified,
            final ProposalMatch match) {

        final RobotSuiteFile file = userKeyword.getSuiteFile();
        final ArgumentsDescriptor argsDescriptor = userKeyword.createArgumentsDescriptor();
        return new RedUserKeywordProposal(Files.getNameWithoutExtension(file.getFile().getName()), scope, bddPrefix,
                userKeyword.getName(), argsDescriptor, () -> userKeyword.createDocumentation(),
                userKeyword.isDeprecated(), file.getFile().getFullPath(), shouldUseQualified, match);
    }

    static GherkinReservedWordProposal createGherkinReservedWordProposal(final String word, final ProposalMatch match) {
        return new GherkinReservedWordProposal(word, match);
    }

    static ForLoopReservedWordProposal createForLoopReservedWordProposal(final String word, final ProposalMatch match,
            final boolean isDeprecated) {
        return new ForLoopReservedWordProposal(word, match, isDeprecated);
    }

    static LibraryAliasReservedWordProposal createLibraryAliasReservedWordProposal(final ProposalMatch match) {
        return new LibraryAliasReservedWordProposal(match);
    }

    static DisableSettingReservedWordProposal createDisableSettingReservedWordProposal(final ProposalMatch match) {
        return new DisableSettingReservedWordProposal(match);
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
        final String varName = robotVariable.getActualName();
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
        return (proposal1, proposal2) -> proposal1.getLabel().compareToIgnoreCase(proposal2.getLabel());
    }

    public static Comparator<AssistProposal> sortedByLabelsPrefixedFirst(final String prefix) {
        return (proposal1, proposal2) -> {
            final String lowerCasePrefix = prefix.toLowerCase();
            final boolean isPrefixed1 = proposal1.getLabel().toLowerCase().startsWith(lowerCasePrefix);
            final boolean isPrefixed2 = proposal2.getLabel().toLowerCase().startsWith(lowerCasePrefix);
            final int result = Boolean.compare(isPrefixed2, isPrefixed1);
            if (result != 0) {
                return result;
            }
            return sortedByLabels().compare(proposal1, proposal2);
        };
    }

    public static Comparator<RedKeywordProposal> sortedByLabelsCamelCaseAndPrefixedFirstWithDefaultScopeOrder(
            final String input, final IPath useplaceFilepath) {
        return (proposal1, proposal2) -> {
            final boolean isCamelCase1 = !CamelCaseKeywordNamesSupport.matches(proposal1.getLabel(), input).isEmpty();
            final boolean isCamelCase2 = !CamelCaseKeywordNamesSupport.matches(proposal2.getLabel(), input).isEmpty();
            final int result = Boolean.compare(isCamelCase2, isCamelCase1);
            if (result != 0) {
                return result;
            }

            if (proposal1.getKeywordName().equals(proposal2.getKeywordName())) {
                final List<KeywordScope> scopesOrder = KeywordScope.defaultOrder();
                final KeywordScope scope1 = proposal1.getScope(useplaceFilepath);
                final KeywordScope scope2 = proposal2.getScope(useplaceFilepath);
                final int scopeResult = Integer.compare(scopesOrder.indexOf(scope1), scopesOrder.indexOf(scope2));
                if (scopeResult != 0) {
                    return scopeResult;
                }
            }

            return sortedByLabelsPrefixedFirst(input).compare(proposal1, proposal2);
        };
    }

    public static Comparator<RedVariableProposal> sortedByTypesAndOrigin() {
        final List<Character> typesOrder = newArrayList('$', '@', '&');
        return (proposal1, proposal2) -> {
            final char type1 = proposal1.getContent().charAt(0);
            final char type2 = proposal2.getContent().charAt(0);

            if (type1 == type2) {
                final boolean isBuiltIn1 = proposal1.getOrigin() == VariableOrigin.BUILTIN;
                final boolean isBuiltIn2 = proposal2.getOrigin() == VariableOrigin.BUILTIN;
                final int result = Boolean.compare(isBuiltIn1, isBuiltIn2);
                if (result != 0) {
                    return result;
                }
                return sortedByLabels().compare(proposal1, proposal2);
            }

            return Integer.compare(typesOrder.indexOf(type1), typesOrder.indexOf(type2));
        };
    }

    public static Comparator<RedLibraryProposal> sortedByLabelsNotImportedFirst() {
        return (proposal1, proposal2) -> {
            final int result = Boolean.compare(proposal1.isImported(), proposal2.isImported());
            if (result != 0) {
                return result;
            }
            return sortedByLabels().compare(proposal1, proposal2);
        };
    }

    public static Comparator<RedSitePackagesLibraryProposal> sortedByLabelsNotImportedFirstForSitePackagesLibraries() {
        return (proposal1, proposal2) -> {
            final int result = Boolean.compare(proposal1.isImported(), proposal2.isImported());
            if (result != 0) {
                return result;
            }
            return sortedByLabels().compare(proposal1, proposal2);
        };
    }
}
