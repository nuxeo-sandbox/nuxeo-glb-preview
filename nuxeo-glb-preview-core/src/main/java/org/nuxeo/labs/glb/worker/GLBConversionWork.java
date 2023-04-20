/*
 * (C) Copyright 2022 Nuxeo (http://nuxeo.com/) and others.
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

package org.nuxeo.labs.glb.worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.labs.glb.adapter.GLBModelAdapter;
import org.nuxeo.labs.glb.adapter.GLBRendition;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;
import java.util.HashMap;

import static org.nuxeo.ecm.core.api.CoreSession.ALLOW_VERSION_WRITE;
import static org.nuxeo.ecm.core.api.versioning.VersioningService.DISABLE_AUTO_CHECKOUT;

public class GLBConversionWork extends AbstractWork {

    public static final String GLB_CONVERSION_DONE_EVENT = "GLBConversionDone";

    private static final Logger log = LogManager.getLogger(GLBConversionWork.class);
    public static final String OPTIMIZED_RENDITION_NAME = "optimized";

    protected static String computeIdPrefix(String repositoryName, String docId) {
        return repositoryName + ':' + docId + ":glbconversion:";
    }

    public GLBConversionWork(String repositoryName, String docId) {
        super(computeIdPrefix(repositoryName, docId));
        setDocument(repositoryName, docId);
    }

    @Override
    public void work() {
        setProgress(Progress.PROGRESS_INDETERMINATE);
        openSystemSession();

        // get GLB blob
        DocumentModel doc = session.getDocument(new IdRef(docId));
        BlobHolder blobHolder = doc.getAdapter(BlobHolder.class);
        Blob glb = blobHolder.getBlob();

        // update previews
        setStatus("Updating previews");
        boolean save = computePreviews(doc, glb);

        if (save) {
            // save document
            if (doc.isVersion()) {
                doc.putContextData(ALLOW_VERSION_WRITE, Boolean.TRUE);
            }
            doc.putContextData(DISABLE_AUTO_CHECKOUT, Boolean.TRUE);
            session.saveDocument(doc);
        }

        setStatus("Done");
        fireGLBConversionDoneEvent();
    }

    @Override
    public String getTitle() {
        return "GLB Conversions: " + getId();
    }

    protected boolean computePreviews(DocumentModel doc, Blob blob) {
        HashMap<String, Serializable> params = new HashMap<>();
        params.put("targetFileName",blob.getFilename());
        ConversionService cs = Framework.getService(ConversionService.class);
        BlobHolder result = cs.convert("glbOptimizer", new SimpleBlobHolder(blob),params);

        GLBRendition rendition = new GLBRendition(OPTIMIZED_RENDITION_NAME,result.getBlob());
        GLBModelAdapter adapter = doc.getAdapter(GLBModelAdapter.class);
        adapter.addRendition(rendition);
        return true;
    }

    protected void fireGLBConversionDoneEvent() {
        DocumentModel doc = session.getDocument(new IdRef(docId));
        DocumentEventContext ctx = new DocumentEventContext(session, session.getPrincipal(), doc);
        Event event = ctx.newEvent(GLB_CONVERSION_DONE_EVENT);
        Framework.getService(EventService.class).fireEvent(event);
    }

}