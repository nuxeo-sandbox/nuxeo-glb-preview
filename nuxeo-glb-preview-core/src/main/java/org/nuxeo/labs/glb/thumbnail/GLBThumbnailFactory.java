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

package org.nuxeo.labs.glb.thumbnail;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.thumbnail.factories.ThumbnailDocumentFactory;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;
import java.util.HashMap;

public class GLBThumbnailFactory extends ThumbnailDocumentFactory {

    @Override
    public Blob computeThumbnail(DocumentModel documentModel, CoreSession coreSession) {
        Blob glbBlob = (Blob) documentModel.getPropertyValue("file:content");
        HashMap<String, Serializable> params = new HashMap<>();
        params.put("targetFileName","thumbnail.png");
        ConversionService cs = Framework.getService(ConversionService.class);
        BlobHolder result = cs.convert("glb2png", new SimpleBlobHolder(glbBlob),params);
        return result.getBlob();
    }
}
