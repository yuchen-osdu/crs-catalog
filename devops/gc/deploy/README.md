<!--- Deploy -->

# Deploy helm chart

## Introduction

This chart bootstraps a deployment on a [Kubernetes](https://kubernetes.io) cluster using [Helm](https://helm.sh) package manager.

## Prerequisites

The code was tested on **Kubernetes cluster** (v1.21.11) with **Istio** (1.12.6)
> It is possible to use other versions, but it hasn't been tested

### Operation system

The code works in Debian-based Linux (Debian 10 and Ubuntu 20.04) and Windows WSL 2. Also, it works but is not guaranteed in Google Cloud Shell. All other operating systems, including macOS, are not verified and supported.

### Packages

Packages are only needed for installation from a local computer.

- **HELM** (version: v3.7.1 or higher) [helm](https://helm.sh/docs/intro/install/)
- **Kubectl** (version: v1.21.0 or higher) [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)

## Installation

Before installing deploy Helm chart you need to set variables in **values.yaml** file using any code editor. Some of the values are prefilled, but you need to specify some values as well. You can find more information about them below.

### Configmap variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**data.logLevel** | logging level | string | `ERROR` | yes
**data.entitlementsHost** | Entitlements service host | string | `http://entitlements` | yes

### Deployment variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**data.image** | path to the image in a registry | string | - | yes
**data.requestsCpu** | amount of requests CPU | string | `10m` | yes
**data.requestsMemory** | amount of requests memory| string | `350Mi` | yes
**data.limitsCpu** | CPU limit | string | `1` | yes
**data.limitsMemory** | memory limit | string | `1G` | yes
**data.serviceAccountName** | name of kubernetes service account | string | `crs-catalog` | yes
**data.imagePullPolicy** | when to pull the image | string | `IfNotPresent` | yes

### Configuration variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**conf.domain** | your domain, ex `example.com` | string | - | yes
**conf.appName** | Service name | string | `crs-catalog` | yes
**conf.onPremEnabled** | whether on-prem is enabled | boolean | false | yes

### ISTIO variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**istio.proxyCPU** | CPU request for Envoy sidecars | string | 10m | yes
**istio.proxyCPULimit** | CPU limit for Envoy sidecars | string | 200m | yes
**istio.proxyMemory** | memory request for Envoy sidecars | string | 100Mi | yes
**istio.proxyMemoryLimit** | memory limit for Envoy sidecars | string | 256Mi | yes

### Install the helm chart

Run this command from within this directory:

```console
helm install gc-crs-catalog-deploy .
```

## Uninstalling the Chart

To uninstall the helm release:

```console
helm uninstall gc-crs-catalog-deploy
```

[Move-to-Top](#deploy-helm-chart)
