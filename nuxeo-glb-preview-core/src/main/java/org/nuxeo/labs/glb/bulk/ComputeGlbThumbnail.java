package org.nuxeo.labs.glb.bulk;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.bulk.action.computation.AbstractBulkComputation;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.lib.stream.computation.Topology;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.stream.StreamProcessorTopology;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nuxeo.ecm.core.bulk.BulkServiceImpl.STATUS_STREAM;
import static org.nuxeo.ecm.platform.thumbnail.ThumbnailConstants.THUMBNAIL_FACET;
import static org.nuxeo.lib.stream.computation.AbstractComputation.INPUT_1;
import static org.nuxeo.lib.stream.computation.AbstractComputation.OUTPUT_1;

public class ComputeGlbThumbnail implements StreamProcessorTopology {

    public static final String ACTION_NAME = "computeGlbThumbnail";

    public static final String ACTION_FULL_NAME = "bulk/" + ACTION_NAME;

    @Override
    public Topology getTopology(Map<String, String> map) {
        return Topology.builder()
                .addComputation(ComputeGlbThumbnailComputation::new,
                        Arrays.asList(INPUT_1 + ":" + ACTION_FULL_NAME, OUTPUT_1 + ":" + STATUS_STREAM))
                .build();
    }

    public static class ComputeGlbThumbnailComputation extends AbstractBulkComputation {

        public ComputeGlbThumbnailComputation() {
            super(ACTION_FULL_NAME);
        }

        @Override
        protected void compute(CoreSession session, List<String> ids, Map<String, Serializable> properties) {
            //Should you expect any error during this action, be sure to catch it to avoid blocking the stream. For instance:
            // - a document could have been deleted
            // - you might not have the write permission
            // - if you call a third part service, always set timeout and catch possible errors

            // You can report errors to the bulk command status using:
            // delta.inError(numberOfErrorInTheBatch, someErrorMessage);

            // Note that in case of failure (uncatched exception) the retry mechanism (stream processor policy) at the computation level will operate
            // but this can create duplicate processing, for this reason, it is always better if the processing can be idempotent.
            // Furthermore, if the failure is systematic the bulk command will never be in completed status.

            // You can retrieve documents and perform action against each document like this
            ConversionService cs = Framework.getService(ConversionService.class);
            for (DocumentModel doc : loadDocuments(session, ids)) {
                Blob glbBlob = (Blob) doc.getPropertyValue("file:content");
                HashMap<String, Serializable> params = new HashMap<>();
                params.put("targetFileName","thumbnail.png");
                BlobHolder result = cs.convert("glb2png", new SimpleBlobHolder(glbBlob),params);
                Blob thumbnailBlob = result.getBlob();
                if (!doc.hasFacet(THUMBNAIL_FACET)) {
                    doc.addFacet(THUMBNAIL_FACET);
                }
                doc.setPropertyValue("thumb:thumbnail", (Serializable) thumbnailBlob);
                session.saveDocument(doc);
            }

            // Parameters can also be send to the BulkAction. See example of setPropertiesAction call in Bulk Action Framework documentation if needed
            // for (Map.Entry<String, Serializable> es : properties.entrySet()) {
            //     es.getKey();
            //     es.getValue();
            // }
        }
    }
}
