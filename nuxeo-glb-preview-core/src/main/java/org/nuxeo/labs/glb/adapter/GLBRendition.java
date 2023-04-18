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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GLBRendition {

    public static final String NAME_PROPERTY = "name";
    public static final String CONTENT_PROPERTY = "content";
    protected String name;
    protected Blob content;

    public GLBRendition(String name, Blob content) {
        this.name = name;
        this.content = content;
    }

    public GLBRendition(Map<String, Serializable> map) {
        this.name = (String) map.get(NAME_PROPERTY);
        this.content = (Blob) map.get(CONTENT_PROPERTY);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Blob getContent() {
        return content;
    }

    public void setContent(Blob content) {
        this.content = content;
    }

    public Map<String, Serializable> toMap() {
        Map<String, Serializable> map = new HashMap<>();
        map.put(NAME_PROPERTY, name);
        map.put(CONTENT_PROPERTY, (Serializable) content);
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GLBRendition)) return false;
        GLBRendition that = (GLBRendition) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getContent(), that.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getContent());
    }
}

