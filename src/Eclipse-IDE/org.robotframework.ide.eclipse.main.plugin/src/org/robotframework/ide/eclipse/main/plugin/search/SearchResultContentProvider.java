/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.text.Match;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.viewers.TreeContentProvider;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;

/**
 * @author Michal Anglart
 */
class SearchResultContentProvider extends TreeContentProvider {

    private SearchResult input = null;

    private RobotModel model;

    @Override
    public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        input = (SearchResult) newInput;
        model = input == null ? null : ((SearchQuery) input.getQuery()).getModel();
    }


    @Override
    public Object[] getElements(final Object inputElement) {
        final SearchResult result = (SearchResult) inputElement;

        final Set<IProject> projects = new HashSet<>();
        for (final Object matchingElement : result.getElements()) {
            if (matchingElement instanceof IFile) {
                projects.add(((IFile) matchingElement).getProject());

            } else if (matchingElement instanceof IProject) {
                projects.add((IProject) matchingElement);

            } else if (matchingElement instanceof MatchesGroupingElement) {
                ((MatchesGroupingElement) matchingElement).getGroupingObjectOf(IProject.class)
                        .ifPresent(p -> projects.add(p));
            }
        }
        return projects.toArray();
    }

    @Override
    public Object[] getChildren(final Object parentElement) {
        if (parentElement instanceof IContainer) {
            try {
                final List<Object> children = new ArrayList<>();
                if (parentElement instanceof IProject) {
                    if (libsShouldBeShown((IProject) parentElement)) {
                        children.add(new Libs((IProject) parentElement));
                    }
                }
                for (final IResource resource : ((IContainer) parentElement).members()) {
                    if (resourceShouldBeShown(resource)) {
                        children.add(resource);
                    }
                }
                return children.toArray();
            } catch (final CoreException e) {
                RedPlugin.logError("Problem when displaying search results", e);
            }
        } else if (parentElement instanceof IFile) {
            final List<Match> children = new ArrayList<>();

            final Match[] matches = input.getMatches(parentElement);
            children.addAll(newArrayList(matches));
            return children.toArray();

        } else if (parentElement instanceof Libs) {
            final Set<Object> children = new LinkedHashSet<>();
            
            final Libs parent = (Libs) parentElement;
            final IProject project = parent.getParent();
            final RobotProject robotProject = model.createRobotProject(project);
            
            for (final LibrarySpecification libSpec : robotProject.getLibrarySpecifications()) {
                final Match[] libMatches = input.getMatches(new MatchesGroupingElement(project, libSpec));
                if (libMatches.length > 0) {
                    children.add(new LibraryWithParent(parent, libSpec, newArrayList(libMatches)));
                } else {
                    for (final KeywordSpecification kwSpec : libSpec.getKeywords()) {
                        final Match[] kwMatches = input.getMatches(new MatchesGroupingElement(project, libSpec,
                                kwSpec));
                        if (kwMatches.length > 0) {
                            children.add(new LibraryWithParent(parent, libSpec, new ArrayList<Match>()));
                        }
                    }
                }
            }
            return children.toArray();

        } else if (parentElement instanceof LibraryWithParent) {
            final Set<Object> children = new LinkedHashSet<>();

            final LibraryWithParent parent = (LibraryWithParent) parentElement;
            final IProject project = parent.getParent().getParent();
            final LibrarySpecification libSpec = parent.getSpecification();

            children.addAll(parent.getMatches());
            for (final KeywordSpecification kwSpec : libSpec.getKeywords()) {
                final Match[] kwMatches = input.getMatches(new MatchesGroupingElement(project, libSpec, kwSpec));

                if (kwMatches.length > 0) {
                    children.add(new KeywordWithParent(parent, kwSpec, newArrayList(kwMatches)));
                }
            }
            return children.toArray();

        } else if (parentElement instanceof KeywordWithParent) {
            final Set<Object> children = new LinkedHashSet<>();
            final KeywordWithParent parent = (KeywordWithParent) parentElement;
            children.addAll(parent.getMatches());
            return children.toArray();
        }
        return null;
    }


    private boolean libsShouldBeShown(final IProject project) {
        final RobotProject robotProject = model.createRobotProject(project);
        for (final LibrarySpecification libSpec : robotProject.getLibrarySpecifications()) {
            if (input.containMatches(new MatchesGroupingElement(project, libSpec))) {
                return true;
            }
            for (final KeywordSpecification kwSpec : libSpec.getKeywords()) {
                if (input.containMatches(new MatchesGroupingElement(project, libSpec, kwSpec))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean resourceShouldBeShown(final IResource resource) throws CoreException {
        final AtomicBoolean shouldBeShown = new AtomicBoolean(false);
        resource.accept(r -> {
            if (r.getType() == IResource.FILE) {
                if (input.containMatches(r)) {
                    shouldBeShown.set(true);
                    return false;
                }
            }
            return true;
        });
        return shouldBeShown.get();
    }

    @Override
    public Object getParent(final Object element) {
        if (element instanceof IResource) {
            return ((IResource) element).getParent();
        } else if (element instanceof Libs) {
            return ((Libs) element).getParent();
        } else if (element instanceof LibraryWithParent) {
            return ((LibraryWithParent) element).getParent();
        } else if (element instanceof KeywordWithParent) {
            return ((KeywordWithParent) element).getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(final Object element) {
        return !(element instanceof Match);
    }


    static class Libs {

        private final IProject project;

        @VisibleForTesting
        Libs(final IProject project) {
            this.project = project;
        }

        private IProject getParent() {
            return project;
        }
    }

    private static abstract class LibraryEntity<P, S> {

        private final P parent;

        private final S specification;

        private final List<Match> matches;

        private LibraryEntity(final P parent, final S specification, final List<Match> matches) {
            this.parent = parent;
            this.specification = specification;
            this.matches = matches;
        }

        P getParent() {
            return parent;
        }

        S getSpecification() {
            return specification;
        }

        List<Match> getMatches() {
            return matches;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof LibraryEntity) {
                final LibraryEntity<?, ?> that = (LibraryEntity<?, ?>) obj;
                return this.parent == that.parent && this.specification.equals(that.specification);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(parent, specification);
        }

        StyledString getLabel() {
            final StyledString label = new StyledString(getName());
            if (!matches.isEmpty()) {
                label.append(" (" + matches.size() + " matches)", Stylers.Common.ECLIPSE_DECORATION_STYLER);
            }
            return label;
        }

        abstract String getName();
    }

    static final class LibraryWithParent extends LibraryEntity<Libs, LibrarySpecification> {

        @VisibleForTesting
        LibraryWithParent(final Libs parent, final LibrarySpecification specification,
                final List<Match> matches) {
            super(parent, specification, matches);
        }

        @Override
        String getName() {
            return getSpecification().getName();
        }
    }

    static final class KeywordWithParent extends LibraryEntity<LibraryWithParent, KeywordSpecification> {

        @VisibleForTesting
        KeywordWithParent(final LibraryWithParent parent, final KeywordSpecification specification,
                final List<Match> matches) {
            super(parent, specification, matches);
        }

        @Override
        String getName() {
            return getSpecification().getName();
        }
    }
}
