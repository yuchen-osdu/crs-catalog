# Copyright © Amazon
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import sys
import urllib3
urllib3.disable_warnings()
import json
import time

from catalog_test_core.v3.HttpClient import HttpClient

def main(argv):
    pass

# coding: utf-8
"""Integration unit tests for crs-catalog service V2"""
import unittest
import jwt_client
import catalog_test_core.constants as constants

if __name__ == '__main__':
    main(sys.argv)

legal_tag_name = "crs-catalog-int-test-legaltag"


class TestCrsCatalog(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.path = '../catalog_test_core/'
        if constants.LOCAL_MODE == "true":
            cls.path = './catalog_test_core/'

        cls.client = HttpClient(constants.ROOT_URL, constants.MY_TENANT, jwt_client)

        # Make sure schemas are made
        with open(f'{cls.path}v3/CoordinateReferenceSystemSchema.json') as crs_schema_file:
            crs_schema = crs_schema_file.read().replace('{{data_partition_id}}', constants.MY_TENANT)
            schema_response = cls.client.make_request('POST', '/api/schema-service/v1/schema', crs_schema)
            if schema_response.status_code != 201 and (schema_response.status_code != 400 and 'Schema Id is already present' not in schema_response.content.decode("utf-8", "ignore")):
                raise Exception(f"Could not create CRS schema. Received {schema_response.status_code} from schema service")

        with open(f'{cls.path}v3/CoordinateTransformationSchema.json') as ct_schema_file:
            ct_schema = ct_schema_file.read().replace('{{data_partition_id}}', constants.MY_TENANT)
            schema_response = cls.client.make_request('POST', '/api/schema-service/v1/schema', ct_schema)
            if schema_response.status_code != 201 and (schema_response.status_code != 400 and 'Schema Id is already present' not in schema_response.content.decode("utf-8", "ignore")):
                raise Exception(f"Could not create CT schema. Received {schema_response.status_code} from schema service")

        # Make sure legal tag is made
        with open(f'{cls.path}v3/LegalTag.json') as legal_tag_file:
            legal_tag = legal_tag_file.read().replace('{{legal_tag_name}}', legal_tag_name)
            legal_response = cls.client.make_request('POST', '/api/legal/v1/legaltags', legal_tag)
            if legal_response.status_code not in [201, 409]:
                raise Exception(f"Could not create legal tag. Received {legal_response.status_code} from legal service")

        # Create records
        with open(f'{cls.path}v3/CRSAndCTRecords.json') as records_file:
            records = records_file.read().replace('{{data_partition_id}}', constants.MY_TENANT).replace('{{acl_domain}}', constants.ACL_DOMAIN)
            records_obj = json.loads(records)
            storage_response = cls.client.make_request('PUT', '/api/storage/v2/records', records)
            if storage_response.status_code != 201:
                raise Exception(f"Could not create records. Received {storage_response.status_code} from storage service")

            # check that records show up in search before continuing
            found = False
            max_checks = 20
            checks = 0
            while found == False:
                if checks >= max_checks:
                    raise Exception(f"Could not find records indexed against search after {max_checks} checks")
                    break
                search_response = cls.client.make_request('POST', '/api/search/v2/query', f"""
                    {{
                        "kind":"osdu:wks:reference-data--Coordinate*:1.1.0"
                    }}
                    """)
                if search_response.status_code != 200:
                    raise Exception(f"Could not search. received {search_response.status_code} from search service")
                search_response_body = json.loads(search_response.content)
                if search_response_body["totalCount"] == len(records_obj):
                    found = True
                else:
                    checks += 1
                    print(f"Didn't find records after {checks} checks. Waiting a bit to let records index")
                    time.sleep(5)

            # Some timing issues still persist without waiting a bit longer
            time.sleep(10)

    def test_get_coordinate_transformation_dataId(self):
        with open(f'{self.path}v3/GetCoordinateTransformationTestData.json') as test_data_file:
            test_data = json.loads(test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))

            response = self.client.make_request('GET', f'/api/crs/catalog/v3/coordinate-transformation?dataId={test_data["dataId"]}')
            response_body = json.loads(response.content)
            assert response.status_code == 200
            assert len(response_body["searchResults"]["results"]) == 1
            assert response_body["searchResults"]["results"][0]["id"] == test_data["recordId"]
            assert response_body["searchResults"]["results"][0]["data"]["ID"] == test_data["dataId"]

    def test_get_coordinate_transformation_recordId(self):
        with open(f'{self.path}v3/GetCoordinateTransformationTestData.json') as test_data_file:
            test_data = json.loads(test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))

            response = self.client.make_request('GET', f'/api/crs/catalog/v3/coordinate-transformation?recordId={test_data["recordId"]}')
            response_body = json.loads(response.content)
            assert response.status_code == 200
            assert len(response_body["searchResults"]["results"]) == 1
            assert response_body["searchResults"]["results"][0]["id"] == test_data["recordId"]
            assert response_body["searchResults"]["results"][0]["data"]["ID"] == test_data["dataId"]

    def test_get_coordinate_transformation_dataId_recordId(self):
        with open(f'{self.path}v3/GetCoordinateTransformationTestData.json') as test_data_file:
            test_data = json.loads(test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))

            response = self.client.make_request('GET', f'/api/crs/catalog/v3/coordinate-transformation?dataId={test_data["dataId"]}&recordId={test_data["recordId"]}')
            response_body = json.loads(response.content)
            assert response.status_code == 200
            assert len(response_body["searchResults"]["results"]) == 1
            assert response_body["searchResults"]["results"][0]["id"] == test_data["recordId"]
            assert response_body["searchResults"]["results"][0]["data"]["ID"] == test_data["dataId"]

    def test_get_coordinate_reference_system_dataId(self):
        with open(f'{self.path}v3/GetCoordinateReferenceSystemTestData.json') as test_data_file:
            test_data = json.loads(test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))

            response = self.client.make_request('GET', f'/api/crs/catalog/v3/coordinate-reference-system?dataId={test_data["dataId"]}')
            response_body = json.loads(response.content)
            assert response.status_code == 200
            assert len(response_body["searchResults"]["results"]) == 1
            assert response_body["searchResults"]["results"][0]["id"] == test_data["recordId"]
            assert response_body["searchResults"]["results"][0]["data"]["ID"] == test_data["dataId"]

    def test_get_coordinate_reference_system_recordId(self):
        with open(f'{self.path}v3/GetCoordinateReferenceSystemTestData.json') as test_data_file:
            test_data = json.loads(test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))

            response = self.client.make_request('GET', f'/api/crs/catalog/v3/coordinate-reference-system?recordId={test_data["recordId"]}')
            response_body = json.loads(response.content)
            assert response.status_code == 200
            assert len(response_body["searchResults"]["results"]) == 1
            assert response_body["searchResults"]["results"][0]["id"] == test_data["recordId"]
            assert response_body["searchResults"]["results"][0]["data"]["ID"] == test_data["dataId"]

    def test_get_coordinate_reference_system_dataId_recordId(self):
        with open(f'{self.path}v3/GetCoordinateReferenceSystemTestData.json') as test_data_file:
            test_data = json.loads(test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))

            response = self.client.make_request('GET', f'/api/crs/catalog/v3/coordinate-reference-system?dataId={test_data["dataId"]}&recordId={test_data["recordId"]}')
            response_body = json.loads(response.content)
            assert response.status_code == 200
            assert len(response_body["searchResults"]["results"]) == 1
            assert response_body["searchResults"]["results"][0]["id"] == test_data["recordId"]
            assert response_body["searchResults"]["results"][0]["data"]["ID"] == test_data["dataId"]

    def test_search_coordinate_transformations(self):
        with open(f'{self.path}v3/SearchCoordinateTransformations.json') as test_data_file:
            test_data = test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT)
            test_data_obj = json.loads(test_data)

            response = self.client.make_request('POST', f'/api/crs/catalog/v3/coordinate-transformation', test_data)
            response_body = json.loads(response.content)
            assert response.status_code == 200
            assert len(response_body["searchResults"]["results"]) == 1

    def test_search_coordinate_transformations(self):
        with open(f'{self.path}v3/SearchCoordinateTransformations.json') as test_data_file:
            test_data = test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT)
            test_data_obj = json.loads(test_data)

            response = self.client.make_request('POST', f'/api/crs/catalog/v3/coordinate-reference-system', test_data)
            response_body = json.loads(response.content)
            assert response.status_code == 200
            assert len(response_body["searchResults"]["results"]) == 1

    def test_check_points_in_aou(self):
        with open(f'{self.path}v3/CheckPointsInAou.json') as test_data_file:
            test_data = test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT)
            test_data_obj = json.loads(test_data)

            response = self.client.make_request('POST', f'/api/crs/catalog/v3/points-in-aou', test_data)
            response_body = json.loads(response.content)
            assert response.status_code == 200
            assert len(response_body["successfulPoints"]) == 1
            assert len(response_body["failedPoints"]) == 3
            assert response_body["successfulPoints"][0]["latitude"] == 16.5
            assert response_body["successfulPoints"][0]["longitude"] == 35.8
            assert response_body["failedPoints"][0]["point"]["latitude"] == 16.0
            assert response_body["failedPoints"][0]["point"]["longitude"] == 34.0
            assert response_body["failedPoints"][0]["approximateKmDistanceOutside"] == 69.67
            assert response_body["failedPoints"][0]["degreeDistanceOutside"] == 0.63
            assert response_body["failedPoints"][1]["point"]["latitude"] == 16.5
            assert response_body["failedPoints"][1]["point"]["longitude"] == 34.1
            assert response_body["failedPoints"][1]["approximateKmDistanceOutside"] == 45.335
            assert response_body["failedPoints"][1]["degreeDistanceOutside"] == 0.41
            assert response_body["failedPoints"][2]["point"]["latitude"] == 80.1
            assert response_body["failedPoints"][2]["point"]["longitude"] == 90.0
            assert response_body["failedPoints"][2]["approximateKmDistanceOutside"] == 6519.922
            assert response_body["failedPoints"][2]["degreeDistanceOutside"] == 58.964
            assert response_body["maxDistKmOutside"] == 6519.922


    @classmethod
    def tearDownClass(cls):
        # Delete legal tag
        legal_tag_delete_response = cls.client.make_request('DELETE', f'/api/legal/v1/legaltags/{legal_tag_name}')
        if legal_tag_delete_response.status_code != 204:
            raise Exception(f"Could not delete legal tag on teardown. Received {legal_tag_delete_response.status_code} from legal service")

        # Delete records
        with open(f'{cls.path}v3/CRSAndCTRecords.json') as records_file:
            records = json.loads(records_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))
            for record in records:
                storage_delete_response = cls.client.make_request('DELETE', f'/api/storage/v2/records/{record["id"]}')
                if storage_delete_response.status_code != 204:
                    raise Exception(f"Could not delete record on teardown. Received {storage_delete_response.status_code} from storage service")
