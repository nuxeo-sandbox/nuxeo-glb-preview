package org.nuxeo.labs.glb.listener;

import java.util.Arrays;
import java.util.List;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitFilteringEventListener;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

import static org.nuxeo.labs.glb.bulk.ComputeGlbThumbnail.ACTION_NAME;

public class GlbModifiedListener implements PostCommitFilteringEventListener {
  
  protected final List<String> handled = Arrays.asList("documentCreated", "documentModified");
  

    @Override
    public void handleEvent(EventBundle events) {
        for (Event event : events) {
            if (acceptEvent(event)) {
                handleEvent(event);
            }
        }
    }

    @Override
    public boolean acceptEvent(Event event) {
        return handled.contains(event.getName());
    }

  
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
          return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();

        if (doc.hasSchema("file") && !doc.isProxy()) {
            Blob blob = (Blob) doc.getPropertyValue("file:content");
            if (blob != null && "model/gltf-binary".equals(blob.getMimeType())) {
                String query = "SELECT * FROM Document WHERE ecm:uuid='" + doc.getId() + "'";
                BulkService service = Framework.getService(BulkService.class);
                String username = ctx.getPrincipal().getName();
                service.submit(
                        new BulkCommand.Builder(ACTION_NAME, query, username).build());
            }
        }
    }
}
