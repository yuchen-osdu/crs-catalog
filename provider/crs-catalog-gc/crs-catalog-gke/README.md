# Spatial Reference Catalog Service

The main goals of the Spatial Reference Catalog service are to offer

- *Coordinate Reference Systems* (CRSs) to enable end-users to
make a CRS selection.
- Search for CRSs given a number of constraints.
- Download of the entire catalog for local caching and when the cache
has to be refreshed.
- Access to various sub-sets of the catalog.
- Once a CRS is found, produce a persistable reference to be stored with
the data, which fully describes the CRS; this persistable reference string
becomes catalog independent. This means any consumer will be able to
understand the CRS definition even if a different catalog is used in the
future context.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Requirements
* Java 8
* [Maven 3.6.0+](https://maven.apache.org/download.cgi)
* GCloud command line tool
* GCloud access to opendes project

### General Tips

**Environment Variable Management**
The following tools make environment variable configuration simpler
 - [direnv](https://direnv.net/) - for a shell/terminal environment
 - [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) - for [Intellij IDEA](https://www.jetbrains.com/idea/)

**Lombok**
This project uses [Lombok](https://projectlombok.org/) for code generation. You may need to configure your IDE to take advantage of this tool.
 - [Intellij configuration](https://projectlombok.org/setup/intellij)
 - [VSCode configuration](https://projectlombok.org/setup/vscode)
 
### Installation
In order to run the service locally or remotely, you will need to have the following environment variables defined.
 
 | name | value | description | sensitive? | source |
 | ---  | ---   | ---         | ---        | ---    |
 | `LOG_PREFIX` | `service` | Logging prefix | no | - |
 | `SERVER_SERVLET_CONTEXPATH` | `/api/crs/catalog/` | CRS Catalog service context path | no | - |
 | `CRS_CATALOG_FILENAME` | ex `C:\\osdu-delfi\\os-crs-catalog-service\\data\\crs_catalog_v2.json` | Crs Catalog V2 file | no | output of infrastructure deployment |
 | `ENTITLEMENTS_API` | `http://entitlements/api/entitlements/v2/` | Entitlements service endpoint | no | - |
 | `SEARCH_API` | `http://search/api/search/v2` | Search service endpoint | no | - |

### Run Locally
Check that maven is installed:

```bash
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 1.8.0_212, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/jdk8u212-b04/jre
...
```

You will need to configure access to the remote maven repository that holds the OSDU dependencies. This file should live within `~/.m2/settings.xml`:

```bash
$ cat ~/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>os-core</id>
            <username>slb-des-ext-collaboration</username>
            <!-- Treat this auth token like a password. Do not share it with anyone, including Microsoft support. -->
            <password>${VSTS_FEED_TOKEN}</password>
        </server>
    </servers>
</settings>
```

* Update the Google cloud SDK to the latest version:

```bash
gcloud components update
```
* Set Google Project Id:

```bash
gcloud config set project <YOUR-PROJECT-ID>
```

* Perform a basic authentication in the selected project:

```bash
gcloud auth application-default login
```

* Navigate to CRS Catalog service root folder and run:

```bash
mvn jetty:run
## Testing
* Navigate to CRS Catalog service root folder and run:
 
```bash
mvn clean install   
```

* If you wish to see the coverage report then go to testing/target/site/jacoco-aggregate and open index.html

* If you wish to build the project without running tests

```bash
mvn clean install -DskipTests
```

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*

```bash
cd provider/crs-catalog-gc/crs-catalog-gke/ && mvn spring-boot:run
```

## Testing
Navigate to CRS Catalog service root folder and run all the tests:

```bash
# build + test + install core service code
$ (cd crs-catalog-core/ && mvn clean install)
```

## Test the application
After the service has started it should be accessible via a web browser by visiting [http://localhost:8080/api/crs/catalog/swagger-ui.html](http://localhost:8080/api/crs/catalog/swagger-ui.html). If the request does not fail, you can then run the integration tests.

### Running E2E Tests 
This section describes how to run cloud OSDU E2E tests (testing/catalog_test_gc/jwt_client.py).

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `INTEGRATION_TESTER` | `********` | A base64 encoded google service account json credentials authorization for OSDU services | yes | output of infrastructure deployment |
| `GOOGLE_AUDIENCES` | ex `*****.apps.googleusercontent.com` | Client ID for getting access to cloud resources | yes | https://console.cloud.google.com/apis/credentials |

This section describes how to run cloud OSDU E2E tests (testing/catalog_test_anthos/jwt_client.py).

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `INTEGRATION_TESTER` | `********` | A base64 encoded google service account json credentials authorization for OSDU services | yes | output of infrastructure deployment |

## Tests core (testing/catalog_test_core/constants.py)
| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `BASE_URL` | ex `/api/crs/catalog/v2` | CRS Catalog service context path | yes | output of infrastructure deployment |
| `VIRTUAL_SERVICE_HOST_NAME` | ex `open.opendes.cloud.slb-ds.com` | - | false | output of infrastructure deployment |
| `MY_TENANT` | ex `opendes` | Current Tenant | false | output of infrastructure deployment |

| INTEGRATION_TESTER | 
| ---  |
| users<br/>service.entitlements.user<br/>data.test1<br/>data.integration.test<br/>users@{tenant1}@{domain}.com |

Execute following command to build code and run all the integration tests:

## Building/running
This integration test uses a swagger generated Python client to test a 
deployed crs-catalog service.

The python client code is automatically generated. The latest online version (May 2018)
created incorrect impost statements for cyclic class references. Therefore the current
code is generated using [swagger-codegen-cli-2.2.3.jar](http://repo1.maven.org/maven2/io/swagger/swagger-codegen-cli/2.2.3/swagger-codegen-cli-2.2.3.jar).
The command to create the python code is:

```bash
cd testing
java -jar ~/swagger-codegen-cli-2.2.3.jar generate -i catalog_test_core/api_spec/crs-catalog-openapi-v2.json -l python -o catalog_test_core/v2
```

Go to the provider folder:
```bash
cd catalog_test_$PROVIDER_NAME/ # e.g. catalog_test_sauth
```

To set up a virtual environment:
```bash
virtualenv venv
```

To activate the venv:
```bash
venv\Scripts\activate (on Windows)
source venv/bin/activate (on Linux)
```

Install runtime dependencies in venv
```bash
python3 -m pip install -r requirements.txt
```

To run:
```bash
python3 run_test.py
```

**Note:** To simulate a runtime exactly as that of the vsts build agent, you can simply exec into the docker image we use for the build agent, and run the tests from inside it. To know how to do this, please follow [this](https://slb-swt.visualstudio.com/data-at-rest/_git/dps-vsts-build-agent?path=%2FREADME.md&version=GBmaster) documentation.

## Deployment
See Google Documentation: https://cloud.google.com/cloud-build/docs/deploying-builds/deploy-gke

## Licence
Copyright © Google LLC
Copyright © EPAM Systems
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
