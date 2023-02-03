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

# Install FFmpeg, mediainfo and codecs
RUN dnf config-manager --set-enabled crb && dnf -y install epel-release && \
    dnf -y install https://mirrors.rpmfusion.org/free/el/rpmfusion-free-release-9.noarch.rpm

# Dependencies for GLB
RUN dnf -y install alsa-lib at-spi2-atk libX11 libXcomposite libXcursor libXdamage libXext libXi libXtst libXScrnSaver libXrandr libXrandr atk pango gtk3

# Set back original (nuxeo) user
USER 900

# Install node.js and tools
RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash && \
    source ~/.bash_profile && nvm install 18.16.1 && npm install -g @shopify/screenshot-glb @gltf-transform/cli

# Workaround to make the node commands available in the default system PATH used by Java
USER 0
RUN ln -s /home/nuxeo/.nvm/versions/node/v18.16.1/bin/node /usr/local/bin/node
RUN ln -s /home/nuxeo/.nvm/versions/node/v18.16.1/bin/screenshot-glb /usr/local/bin/screenshot-glb
RUN ln -s /home/nuxeo/.nvm/versions/node/v18.16.1/bin/gltf-transform /usr/local/bin/gltf-transform
USER 900

#install nuxeo package
COPY --chown=nuxeo:0 target/nuxeo-glb-preview-package-*.zip /home/nuxeo/local-packages/nuxeo-glb-preview-package.zip
RUN /install-packages.sh --offline /home/nuxeo/local-packages/nuxeo-glb-preview-package.zip
