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

import org.nuxeo.ecm.core.api.DocumentModel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GLBModelAdapter {

    protected DocumentModel doc;

    public GLBModelAdapter(DocumentModel doc) {
        this.doc = doc;
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
        List<Map<String, Serializable>> renditions = getStoredRenditions();
        renditions.add(rendition.toMap());
        setStoredRenditions(renditions);
    }

    public void clearRenditions() {
        doc.setPropertyValue("glb:renditions", null);
    }

    protected List<Map<String, Serializable>> getStoredRenditions() {
        return (List<Map<String, Serializable>>) doc.getPropertyValue("glb:renditions");
    }

    protected void setStoredRenditions(List<Map<String, Serializable>> values) {
        doc.setPropertyValue("glb:renditions", (Serializable) values);
    }

}
