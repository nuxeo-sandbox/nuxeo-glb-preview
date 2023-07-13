/*
 * (C) Copyright 2023 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Michael Vachette
 */

package org.nuxeo.labs.glb.adapter;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GLBModelAdapter {

    public static final String GLB_RENDITIONS_PROP = "glb:renditions";
    public static final String GLB_RENDER_VIEWS_PROP = "picture:views";
    public static final String GLB_THUMBNAIL_PROP = "glb:thumbnail";

    protected final DocumentModel doc;

    public GLBModelAdapter(DocumentModel doc) {
        this.doc = doc;
    }

    public void clear() {
        doc.setPropertyValue(GLB_RENDITIONS_PROP, null);
        doc.setPropertyValue(GLB_RENDER_VIEWS_PROP, null);
        doc.setPropertyValue(GLB_THUMBNAIL_PROP, null);
    }

    public List<GLBRendition> getRenditions() {
        List<Map<String, Serializable>> renditions = getStoredRenditions();
        return renditions.stream().map(GLBRendition::new).collect(Collectors.toList());
    }

    public GLBRendition getRendition(String name) {
        List<Map<String, Serializable>> renditions = getStoredRenditions();
        Optional<GLBRendition> result =  renditions.stream().map(GLBRendition::new).filter(entry -> (entry.name != null && entry.name.equals(name))).findFirst();
        return result.orElse(null);
    }

    public void addRendition(GLBRendition rendition) {
        List<GLBRendition> renditions = getRenditions();
        //remove rendition if it already exists
        renditions = renditions.stream().filter(item -> !(rendition.getName().equals(item.getName()))).collect(Collectors.toList());
        renditions.add(rendition);
        saveRenditions(renditions);
    }

    public void removeRendition(String name) {
        List<GLBRendition> renditions = getRenditions();
        List<GLBRendition> filteredRenditions = renditions.stream().filter(rendition -> !(name.equals(rendition.getName()))).collect(Collectors.toList());
        if(renditions.size() != filteredRenditions.size()) {
            saveRenditions(filteredRenditions);
        }
    }

    public Blob getThumbnail() {
        return (Blob) doc.getPropertyValue(GLB_THUMBNAIL_PROP);
    }

    public void setThumbnail(Blob blob) {
        doc.setPropertyValue(GLB_THUMBNAIL_PROP, (Serializable) blob);
    }

    public List<GLBRenderView> getRenderViews() {
        List<Map<String, Serializable>> renders = getStoredRenderViews();
        return renders.stream().map(GLBRenderView::new).collect(Collectors.toList());
    }

    public GLBRenderView getRenderView(String name) {
        List<Map<String, Serializable>> renders = getStoredRenderViews();
        Optional<GLBRenderView> result =   renders.stream().map(GLBRenderView::new).filter(entry -> (entry.name != null && entry.name.equals(name))).findFirst();
        return result.orElse(null);
    }

    public void addRenderView(GLBRenderView renderView) {
        List<GLBRenderView> renderViews = getRenderViews();
        //remove the view if it already exists
        renderViews= renderViews.stream().filter(item -> !(renderView.getName().equals(item.getName()))).collect(Collectors.toList());
        renderViews.add(renderView);
        saveRenderViews(renderViews);
    }

    public void removeRenderView(String name) {
        List<GLBRenderView> renderViews = getRenderViews();
        List<GLBRenderView> filteredRenditions = renderViews.stream().filter(rendition -> !(name.equals(rendition.getName()))).collect(Collectors.toList());
        if(renderViews.size() != filteredRenditions.size()) {
            saveRenderViews(filteredRenditions);
        }
    }

    protected List<Map<String, Serializable>> getStoredRenditions() {
        return (List<Map<String, Serializable>>) doc.getPropertyValue(GLB_RENDITIONS_PROP);
    }

    public void saveRenditions(List<GLBRendition> renditions) {
        List<Map<String, Serializable>> renditionsToStore =  renditions.stream().map(GLBRendition::toMap).collect(Collectors.toList());
        doc.setPropertyValue(GLB_RENDITIONS_PROP, (Serializable) renditionsToStore);
    }

    protected List<Map<String, Serializable>> getStoredRenderViews() {
        return (List<Map<String, Serializable>>) doc.getPropertyValue(GLB_RENDER_VIEWS_PROP);
    }

    public void saveRenderViews(List<GLBRenderView> renderViews) {
        List<Map<String, Serializable>> rendersToStore =  renderViews.stream().map(GLBRenderView::toMap).collect(Collectors.toList());
        doc.setPropertyValue(GLB_RENDER_VIEWS_PROP, (Serializable) rendersToStore);
    }

}
