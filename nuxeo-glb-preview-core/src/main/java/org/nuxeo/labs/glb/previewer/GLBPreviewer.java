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

package org.nuxeo.labs.glb.previewer;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.download.DownloadService;
import org.nuxeo.ecm.platform.preview.adapter.MimeTypePreviewer;
import org.nuxeo.ecm.platform.preview.api.PreviewException;
import org.nuxeo.runtime.api.Framework;

import java.util.List;

public class GLBPreviewer implements MimeTypePreviewer {

    @Override
    public List<Blob> getPreview(Blob blob, DocumentModel dm) throws PreviewException {

        DownloadService downloadService = Framework.getService(DownloadService.class);
        String blobUrl = downloadService.getFullDownloadUrl(dm, "file:content", blob, "/nuxeo/");
        String thumbnailUrl = downloadService.getFullDownloadUrl(dm, "thumb:thumbnail", blob, "/nuxeo/");

        String head =
            "<script type=\"module\" src=\"https://unpkg.com/@google/model-viewer/dist/model-viewer.min.js\"></script>\n" +
            "<style>\n" +
            "html,body {\n" +
            "    height: 100%;\n" +
            "    width: 100%;\n" +
            "    margin: 0px;\n" +
            "}\n" +
            "\n" +
            "model-viewer {\n" +
            "    height: 100%;\n" +
            "    width: 100%;\n" +
            "}\n" +
            "</style>\n";

        String body = String.format(
            "<model-viewer alt=\"%s\" src=\"%s\" poster=\"%s\"\n" +
            "   environment-image=\"neutral\" shadow-intensity=\"0\" shadow-softness=\"0\" exposure=\"0.92\" camera-controls>\n" +
            "</model-viewer>\n"
        ,dm.getTitle(),blobUrl,thumbnailUrl);

        String html = String.format(
            "<!doctype html>\n" +
            "<html>\n" +
            "    <head>%s</head>\n" +
            "    <body>%s</body>\n" +
            "</html>\n"
        , head, body);

        Blob mainBlob = Blobs.createBlob(html, "text/html", "UTF-8", "index.html");
        return List.of(mainBlob);
    }
}
