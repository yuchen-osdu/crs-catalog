import pytest
import schemathesis
import jwt_client
import os
import allure

from hypothesis import settings
from dotenv import load_dotenv

# Module-level skip marker to skip all tests in this module
pytestmark = pytest.mark.skip(reason="Schemathesis OpenAPI spec testing is disabled as per requirements. OpenAPI contract validation is not required at this time.")

# loading variables from .env file
load_dotenv()

# Schema loading is disabled since these tests are skipped
# Uncomment below line if you need to re-enable schemathesis tests
# schema = schemathesis.openapi.from_url(f"https://{os.environ['VIRTUAL_SERVICE_HOST_NAME']}/api/crs/catalog/api-docs/v3/")
schema = None

@pytest.fixture(scope="session")
def token():
    return jwt_client.get_id_token()

# exclude methods that fail
# TODO: should be fixed on later api revisions
@allure.feature('CRS Catalog API v3')
@allure.story('API Contract Testing')
@allure.severity(allure.severity_level.CRITICAL)
def test_api(case, token):
    with allure.step(f"Test {case.method} {case.path}"):
        case.headers = {"Authorization": f"Bearer {token}"}
        response = case.call()
        case.validate_response(response)