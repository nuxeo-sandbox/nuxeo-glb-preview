/*
 * (C) Copyright 2021 Nuxeo (http://nuxeo.com/) and others.
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
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.EventListenerDescriptor;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.labs.glb.listener.GLBChangedListener;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import jakarta.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(FeaturesRunner.class)
@Features({TestFeature.class})
public class TestGLBConversionListener {

    @Inject
    protected EventService s;

    @Inject
    protected WorkManager workManager;

    @Inject
    protected TransactionalFeature transactionalFeature;

    @Inject
    CoreSession session;

    protected final List<String> events = Arrays.asList("documentCreated", "beforeDocumentModification");

    @Test
    public void listenerRegistration() {
        EventListenerDescriptor listener = s.getEventListener("GLBChangedListener");
        assertNotNull(listener);
        assertTrue(events.stream().allMatch(listener::acceptEvent));
        Assert.assertEquals(listener.asEventListener().getClass().getName(), GLBChangedListener.class.getName());
    }

    @Test
    public void testCreateWithBlob() {
        int completed = workManager.getMetrics("GLB").completed.intValue();

        File file = new File(getClass().getResource("/files/scene.glb").getPath());
        Blob blob = new FileBlob(file, "model/gltf-binary");
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "File", "File");
        doc.addFacet("GLB");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.createDocument(doc);
        transactionalFeature.nextTransaction();

        Assert.assertEquals(completed+ 1,workManager.getMetrics("GLB").completed.intValue());
    }

    @Test
    public void testCreateWithoutBlob() {
        int completed = workManager.getMetrics("GLB").completed.intValue();

        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "File", "File");
        doc.addFacet("GLB");
        doc = session.createDocument(doc);

        transactionalFeature.nextTransaction();
        doc = session.getDocument(doc.getRef());

        Assert.assertEquals(0,workManager.getMetrics("GLB").scheduled.intValue());

        File file = new File(getClass().getResource("/files/scene.glb").getPath());
        Blob blob = new FileBlob(file, "model/gltf-binary");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.saveDocument(doc);
        transactionalFeature.nextTransaction();

        Assert.assertEquals(completed+ 1,workManager.getMetrics("GLB").completed.intValue());
    }

}
