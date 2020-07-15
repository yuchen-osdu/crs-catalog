Copyright 2017-2019, Schlumberger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
# Spatial Reference Catalog Service

## This repository contains 
1. The Java implementation of the Spatial Reference Catalog (aka crs-catalog). The Java code is located in the ```src``` folder. To open the Java project, open ```pom.xml```.
1. Tests are located in ```src/test/java/org/opengroup/osdu/crs/...```
1. The openapi specification file is crs_catalog_openapi.json.
1. Python integration and health tests in the ```testing``` folder. 
See also the test's [README.md](testing/README.md)

## Prerequisites
1. The project builds with [maven](https://maven.apache.org/). Make sure maven is installed locally.
1. The project requires the [Lombok](https://projectlombok.org/) plug-in installed for your IDE.

### Build service and run unit tests

```sh
mvn clean install
```

## Running Azure Catalog Service locally
### Configure Maven Settings
To obtain maven dependencies from the **Azure DevOps Artifacts** we need to configure the value for variable ${VSTS_FEED_TOKEN} described in `maven\settings.xml`:
- **Get token:**  
We can use personal token generated in VSTS on [Personal Access Tokens](https://dev.azure.com/slb-swt/_usersSettings/tokens) > New token > Organization: slb-des-ext-collaboration > Create
- **Set token in your local home folder:** 
Open or create `USER_HOME_FOLDER\.m2\settings.xml` and paste your personal token in `<password></password>`section.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
        <server>
          <id>os-core</id>
          <username>slb-des-ext-collaboration</username>
          <password></password>
        </server>
  </servers>
</settings>
```
#### Build and run Catalog Service locally using bash
- Set the required environments described in [Release/deployment](##Release/deployment) section
- Navigate to the Catalog Service's root folder ```os-crs-catalog-service``` 
- Build core and run unit tests in command line:
```bash
mvn clean install -P crs-catalog-core
# To run without tests add -Dmaven.test.skip=true
```
- Navigate to the Catalog Service's Azure 
- Build services in command line:
```bash
mvn clean package -P crs-catalog-aks,crs-catalog-core
```
- Navigate to the Catalog Service's root folder ```os-crs-catalog-service``` 
- Run application with command
```bash
java -jar provider/crs-catalog-azure/crs-catalog-aks/target/crs-catalog-aks-1.0.0.jar
```

#### Running Azure Catalog Service using IntelliJ IDEA
Navigate to the **Create Run/Debug Configuration** tool
Select **'Add New Configuration'** and select **Application**

Type the next commands into the suggested fields: 
- Working directory: ```{path_to_the_catalog}/os-crs-catalog-service``` 
- Main class: ```org.opengroup.osdu.crs.CRSAKSApplication``` 
- Use classpath of module:  ```crs-catalog-aks```  
***Note: If you don't see "crs-catalog-aks" in the dropdown menu - find appropriate pom.xml and click "Add as a Maven project"***
- Environment variables: Set the required environments described in [Release/deployment](##Release/deployment) section  

Execute **Run** or **Debug** for configured Application


### Debug locally - e.g. using Postman
In the Postman Settings / General, turn SSL certificate validation off when running locally.
Similarly, when not using Postman but client code, set the configuration  ```verify_ssl``` false (see [instructions](https://github.com/swagger-api/swagger-codegen/issues/7778))

Run application using debug mode and use [Postman](https://www.getpostman.com/)
to send a GET request to obtaining the Swagger API documentation
```
http://localhost:8080/api/crs/catalog/v2/swagger.json
```
or send a Get request to catalog:
```
http://localhost:8080/api/crs/catalog/v2
```
Open the Swagger-UI:
```
http://localhost:8080/api/crs/catalog/swagger-ui.html
```

Headers for Postman:
| Key | Value |
|----------|----------|
| Authorization | Bearer `<token>` |
| data-partition-id | $MY_TENANT (see [testing\README.md](testing/README.md)) |

### Build and run the Docker container locally
1. Run the `maven run` command to have the .jar file generated.
1. Have the Azure subscription set up 
1. Open a Powershell
1. Install the Azure CLI locally
1. Authenticate yourself to Azure Container Registry (acr) with the following command:
```az acr login --name delfi```
1. Execute the following command to build the container image:
```docker build -t crs-catalog .```
1. Execute the following command to build the container image:
```docker run -t --rm -p 8080:8080 crs-catalog```
1. Use Postman or curl to try out the endpoints


## Release/deployment
VSTS release definition is located at provider\crs-catalog-azure\crs-catalog-aks\devops, which 
requires the following environment variables:

| Variable | Contents |
|----------|----------|
| ENTITLEMENT_URL | Required |
| CRS_CATALOG_FILENAME | Required, e.g. `{path_to_the_catalog}/os-crs-catalog-service/data/crs_catalog_v2.json` |
