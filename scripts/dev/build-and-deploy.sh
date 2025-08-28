#!/bin/bash

set -euo pipefail

APP_NAME="mm-recommendation-service"
IMAGE_TAG="local"
NAMESPACE="movie-mate"
DOCKER_IMAGE="${APP_NAME}:${IMAGE_TAG}"

echo "🔨 Building local image ${DOCKER_IMAGE} with Jib..."

# Save working dir to come back later
ORIGINAL_DIR="$(pwd)"

# Go to project root
cd ../..

# Build to local Docker without pushing
LOCAL_BUILD=true mvn compile jib:dockerBuild \
  -Dimage=${DOCKER_IMAGE}

# Save the image as tarball
echo "📦 Saving Docker image..."
docker save "${DOCKER_IMAGE}" -o "${APP_NAME}.tar"

# Import into k3s containerd
echo "📦 Importing image into k3s containerd..."
sudo k3s ctr images import "${APP_NAME}.tar"

# Cleanup tarball
rm -f "${APP_NAME}.tar"

# Return to script dir (1 level up from Helm chart)
cd ..

echo "🚀 Deploying to Kubernetes with Helm..."
helm uninstall mm-recommendation-service -n movie-mate
helm upgrade --install ${APP_NAME} ./mm-infrastructure/k8s/core/${APP_NAME} \
  -f ./mm-infrastructure/k8s/core/${APP_NAME}/values.local.yaml \
  -n ${NAMESPACE} --create-namespace

echo "✅ Done! Image '${DOCKER_IMAGE}' deployed to '${NAMESPACE}' namespace."
