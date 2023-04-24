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
import org.nuxeo.ecm.core.api.thumbnail.ThumbnailAdapter;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.labs.glb.adapter.GLBModelAdapter;
import org.nuxeo.labs.glb.adapter.GLBRendition;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.List;

import static org.nuxeo.labs.glb.worker.GLBConversionWork.OPTIMIZED_RENDITION_NAME;

@RunWith(FeaturesRunner.class)
@Features({PlatformFeature.class})
@Deploy({
        "org.nuxeo.labs.glb.nuxeo-glb-preview-core",
        "org.nuxeo.ecm.platform.thumbnail"
})
public class TestGLBConversionWorker {

    @Inject
    protected TransactionalFeature transactionalFeature;

    @Inject
    CoreSession session;

    @Test
    public void testWorker() {
        File file = new File(getClass().getResource("/files/scene.glb").getPath());
        Blob blob = new FileBlob(file, "model/gltf-binary");
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(), "File", "File");
        doc.addFacet("GLB");
        doc.setPropertyValue("file:content", (Serializable) blob);

        doc = session.createDocument(doc);

        // wait for async work
        transactionalFeature.nextTransaction();

        doc = session.getDocument(doc.getRef());

        GLBModelAdapter adapter = doc.getAdapter(GLBModelAdapter.class);

        List<GLBRendition> renditions = adapter.getRenditions();
        Assert.assertEquals(1, renditions.size());

        GLBRendition rendition = adapter.getRendition(OPTIMIZED_RENDITION_NAME);
        Assert.assertNotNull(rendition);
        Assert.assertEquals(OPTIMIZED_RENDITION_NAME, rendition.getName());
        Blob optimized = rendition.getContent();
        Assert.assertTrue(optimized.getLength() > 0);
        Assert.assertEquals("model/gltf-binary",optimized.getMimeType());

        ThumbnailAdapter thumbnailAdapter = doc.getAdapter(ThumbnailAdapter.class);
        Blob thumbnail = thumbnailAdapter.getThumbnail(session);
        Assert.assertNotNull(thumbnail);
        Assert.assertTrue(thumbnail.getLength() > 0);
        Assert.assertEquals("image/png",thumbnail.getMimeType());
    }

}
