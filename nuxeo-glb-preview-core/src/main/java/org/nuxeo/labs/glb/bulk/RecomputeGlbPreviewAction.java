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
 */
package org.nuxeo.labs.glb.bulk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.bulk.action.computation.AbstractBulkComputation;
import org.nuxeo.ecm.core.bulk.message.BulkCommand;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.labs.glb.adapter.GLBModelAdapter;
import org.nuxeo.labs.glb.worker.GLBConversionWork;
import org.nuxeo.lib.stream.computation.Topology;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.stream.StreamProcessorTopology;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.nuxeo.ecm.core.bulk.BulkServiceImpl.STATUS_STREAM;
import static org.nuxeo.lib.stream.computation.AbstractComputation.INPUT_1;
import static org.nuxeo.lib.stream.computation.AbstractComputation.OUTPUT_1;

/**
 * BAF Computation that fills picture views for the blob property described by the given xpath.
 *
 * @since 11.1
 */
public class RecomputeGlbPreviewAction implements StreamProcessorTopology {

    private static final Logger log = LogManager.getLogger(RecomputeGlbPreviewAction.class);

    public static final String ACTION_NAME = "recomputeGlbPreview";

    // @since 11.1
    public static final String ACTION_FULL_NAME = "bulk/" + ACTION_NAME;

    public static final String PARAM_XPATH = "xpath";

    @Override
    public Topology getTopology(Map<String, String> options) {
        return Topology.builder()
                .addComputation(RecomputeGlbPreviewComputation::new, //
                        Arrays.asList(INPUT_1 + ":" + ACTION_FULL_NAME, OUTPUT_1 + ":" + STATUS_STREAM))
                .build();
    }

    public static class RecomputeGlbPreviewComputation extends AbstractBulkComputation {

        protected String xpath;

        public RecomputeGlbPreviewComputation() {
            super(ACTION_FULL_NAME);
        }

        @Override
        public void startBucket(String bucketKey) {
            BulkCommand command = getCurrentCommand();
            xpath = command.getParam(PARAM_XPATH);
        }

        @Override
        protected void compute(CoreSession session, List<String> ids, Map<String, Serializable> properties) {
            log.debug("Compute action: {} for doc ids: {}", ACTION_NAME, ids);
            WorkManager workManager = Framework.getService(WorkManager.class);
            DocumentModelList documents = loadDocuments(session, ids);
            for(DocumentModel doc : documents) {
                Blob blob = (Blob) doc.getPropertyValue(PARAM_XPATH);
                if (blob == null) {
                    continue;
                }
                GLBModelAdapter adapter = doc.getAdapter(GLBModelAdapter.class);
                if (adapter == null) {
                    continue;
                }
                adapter.clear();
                GLBConversionWork work = new GLBConversionWork(doc.getRepositoryName(), doc.getId());
                log.debug("Scheduling work: glb conversion of document {}.", doc);
                workManager.schedule(work, true);
            }
        }

    }

}
