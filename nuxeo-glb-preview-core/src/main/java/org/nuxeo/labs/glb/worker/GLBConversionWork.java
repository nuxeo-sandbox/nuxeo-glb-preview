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
import org.nuxeo.ecm.core.api.CoreSession;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nuxeo.ecm.core.api.CoreSession.ALLOW_VERSION_WRITE;
import static org.nuxeo.ecm.core.api.versioning.VersioningService.DISABLE_AUTOMATIC_VERSIONING;
import static org.nuxeo.ecm.core.api.versioning.VersioningService.DISABLE_AUTO_CHECKOUT;
import static org.nuxeo.labs.glb.listener.GLBChangedListener.DISABLE_GLB_CONVERSIONS_GENERATION_LISTENER;

public class GLBConversionWork extends AbstractWork {

    public static final String GLB_CONVERSION_DONE_EVENT = "GLBConversionDone";

    private static final Logger log = LogManager.getLogger(GLBConversionWork.class);
    public static final String OPTIMIZED_RENDITION_NAME = "optimized";
    public static final String CATEGORY = "glbConversion";

    public static final String BASELINE_ENVIRONMENT = "environment-image=neutral&shadow-intensity=0&shadow-softness=0&exposure=0.92" +
            "&min-camera-orbit=-Infinity 0deg auto&max-camera-orbit=Infinity 180deg auto";

    protected static String computeIdPrefix(String repositoryName, String docId) {
        return repositoryName + ':' + docId + ":glbconversion:";
    }

    public GLBConversionWork(String repositoryName, String docId) {
        super(computeIdPrefix(repositoryName, docId));
        setDocument(repositoryName, docId);
    }

    @Override
    public String getCategory() {
        return CATEGORY;
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
        computePreviews(doc, glb);

        doSaveAndCommit(doc, false);

        setStatus("Done");
        fireGLBConversionDoneEvent();
    }

    @Override
    public int getRetryCount() {
        return 2;
    }

    @Override
    public String getTitle() {
        return "GLB Conversions: " + getId();
    }

    protected void computePreviews(DocumentModel doc, Blob blob) {
        //compute optimized rendition
        HashMap<String, Serializable> params = new HashMap<>();
        params.put("targetFileName",blob.getFilename());
        ConversionService cs = Framework.getService(ConversionService.class);
        BlobHolder OptimizationResult = cs.convert("glbOptimizer", new SimpleBlobHolder(blob),params);
        Blob optimizedBlob = OptimizationResult.getBlob();
        GLBRendition rendition = new GLBRendition(OPTIMIZED_RENDITION_NAME,optimizedBlob);
        GLBModelAdapter adapter = doc.getAdapter(GLBModelAdapter.class);
        adapter.addRendition(rendition);

        doSaveAndCommit(doc,true);

        //compute thumbnail
        Blob thumbnailBlob = doScreenshot(optimizedBlob, "480","480", BASELINE_ENVIRONMENT,"thumbnail.png");
        adapter.setThumbnail(thumbnailBlob);

        doSaveAndCommit(doc, true);

        //compute render views
        List<Map<String,Serializable>> views = new ArrayList<>();

        String[] viewNames = new String[]{"front","back","top","bottom","right","left","isometric"};
        String[] viewCoordinates = new String[]{
                "0deg 90deg 100%",
                "180deg 90deg 100%",
                "0deg 0deg 100%",
                "0deg 180deg 100%",
                "-90deg 90deg 100%",
                "90deg 90deg 100%",
                "45deg 45deg 100%"};

        for(int i=0; i<viewNames.length; i++) {
            String environment = String.format("%s&camera-orbit=%s", BASELINE_ENVIRONMENT,viewCoordinates[i]);
            Blob viewBlob = doScreenshot(optimizedBlob, "1080","1080", environment,
                    String.format("%s.png",viewNames[i]));
            Map<String,Serializable> view = new HashMap<>();
            view.put("title",viewNames[i]);
            view.put("content", (Serializable) viewBlob);
            views.add(view);
        }

        doc.setPropertyValue("picture:views", (Serializable) views);
    }

    protected void fireGLBConversionDoneEvent() {
        DocumentModel doc = session.getDocument(new IdRef(docId));
        DocumentEventContext ctx = new DocumentEventContext(session, session.getPrincipal(), doc);
        Event event = ctx.newEvent(GLB_CONVERSION_DONE_EVENT);
        Framework.getService(EventService.class).fireEvent(event);
    }

    protected Blob doScreenshot(Blob glbBlob, String width, String height, String environment, String outFilename) {
        HashMap<String, Serializable> params = new HashMap<>();
        params.put("targetFileName",outFilename);
        params.put("height", height);
        params.put("width", width);
        params.put("environment",environment);
        ConversionService cs = Framework.getService(ConversionService.class);
        BlobHolder result = cs.convert("glb2png", new SimpleBlobHolder(glbBlob),params);
        return result.getBlob();
    }

    protected void doSaveAndCommit(DocumentModel doc, boolean doCommit) {
        // save document
        if (doc.isVersion()) {
            doc.putContextData(ALLOW_VERSION_WRITE, Boolean.TRUE);
        }
        doc.putContextData(DISABLE_GLB_CONVERSIONS_GENERATION_LISTENER, Boolean.TRUE);
        doc.putContextData("disableNotificationService", Boolean.TRUE);
        doc.putContextData(CoreSession.DISABLE_AUDIT_LOGGER, Boolean.TRUE);
        doc.putContextData(DISABLE_AUTO_CHECKOUT, Boolean.TRUE);
        doc.putContextData(DISABLE_AUTOMATIC_VERSIONING, Boolean.TRUE);
        session.saveDocument(doc);
        if (doCommit) {
            commitOrRollbackTransaction();
            startTransaction();
        }
    }

}
