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

package org.nuxeo.labs.glb.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.labs.glb.adapter.GLBModelAdapter;
import org.nuxeo.labs.glb.worker.GLBConversionWork;
import org.nuxeo.runtime.api.Framework;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.BEFORE_DOC_UPDATE;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;

public class GLBChangedListener implements EventListener {

    private static final Logger log = LogManager.getLogger(GLBChangedListener.class);

    /** @since 11.1 **/
    public static final String DISABLE_GLB_CONVERSIONS_GENERATION_LISTENER = "disableGLBConversionsGenerationListener";

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();
        if (Boolean.TRUE.equals(ctx.getProperty(DISABLE_GLB_CONVERSIONS_GENERATION_LISTENER))) {
            log.trace("GLB conversions are disabled for document {}", doc::getId);
            return;
        }

        String eventName = event.getName();
        if (shouldProcess(doc, eventName)) {
            if (BEFORE_DOC_UPDATE.equals(eventName)) {
                resetProperties(doc);
            }
            if (doc.getPropertyValue("file:content") != null) {
                scheduleAsyncProcessing(doc);
            }
        }
    }

    protected boolean shouldProcess(DocumentModel doc, String eventName) {
        return doc.hasFacet("GLB") && !doc.isProxy()
                && (DOCUMENT_CREATED.equals(eventName) || doc.getProperty("file:content").isDirty());
    }

    protected void resetProperties(DocumentModel doc) {
        log.debug("Resetting GLB info of document {}", doc);
        GLBModelAdapter adapter = doc.getAdapter(GLBModelAdapter.class);
        adapter.clear();
    }

    protected void scheduleAsyncProcessing(DocumentModel doc) {
        WorkManager workManager = Framework.getService(WorkManager.class);
        GLBConversionWork work = new GLBConversionWork(doc.getRepositoryName(), doc.getId());
        log.debug("Scheduling work: glb conversion of document {}.", doc);
        workManager.schedule(work, true);
    }

}
