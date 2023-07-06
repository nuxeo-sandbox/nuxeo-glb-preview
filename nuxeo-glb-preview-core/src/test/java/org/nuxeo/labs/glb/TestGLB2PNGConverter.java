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
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

@RunWith(FeaturesRunner.class)
@Features({TestFeature.class})
public class TestGLB2PNGConverter {

    @Inject
    protected CommandLineExecutorService commandLineExecutorService;

    @Inject
    protected ConversionService conversionService;

    @Test
    public void commandLineIsAvailable() {
        Assert.assertTrue(commandLineExecutorService.getCommandAvailability("glb2png").isAvailable());
    }

    @Test
    public void converterIsAvailable() {
        Assert.assertTrue(conversionService.isConverterAvailable("glb2png").isAvailable());
    }

    @Test
    public void testConversion() {
        File file = new File(getClass().getResource("/files/scene.glb").getPath());
        Blob blob = new FileBlob(file,"model/gltf-binary");
        BlobHolder bh = new SimpleBlobHolder(blob);
        HashMap<String, Serializable> params = new HashMap<>();
        params.put("targetFileName","output.png");
        params.put("height", "480");
        params.put("width","480");
        params.put("environment","");
        BlobHolder conversionResult = conversionService.convert("glb2png",bh,params);
        Blob imageBlob = conversionResult.getBlob();
        Assert.assertNotNull(imageBlob);
        Assert.assertEquals("image/png",imageBlob.getMimeType());
        Assert.assertEquals("output.png",imageBlob.getFilename());
        Assert.assertTrue(imageBlob.getLength()>0);
    }

}
