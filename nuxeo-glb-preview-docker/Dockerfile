# vim:set ft=dockerfile:
ARG BASE_IMAGE

FROM ${BASE_IMAGE}

ARG VERSION
ARG SCM_REF
ARG BUILD_TAG
ARG NUXEO_CLID
ARG PACKAGES

LABEL com.customer.sample.version=${VERSION}
LABEL com.customer.sample.scm-ref=${SCM_REF}
LABEL com.customer.sample.build-tag=${BUILD_TAG}

# We need to be root to run yum commands
USER 0

RUN yum -y install alsa-lib at-spi2-atk libX11 libXcomposite libXcursor libXdamage libXext libXi libXtst libXScrnSaver libXrandr libXrandr atk pango gtk3

# Set back original (nuxeo) user
USER 900

# install node.js and screenshot-glb
RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash && \
    source ~/.bash_profile && nvm install 16.20.0 && npm install -g @shopify/screenshot-glb

# workaround to make the screenshot-glb command available in the default system PATH used by Java
USER 0
RUN ln -s /home/nuxeo/.nvm/versions/node/v16.20.0/bin/node /usr/local/bin/node
RUN ln -s /home/nuxeo/.nvm/versions/node/v16.20.0/bin/screenshot-glb /usr/local/bin/screenshot-glb
USER 900

#install nuxeo package
COPY --chown=nuxeo:0 target/nuxeo-glb-preview-package-*.zip /home/nuxeo/local-packages/nuxeo-glb-preview-package.zip
RUN /install-packages.sh --offline /home/nuxeo/local-packages/nuxeo-glb-preview-package.zip
