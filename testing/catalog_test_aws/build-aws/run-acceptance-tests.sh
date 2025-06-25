# Copyright © 2020 Amazon Web Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

CUR_DIR=$(pwd)
SCRIPT_SOURCE_DIR=$(dirname "$0")
echo "Script source location"
echo "$SCRIPT_SOURCE_DIR"
cd $SCRIPT_SOURCE_DIR

# Required variables
export ENTITLEMENTS_DOMAIN=$DOMAIN
export VIRTUAL_SERVICE_HOST_NAME=$CRS_CATALOG_HOST
export MY_TENANT=int-test-crs-catalog
export SCHEMA_AUTHORITY=osdu

export AWS_COGNITO_AUTH_FLOW="USER_PASSWORD_AUTH"
export PRIVILEGED_USER_TOKEN=$(aws cognito-idp initiate-auth --region ${AWS_REGION} --auth-flow ${AWS_COGNITO_AUTH_FLOW} --client-id ${AWS_COGNITO_CLIENT_ID} --auth-parameters "{\"USERNAME\":\"${ADMIN_USER}\",\"PASSWORD\":\"${ADMIN_PASSWORD}\"}" --query AuthenticationResult.AccessToken --output text)

python3 -m pip install --upgrade pip

python3 -m venv env
source env/bin/activate
python3 -m pip install -r v2/requirements.txt
pip install xmlrunner==1.7.7

export API_VER="v2"
echo ***RUNNING CATALOG API $API_VER TESTS***
python3 run_test_v2.py
TEST_STATUS_V2=$?
echo ***FINISHED CATALOG API $API_VER TESTS***
echo "TEST STATUS V2: $TEST_STATUS_V2"

python3 -m pip install -r v3/requirements.txt
export API_VER="v3"
echo ***RUNNING CATALOG API $API_VER TESTS***
python3 run_test_v3.py
TEST_STATUS_V3=$?
echo ***FINISHED CATALOG API $API_VER TESTS***
echo "TEST STATUS V3: $TEST_STATUS_V3"

echo "Copying XML files to test-reports folder"
cp *.xml $CUR_DIR/test-reports

deactivate
rm -rf env/

cd $CUR_DIR

if [ $TEST_STATUS_V2 -ne 0 ] || [ $TEST_STATUS_V3 -ne 0 ]
then
  exit 1
else
  exit 0
fi
