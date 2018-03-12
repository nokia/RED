/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class MarkerLabelDecorator implements ILightweightLabelDecorator, IResourceChangeListener {

    private final List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();

    private final List<IMarker> markersCache = new ArrayList<IMarker>();

    private static final ImageDescriptor ERROR = RedImages.getErrorImage();

    private static final ImageDescriptor WARNING = RedImages.getWarningImage();

    public MarkerLabelDecorator() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }

    @Override
    public void decorate(final Object element, final IDecoration decoration) {
        if (element instanceof IResource) {
            final IResource resource = (IResource) element;

            if (resource.getProject().isOpen()) {
                refreshMarkersCache(resource);
                if (markersCache.isEmpty()) {
                    return;
                }
                final ImageDescriptor overlay = getOverlay(resource);
                if (overlay != null) {
                    decoration.addOverlay(overlay);
                }
            }
        }
    }

    private ImageDescriptor getOverlay(final IResource resource) {
        if (resource instanceof IProject) {
            return determineOverlay();
        } else if (resource instanceof IFolder) {
            return determineOverlay((IFolder) resource);
        } else if (resource instanceof IFile) {
            return determineOverlay((IFile) resource);
        }
        return null;
    }

    private ImageDescriptor determineOverlay() {
        for (final IMarker marker : markersCache) {
            if (isMarkerOfSeverity(marker, IMarker.SEVERITY_ERROR)) {
                return ERROR;
            }
        }
        return WARNING;
    }

    private ImageDescriptor determineOverlay(final IFolder folder) {
        boolean hasWarning = false;
        for (final IMarker marker : markersCache) {
            if (isDescendentOf(marker, folder)) {
                if (isMarkerOfSeverity(marker, IMarker.SEVERITY_ERROR)) {
                    return ERROR;
                }
                hasWarning |= isMarkerOfSeverity(marker, IMarker.SEVERITY_WARNING);
            }
        }
        return hasWarning ? WARNING : null;
    }

    private ImageDescriptor determineOverlay(final IFile file) {
        boolean hasWarning = false;
        for (final IMarker marker : markersCache) {
            if (marker.getResource().equals(file)) {
                if (isMarkerOfSeverity(marker, IMarker.SEVERITY_ERROR)) {
                    return ERROR;
                }
                hasWarning |= isMarkerOfSeverity(marker, IMarker.SEVERITY_WARNING);
            }
        }
        return hasWarning ? WARNING : null;
    }

    private void refreshMarkersCache(final IResource resource) {
        try {
            if (!resource.exists()) { // resource does not exists anymore
                return;
            }
            final IMarker[] markers = resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
            markersCache.clear();

            for (final IMarker marker : markers) {
                markersCache.add(marker);
            }
        } catch (final CoreException e) {
            RedPlugin.logError("Unable to refresh markers for resource " + resource.getFullPath(), e);
        }
    }

    @Override
    public void addListener(final ILabelProviderListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        listeners.clear();
    }

    @Override
    public boolean isLabelProperty(final Object element, final String property) {
        return false;
    }

    @Override
    public void removeListener(final ILabelProviderListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        final IResourceDelta delta = event.getDelta();
        if (delta != null) {
            try {
                final CollectAllResourcesVisitor visitor = new CollectAllResourcesVisitor();
                delta.accept(visitor);
                final List<IResource> list = visitor.getResources();
                final LabelProviderChangedEvent labelProviderChangedEvent = new LabelProviderChangedEvent(this,
                        list.toArray());
                for (final ILabelProviderListener listener : listeners) {
                    listener.labelProviderChanged(labelProviderChangedEvent);
                }
            } catch (final CoreException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private boolean isMarkerOfSeverity(final IMarker marker, final int severity) {
        try {
            final Integer result = (Integer) marker.getAttribute(IMarker.SEVERITY);
            return result != null && result.intValue() == severity;
        } catch (final CoreException e) {
            return false;
        }
    }

    private boolean isDescendentOf(final IMarker marker, final IFolder folder) {
        final IResource resource = marker.getResource();
        if (resource != null && resource.exists()) {
            final IPath resourcePath = resource.getLocation();
            final IPath folderPath = folder.getLocation();
            return folderPath.isPrefixOf(resourcePath);
        }
        return false;
    }

    private static class CollectAllResourcesVisitor implements IResourceDeltaVisitor {

        private final List<IResource> resources = new ArrayList<IResource>();

        @Override
        public boolean visit(final IResourceDelta delta) {
            resources.add(delta.getResource());
            return true;
        }

        public List<IResource> getResources() {
            return resources;
        }
    }
}
