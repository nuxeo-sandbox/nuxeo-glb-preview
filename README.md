nuxeo-glb-preview
===================

A plugin that adds glb file preview capabilities to the nuxeo platform

# List of Features (details below)

- GLB to PNG commandline based converter
- A thumbnail factory tied to a GLB document facet
- A webui element to view GLB files that leverages [model-viewer](https://modelviewer.dev/)

## Webui

below is an example of how to use the viewer in a document view layout 

```html
<link rel="import" href="../../nuxeo-glb-preview/widgets/nuxeo-glb-viewer.html">
<link rel="import" href="../../nuxeo-glb-preview/widgets/nuxeo-glb-renditions.html">

<!--
`nuxeo-custom-type-view-layout`
@group Nuxeo UI
@element nuxeo-custom-type-view-layout
-->
<dom-module id="nuxeo-custom-type-view-layout">
    <template>
        <style include="nuxeo-styles">
            .container {
                @apply --paper-card;
                padding: 0;
                margin-bottom: 0;
            }

            nuxeo-glb-renditions, nuxeo-document-blob {
                @apply --paper-card;
            }

            nuxeo-glb-viewer {
                width: 100%;
                height: calc(75vh - 100px);
            }
        </style>

        <div class="container">
            <nuxeo-glb-viewer document="[[document]]"></nuxeo-glb-viewer>
        </div>

        <nuxeo-document-blob document="[[document]]"></nuxeo-document-blob>

        <nuxeo-glb-renditions document="[[document]]" label="Renditions"></nuxeo-glb-renditions>

    </template>[README.md](README.md)
    
  <script>
    Polymer({
      is: 'nuxeo-custom-type-view-layout',
      behaviors: [Nuxeo.LayoutBehavior],
      properties: {
        /**
         * @doctype custom-type
         */
        document: {
          type: Object
        }
      }
    });
  </script>
</dom-module>
```


# Build

Assuming maven is correctly setup on your computer:

```
git clone https://github.com/nuxeo-sandbox/nuxeo-glb-preview
cd nuxeo-glb-preview
mvn clean install
```

To build the plugin without building the Docker image, use:

```
mvn -DskipDocker=true clean install
```


# Install

Install the package on your instance.

This plugin relies on [screenhot-glb](https://github.com/Shopify/screenshot-glb) which must be installed on your nuxeo server performing the conversion. Have a look at the repository [Dockerfile](https://github.com/nuxeo-sandbox/nuxeo-glb-preview/blob/master/nuxeo-glb-preview-docker/Dockerfile) to find more details about the installation steps.


# Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.

# License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

# About Nuxeo

Nuxeo Platform is an open source Content Services platform, written in Java. Data can be stored in both SQL & NoSQL databases.

The development of the Nuxeo Platform is mostly done by Nuxeo employees with an open development model.

The source code, documentation, roadmap, issue tracker, testing, benchmarks are all public.

Typically, Nuxeo users build different types of information management solutions for [document management](https://www.nuxeo.com/solutions/document-management/), [case management](https://www.nuxeo.com/solutions/case-management/), and [digital asset management](https://www.nuxeo.com/solutions/dam-digital-asset-management/), use cases. It uses schema-flexible metadata & content models that allows content to be repurposed to fulfill future use cases.

More information is available at [www.nuxeo.com](https://www.nuxeo.com).
