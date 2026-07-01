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
import json
import time

urllib3.disable_warnings()


def main(argv):
    pass


if __name__ == '__main__':
    main(sys.argv)

# coding: utf-8
"""Integration unit tests for crs-catalog service V3"""
import unittest
import allure
import jwt_client
import constants as constants
from v3.HttpClient import HttpClient

legal_tag_name = "crs-catalog-int-test-legaltag"
v3_path = '/api/crs/catalog/v3'
ct_endpoint_path = f'{v3_path}/coordinate-transformation'
crs_endpoint_path = f'{v3_path}/coordinate-reference-system'
point_in_aou_endpoint_path = f'{v3_path}/points-in-aou'
info_endpoint_path = f'{v3_path}/info'
record_id_set = set()


@allure.feature('CRS Catalog v3')
@allure.epic('CRS Catalog v3 Integration Tests')
class TestCrsCatalog(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.path = './'
        if constants.LOCAL_MODE == "true":
            cls.path = './'

        cls.client = HttpClient(constants.ROOT_URL, constants.MY_TENANT, jwt_client)

        # Make sure schemas are made
        with open(f'{cls.path}v3/CoordinateReferenceSystemSchema.json') as crs_schema_file:
            crs_schema = crs_schema_file.read().replace('{{data_partition_id}}', constants.MY_TENANT).replace(
                '{{schema-authority}}', constants.SCHEMA_AUTHORITY)
            schema_response = cls.client.make_request('POST', '/api/schema-service/v1/schema', crs_schema)
            if schema_response.status_code != 201 and (
                    schema_response.status_code != 400 and 'Schema Id is already present' not in schema_response.content.decode(
                "utf-8", "ignore")):
                raise Exception(
                    f"Could not create CRS schema. Received {schema_response.status_code} from schema service")

        with open(f'{cls.path}v3/CoordinateTransformationSchema.json') as ct_schema_file:
            ct_schema = ct_schema_file.read().replace('{{data_partition_id}}', constants.MY_TENANT).replace(
                '{{schema-authority}}', constants.SCHEMA_AUTHORITY)
            schema_response = cls.client.make_request('POST', '/api/schema-service/v1/schema', ct_schema)
            if schema_response.status_code != 201 and (
                    schema_response.status_code != 400 and 'Schema Id is already present' not in schema_response.content.decode(
                "utf-8", "ignore")):
                raise Exception(
                    f"Could not create CT schema. Received {schema_response.status_code} from schema service")

        # Make sure legal tag is made
        with open(f'{cls.path}v3/LegalTag.json') as legal_tag_file:
            legal_tag = legal_tag_file.read().replace('{{legal_tag_name}}', legal_tag_name)
            legal_response = cls.client.make_request('POST', '/api/legal/v1/legaltags', legal_tag)
            if legal_response.status_code not in [201, 409]:
                raise Exception(f"Could not create legal tag. Received {legal_response.status_code} from legal service")

        # Create records
        with open(f'{cls.path}v3/CRSAndCTRecords.json') as records_file:
            records = records_file.read().replace('{{data_partition_id}}', constants.MY_TENANT) \
                .replace('{{acl_domain}}', constants.MY_TENANT + '.' + constants.ENTITLEMENTS_DOMAIN) \
                .replace('{{schema-authority}}', constants.SCHEMA_AUTHORITY)
            records_obj = json.loads(records)

            for record in records_obj:
                record_id_set.add(record["id"])

            print('Request URL for upsert records: ' + cls.client.root_url + '/api/storage/v2/records')
            storage_response = cls.client.make_request('PUT', '/api/storage/v2/records', records)
            if storage_response.status_code != 201:
                raise Exception(
                    f"Could not create records. Received {storage_response.status_code} : {storage_response.reason} from storage service")

            # check that records show up in search before continuing
            found = False
            max_checks = 20
            checks = 0
            while not found:
                if checks >= max_checks:
                    raise Exception(f"Could not find records indexed against search after {max_checks} checks")

                search_response_id_set = cls.client.get_all_ids_of_kind(f"{constants.SCHEMA_AUTHORITY}:wks:reference-data--Coordinate*:1.1.0")

                if record_id_set.issubset(search_response_id_set):
                    found = True
                else:
                    checks += 1
                    print(f"Didn't find all records after {checks} checks. Waiting a bit to let records index")
                    time.sleep(5)

            # Some timing issues still persist without waiting a bit longer
            time.sleep(10)

    @staticmethod
    def check_get_search_response_count(response, expected_count, test_name):
        response_body = json.loads(response.content)
        assert response.status_code == 200
        response_count = len(response_body["searchResults"]["results"])

        for test_response in response_body["searchResults"]["results"]:
            if test_response["id"] not in record_id_set:
                response_count -= 1

        if response_count != expected_count:
            print(f'Error: Test {test_name} Expects {expected_count} records. Got {response_count} records.')
        assert response_count == expected_count

    @staticmethod
    def check_search_response_count(response, expected_count, test_name):
        response_body = json.loads(response.content)
        assert response.status_code == 200

        response_count = len(response_body["cursorSearchResults"]["results"])

        for test_response in response_body["cursorSearchResults"]["results"]:
            if test_response["id"] not in record_id_set:
                response_count -= 1

        if response_count != expected_count:
            print(f'Error: Test {test_name} Expects {expected_count} records. Got {response_count} records.')
        assert response_count == expected_count

    def test_get_coordinate_transformation_dataId(self):
        with open(f'{self.path}v3/GetCoordinateTransformationTestData.json') as test_data_file:
            test_data = json.loads(test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))
            response = self.client.make_request('GET', f'{ct_endpoint_path}?dataId={test_data["dataId"]}')
            response_body = json.loads(response.content)

            self.check_get_search_response_count(response, 1, "test_get_coordinate_transformation_dataId")
            assert response_body["searchResults"]["results"][0]["id"] == test_data["recordId"]
            assert response_body["searchResults"]["results"][0]["data"]["ID"] == test_data["dataId"]
            for record_property in ('kind', 'version', 'acl', 'legal', 'namespace'):
                assert record_property in response_body["searchResults"]["results"][0]
            for record_property in ('Code', 'Kind', 'PreferredUsage.Name', 'PreferredUsage.Extent.Description',
                                    'PreferredUsage.Extent.Name', 'PersistableReference',
                                    'CoordinateTransformationType'):
                assert record_property in response_body["searchResults"]["results"][0]['data']

    def test_get_coordinate_transformation_recordId(self):
        with open(f'{self.path}v3/GetCoordinateTransformationTestData.json') as test_data_file:
            test_data = json.loads(test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))
            response = self.client.make_request('GET', f'{ct_endpoint_path}?recordId={test_data["recordId"]}')
            response_body = json.loads(response.content)
            self.check_get_search_response_count(response, 1, "test_get_coordinate_transformation_recordId")
            assert response_body["searchResults"]["results"][0]["id"] == test_data["recordId"]
            assert response_body["searchResults"]["results"][0]["data"]["ID"] == test_data["dataId"]

    def test_get_coordinate_transformation_dataId_recordId(self):
        with open(f'{self.path}v3/GetCoordinateTransformationTestData.json') as test_data_file:
            test_data = json.loads(test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))
            response = self.client.make_request('GET',
                                                f'{ct_endpoint_path}?dataId={test_data["dataId"]}&recordId={test_data["recordId"]}')
            response_body = json.loads(response.content)
            self.check_get_search_response_count(response, 1, "test_get_coordinate_transformation_dataId_recordId")
            assert response_body["searchResults"]["results"][0]["id"] == test_data["recordId"]
            assert response_body["searchResults"]["results"][0]["data"]["ID"] == test_data["dataId"]

    def test_get_coordinate_reference_system_dataId(self):
        with open(f'{self.path}v3/GetCoordinateReferenceSystemTestData.json') as test_data_file:
            test_data = json.loads(test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))
            response = self.client.make_request('GET', f'{crs_endpoint_path}?dataId={test_data["dataId"]}')
            response_body = json.loads(response.content)

            self.check_get_search_response_count(response, 1, "test_get_coordinate_reference_system_dataId")
            assert response_body["searchResults"]["results"][0]["id"] == test_data["recordId"]
            assert response_body["searchResults"]["results"][0]["data"]["ID"] == test_data["dataId"]
            for record_property in ('kind', 'version', 'acl', 'legal', 'namespace'):
                assert record_property in response_body["searchResults"]["results"][0]
            for record_property in (
                    'Code', 'Kind', 'VerticalCRS.Name', 'PreferredUsage.Name', 'PreferredUsage.Extent.Description',
                    'PreferredUsage.Extent.Name', 'PersistableReference', 'CoordinateReferenceSystemType'):
                assert record_property in response_body["searchResults"]["results"][0]['data']

    def test_get_coordinate_reference_system_recordId(self):
        with open(f'{self.path}v3/GetCoordinateReferenceSystemTestData.json') as test_data_file:
            test_data = json.loads(test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))
            response = self.client.make_request('GET', f'{crs_endpoint_path}?recordId={test_data["recordId"]}')
            response_body = json.loads(response.content)
            self.check_get_search_response_count(response, 1, "test_get_coordinate_reference_system_recordId")
            assert response_body["searchResults"]["results"][0]["id"] == test_data["recordId"]
            assert response_body["searchResults"]["results"][0]["data"]["ID"] == test_data["dataId"]

    def test_get_coordinate_reference_system_dataId_recordId(self):
        with open(f'{self.path}v3/GetCoordinateReferenceSystemTestData.json') as test_data_file:
            test_data = json.loads(test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT))
            response = self.client.make_request('GET',
                                                f'{crs_endpoint_path}?dataId={test_data["dataId"]}&recordId={test_data["recordId"]}')
            response_body = json.loads(response.content)
            self.check_get_search_response_count(response, 1, "test_get_coordinate_reference_system_dataId_recordId")
            assert response_body["searchResults"]["results"][0]["id"] == test_data["recordId"]
            assert response_body["searchResults"]["results"][0]["data"]["ID"] == test_data["dataId"]

    def test_search_coordinate_transformations(self):
        with open(f'{self.path}v3/SearchCoordinateTransformations.json') as test_data_file:
            test_data = test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT)
            response = self.client.make_request('POST', ct_endpoint_path, test_data)
            self.check_search_response_count(response, 1, "test_search_coordinate_transformations")

    def test_search_coordinate_transformations_with_name(self):
        with open(f'{self.path}v3/SearchCoordinateTransformationsWithName.json') as test_data:
            response = self.client.make_request('POST', ct_endpoint_path, test_data)
            self.check_search_response_count(response, 1, "test_search_coordinate_transformations_with_name")

    def test_search_coordinate_transformations_with_partial_name(self):
        with open(f'{self.path}v3/SearchCoordinateTransformationsWithPartialName.json') as test_data:
            response = self.client.make_request('POST', ct_endpoint_path, test_data)
            self.check_search_response_count(response, 1, "test_search_coordinate_transformations_with_partial_name")

    def test_search_coordinate_transformations_with_common_partial_name(self):
        with open(f'{self.path}v3/SearchCoordinateTransformationsWithCommonPartialName.json') as test_data:
            response = self.client.make_request('POST', ct_endpoint_path, test_data)
            self.check_search_response_count(response, 2,
                                             "test_search_coordinate_transformations_with_common_partial_name")

    def test_search_coordinate_transformations_with_wrong_name(self):
        with open(f'{self.path}v3/SearchCoordinateTransformationsWithWrongName.json') as test_data:
            response = self.client.make_request('POST', ct_endpoint_path, test_data)
            self.check_search_response_count(response, 0, "test_search_coordinate_transformations_with_wrong_name")

    def test_search_coordinate_transformations_with_reversed_source_and_target_crs(self):
        with open(f'{self.path}v3/SearchCoordinateTransformationsReverseSourceAndTarget.json') as test_data_file:
            test_data = test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT)
            response = self.client.make_request('POST', ct_endpoint_path, test_data)
            self.check_search_response_count(response, 1,
                                             "test_search_coordinate_transformations_with_reversed_source_and_target_crs")

    def test_search_coordinate_transformations_find_horizontal(self):
        test_data = "{}"
        response = self.client.make_request('POST', ct_endpoint_path, test_data)
        response_body = json.loads(response.content)
        self.check_search_response_count(response, 1, "test_search_coordinate_transformations_find_horizontal")
        expected_record_code_to_be_found_from_json_file = "1111"
        self._get_record_by_code(response_body["cursorSearchResults"]["results"],
                                 expected_record_code_to_be_found_from_json_file)

    def test_search_coordinate_transformations_find_vertical(self):
        with open(f'{self.path}v3/SearchCoordinateTransformationsVertical.json') as test_data_file:
            test_data = test_data_file.read()
            response = self.client.make_request('POST', ct_endpoint_path, test_data)
            response_body = json.loads(response.content)
            self.check_search_response_count(response, 1, "test_search_coordinate_transformations_find_vertical")
            assert response_body["cursorSearchResults"]["results"][0]["data"]["Code"] == "5429"
            for record_property in ('Code', 'Name', 'Kind', 'CodeSpace', 'PreferredUsage.Name',
                                    'PreferredUsage.Extent.Description', 'PreferredUsage.Extent.Name',
                                    'CoordinateTransformationType', 'SourceCRS.Name', 'SourceCRS.AuthorityCode.Code',
                                    'TargetCRS.Name', 'TargetCRS.AuthorityCode.Code'):
                assert record_property in response_body["cursorSearchResults"]["results"][0]['data']
            assert "InformationSource" not in response_body["cursorSearchResults"]["results"][0]['data']

    def test_search_coordinate_transformations_find_vertical_return_all_fields(self):
        with open(f'{self.path}v3/SearchCoordinateTransformationsVerticalReturnAllFields.json') as test_data_file:
            test_data = test_data_file.read()
            response = self.client.make_request('POST', ct_endpoint_path, test_data)
            response_body = json.loads(response.content)
            self.check_search_response_count(response, 1,
                                             "test_search_coordinate_transformations_find_vertical_return_all_fields")
            assert "InformationSource" in response_body["cursorSearchResults"]["results"][0]['data']

    def test_search_coordinate_transformations_find_all(self):
        with open(f'{self.path}v3/SearchCoordinateTransformationsAllKinds.json') as test_data_file:
            test_data = test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT)
            response = self.client.make_request('POST', ct_endpoint_path, test_data)
            self.check_search_response_count(response, 2, "test_search_coordinate_transformations_find_all")

    def test_search_coordinate_transformations_find_all_include_deprecated(self):
        with open(f'{self.path}v3/SearchCoordinateTransformationsIncludeDeprecated.json') as test_data_file:
            test_data = test_data_file.read()
            response = self.client.make_request('POST', ct_endpoint_path, test_data)
            self.check_search_response_count(response, 3,
                                             "test_search_coordinate_transformations_find_all_include_deprecated")

    def test_search_coordinate_reference_systems(self):
        with open(f'{self.path}v3/SearchCoordinateReferenceSystems.json') as test_data_file:
            test_data = test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT)
            response = self.client.make_request('POST', crs_endpoint_path, test_data)
            self.check_search_response_count(response, 1, "test_search_coordinate_reference_systems")

    def test_search_coordinate_reference_systems_partial(self):
        with open(f'{self.path}v3/SearchCoordinateReferenceSystemsPartial.json') as test_data_file:
            test_data = test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT)
            response = self.client.make_request('POST', crs_endpoint_path, test_data)
            self.check_search_response_count(response, 1, "test_search_coordinate_reference_systems_partial")

    def test_search_coordinate_reference_systems_bound_projected(self):
        with open(f'{self.path}v3/SearchCoordinateReferenceSystemsBoundProjected.json') as test_data:
            response = self.client.make_request('POST', crs_endpoint_path, test_data)

            self.check_search_response_count(response, 1, "test_search_coordinate_reference_systems_bound_projected")
            response_body = json.loads(response.content)
            expected_record_code_to_be_found_from_json_file = "24600001"
            record = self._get_record_by_code(response_body["cursorSearchResults"]["results"],
                                              expected_record_code_to_be_found_from_json_file)
            for record_property in ('Code', 'Name', 'Kind', 'CodeSpace', 'PreferredUsage.Name',
                                    'PreferredUsage.Extent.Description', 'PreferredUsage.Scope.Name',
                                    'CoordinateSystem.Name', 'Datum.Name', 'RevisionDate'):
                assert record_property in record['data']
            assert "InformationSource" not in response_body["cursorSearchResults"]["results"][0]['data']

    def test_search_coordinate_reference_systems_bound_projected_return_all_fields(self):
        with open(f'{self.path}v3/SearchCoordinateReferenceSystemsBoundProjectedReturnAllFields.json') as test_data:
            response = self.client.make_request('POST', crs_endpoint_path, test_data)
            self.check_search_response_count(response, 1,
                                             "test_search_coordinate_reference_systems_bound_projected_return_all_fields")
            response_body = json.loads(response.content)
            assert "InformationSource" in response_body["cursorSearchResults"]["results"][0]['data']

    def test_search_coordinate_reference_systems_bound_geographic2d(self):
        with open(f'{self.path}v3/SearchCoordinateReferenceSystemsBoundGeographic2D.json') as test_data:
            response = self.client.make_request('POST', crs_endpoint_path, test_data)
            self.check_search_response_count(response, 1, "test_search_coordinate_reference_systems_bound_geographic2d")


    def test_search_coordinate_reference_systems_find_all(self):
        with open(f'{self.path}v3/SearchCoordinateReferenceSystemsFindAll.json') as test_data_file:
            search_requests = json.loads(test_data_file.read())
        found_ids = set()
        for request_body in search_requests:
            response = self.client.make_request('POST', crs_endpoint_path, json.dumps(request_body))
            response_body = json.loads(response.content)
            assert response.status_code == 200
            for result in response_body["cursorSearchResults"]["results"]:
                if result["id"] in record_id_set:
                    found_ids.add(result["id"])
        if len(found_ids) != 5:
            print(f'Error: Test test_search_coordinate_reference_systems_find_all Expects 5 records. Got {len(found_ids)} records.')
        assert len(found_ids) == 5

    def test_check_points_in_aou(self):
        with open(f'{self.path}v3/CheckPointsInAou.json') as test_data_file:
            test_data = test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT)
            response = self.client.make_request('POST', point_in_aou_endpoint_path, test_data)
            response_body = json.loads(response.content)
            assert response.status_code == 200
            assert "bboxFailedPoints" in response_body
            failed_points = response_body["bboxFailedPoints"]
            assert len(failed_points) == 3
            assert failed_points[0]["point"]["latitude"] == 16.0
            assert failed_points[0]["point"]["longitude"] == 34.0
            assert failed_points[0]["index"] == 0
            assert failed_points[0]["approximateKmDistanceOutside"] == 68
            assert failed_points[1]["point"]["latitude"] == 16.5
            assert failed_points[1]["point"]["longitude"] == 34.1
            assert failed_points[1]["index"] == 1
            assert failed_points[1]["approximateKmDistanceOutside"] == 43
            assert failed_points[2]["point"]["latitude"] == 80.1
            assert failed_points[2]["point"]["longitude"] == 90.0
            assert failed_points[2]["index"] == 3
            assert failed_points[2]["approximateKmDistanceOutside"] == 5313
            assert response_body["maxDistKmOutsideBBox"] == 5313

    def test_check_points_in_aou_acrss_antimeridian(self):  # There will be multiple polygons
        with open(f'{self.path}v3/CheckPointsInAouAntimeridian.json') as test_data_file:
            test_data = test_data_file.read().replace('{{data_partition_id}}', constants.MY_TENANT)
            response = self.client.make_request('POST', point_in_aou_endpoint_path, test_data)
            response_body = json.loads(response.content)
            assert response.status_code == 200
            assert "bboxFailedPoints" in response_body
            failed_points = response_body["bboxFailedPoints"]
            assert len(failed_points) == 2
            assert failed_points[0]["index"] == 2
            assert failed_points[0]["approximateKmDistanceOutside"] == 63
            assert failed_points[1]["index"] == 3
            assert failed_points[1]["approximateKmDistanceOutside"] == 84
            assert response_body["maxDistKmOutsideBBox"] == 84

    def test_info(self):  # There will be multiple polygons
        response = self.client.make_request('GET', info_endpoint_path)
        response_body = json.loads(response.content)
        assert response.status_code == 200
        assert "connectedOuterServices" in response_body

    @classmethod
    def tearDownClass(cls):
        # Delete legal tag
        legal_tag_delete_response = cls.client.make_request('DELETE', f'/api/legal/v1/legaltags/{legal_tag_name}')
        if legal_tag_delete_response.status_code != 204:
            raise Exception(
                f"Could not delete legal tag on teardown. Received {legal_tag_delete_response.status_code} from legal service")

        # Delete records
        with open(f'{cls.path}v3/CRSAndCTRecords.json') as records_file:
            records = json.loads(records_file.read().replace('{{data_partition_id}}', constants.MY_TENANT).replace(
                '{{schema-authority}}', constants.SCHEMA_AUTHORITY))
            for record in records:
                storage_delete_response = cls.client.make_request('DELETE', f'/api/storage/v2/records/{record["id"]}')
                if storage_delete_response.status_code not in (204, 404):
                    raise Exception(
                        f"Could not delete record on teardown. Received {storage_delete_response.status_code} from storage service")
            record_id_set.clear()

    @staticmethod
    def _get_record_by_code(records_list, code):
        for record in records_list:
            if record["data"]["Code"] == code:
                return record
        assert False, f"No record found with code {code}"
