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
import org.nuxeo.ecm.core.api.thumbnail.ThumbnailService;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;

@RunWith(FeaturesRunner.class)
@Features({TestFeature.class})
public class TestGLB2ThumbnailFactory {

    @Inject
    protected ThumbnailService thumbnailService;

    @Inject
    CoreSession session;

    @Test
    public void testThumbnailFactory() {
        File file = new File(getClass().getResource("/files/scene.glb").getPath());
        Blob blob = new FileBlob(file,"model/gltf-binary");
        DocumentModel doc = session.createDocumentModel(session.getRootDocument().getPathAsString(),"File","File");
        doc.addFacet("GLB");
        doc.setPropertyValue("file:content", (Serializable) blob);
        Blob thumbnailBlob = thumbnailService.computeThumbnail(doc,session);
        Assert.assertNull(thumbnailBlob);
    }

}
