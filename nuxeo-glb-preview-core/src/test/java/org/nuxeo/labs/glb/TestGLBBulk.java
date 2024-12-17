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
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.labs.glb.adapter.GLBModelAdapter;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import jakarta.inject.Inject;
import java.io.File;
import java.io.Serializable;

import static org.nuxeo.labs.glb.bulk.RecomputeGlbPreviewAction.ACTION_NAME;
import static org.nuxeo.labs.glb.bulk.RecomputeGlbPreviewAction.PARAM_XPATH;

@RunWith(FeaturesRunner.class)
@Features({TestFeature.class})
@Deploy({
        "org.nuxeo.labs.glb.nuxeo-glb-preview-core:disable-glb-listener-contrib.xml"
})
public class TestGLBBulk {

    @Inject
    protected TransactionalFeature transactionalFeature;

    @Inject
    protected BulkService bulkService;

    @Inject
    CoreSession session;

    @Test
    public void testRecomputeAction() {
        File file = new File(getClass().getResource("/files/scene.glb").getPath());
        Blob blob = new FileBlob(file, "model/gltf-binary");
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "File", "File");
        doc.addFacet("GLB");
        doc.setPropertyValue("file:content", (Serializable) blob);

        doc = session.createDocument(doc);

        // wait for async work
        transactionalFeature.nextTransaction();

        final String query = String.format("Select * From Document Where ecm:mixinType = 'GLB' AND ecm:uuid ='%s'",doc.getId());
        String commandId = bulkService.submit(new BulkCommand.Builder(ACTION_NAME, query, session.getPrincipal().getName()).param(PARAM_XPATH, "file:content").build());

        // wait for async work
        transactionalFeature.nextTransaction();

        Assert.assertEquals(BulkStatus.State.COMPLETED, bulkService.getStatus(commandId).getState());

        doc = session.getDocument(doc.getRef());
        GLBModelAdapter adapter = doc.getAdapter(GLBModelAdapter.class);
        Assert.assertEquals(1, adapter.getRenditions().size());
        Assert.assertEquals(7, adapter.getRenderViews().size());
        Assert.assertNotNull(adapter.getThumbnail());

    }

}
