package org.nuxeo.labs.glb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.nuxeo.ecm.core.bulk.message.BulkStatus.State.COMPLETED;
import static org.nuxeo.labs.glb.bulk.ComputeGlbThumbnail.ACTION_NAME;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.bulk.BulkService;
import org.nuxeo.ecm.core.bulk.CoreBulkFeature;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.bulk.message.BulkStatus;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.time.Duration;

@RunWith(FeaturesRunner.class)
@Features({ CoreFeature.class, CoreBulkFeature.class })
@Deploy({ "org.nuxeo.labs.glb.nuxeo-glb-preview-core" })
public class TestComputeGlbThumbnail {

    @Inject
    protected BulkService bulkService;

    @Inject
    protected BulkService service;

    @Inject
    protected CoreSession session;

    @Test
    public void testAction() throws Exception {
        // Change NXQL to retrieve the expected test documents
        String nxql = "SELECT * FROM Document WHERE ecm:isTrashed = 0";

        BulkCommand command = new BulkCommand.Builder(ACTION_NAME, nxql,
        session.getPrincipal().getName()).repository(session.getRepositoryName()).build();
        String commandId = service.submit(command);

        // Wait explicitly for the submitted command to be over
        assertTrue(bulkService.await(commandId, Duration.ofSeconds(20)));

        BulkStatus status = service.getStatus(commandId);
        assertNotNull(status);
        assertEquals(COMPLETED, status.getState());

        // TODO: implement test
        // For instance, validate that documents have been modified as expected(e.g. they are no longer retrieved by the NXQL if a flag metadata has been changed)
        assertEquals(0, session.query(nxql).size());
    }
}
