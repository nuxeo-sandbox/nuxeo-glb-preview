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

package org.nuxeo.labs.glb;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.labs.glb.adapter.GLBModelAdapter;
import org.nuxeo.labs.glb.adapter.GLBRendition;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@RunWith(FeaturesRunner.class)
@Features({TestFeature.class})
public class TestGLBModelAdapter {

    @Inject
    CoreSession session;

    @Test
    public void testGetAdapter() {
        DocumentModel doc = getModel();
        GLBModelAdapter adapter = doc.getAdapter(GLBModelAdapter.class);
        Assert.assertNotNull(adapter);
    }

    @Test
    public void testGetRenditions() {
        DocumentModel doc = getModel();
        GLBRendition rendition = new GLBRendition("test",new StringBlob(""));
        doc.setPropertyValue("glb:renditions", (Serializable) List.of(rendition.toMap()));
        GLBModelAdapter adapter = doc.getAdapter(GLBModelAdapter.class);
        Assert.assertEquals(1,adapter.getRenditions().size());
        Assert.assertNotNull(adapter.getRendition("test"));
    }

    @Test
    public void testSetRendition() {
        DocumentModel doc = getModel();
        GLBRendition rendition = new GLBRendition("test",new StringBlob(""));
        GLBModelAdapter adapter = doc.getAdapter(GLBModelAdapter.class);
        adapter.addRendition(rendition);
        List<Map<String,Serializable>> renditions = (List<Map<String, Serializable>>) doc.getPropertyValue("glb:renditions");
        Assert.assertEquals(1,renditions.size());
    }

    protected DocumentModel getModel() {
        DocumentModel doc = session.createDocumentModel(
                session.getRootDocument().getPathAsString(), "File", "File");
        doc.addFacet("GLB");
        return doc;
    }

}
