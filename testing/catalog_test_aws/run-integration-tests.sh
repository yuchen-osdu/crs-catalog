# Copyright © 2020 Amazon Web Services
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

if [[ "$OSTYPE" == "msys" ]]; then
  python -m pip install --upgrade pip
  python -m pip install --user virtualenv
  python -m venv env
  source env/Scripts/activate
  python -m pip install --upgrade pip
  python -m pip install -r requirements.txt

  # Run tests
  echo ***RUNNING CRS Catalog API V2 TESTS***
  python run_test_api_v2.py
  V2_TEST_STATUS=$?
  echo ***FINISHED CRS Catalog API V2 TESTS***

  echo ***RUNNING CRS Catalog API V3 TESTS***
  python run_test_api_v3.py
  V3_TEST_STATUS=$?
  echo ***FINISHED CRS Catalog API V3 TESTS***

  # python -m pip freeze > requirements.txt
  python -m pip uninstall -r requirements.txt -y

else

  echo "Base python path:"
  which python3
  python3 -m venv env
  # sed -i 's/$1/${1:-}/' env/bin/activate # Fix deactivation bug '$1 unbound variable'
  deactivate || echo "No python virtual environment in use."
  source env/bin/activate
  echo "Used python path:"
  which python3
  python3 -m pip install --upgrade pip
  python3 -m pip install -r requirements.txt

  # Run tests
  echo ***RUNNING CRS Catalog API V2 TESTS***
  python3 run_test_api_v2.py
  V2_TEST_STATUS=$?
  echo ***FINISHED CRS Catalog API V2 TESTS***

  # echo ***RUNNING CRS Catalog API V3 TESTS***
  # python3 run_test_api_v3.py
  # V3_TEST_STATUS=$?
  # echo ***FINISHED CRS Catalog API V3 TESTS***
  echo "Skipping CRS Catalog API V3 test because of this issue with Indexer Service: https://community.opengroup.org/osdu/platform/system/indexer-service/-/issues/185"

  # python3 -m pip freeze > requirements.txt
  python3 -m pip uninstall -r requirements.txt -y

fi

deactivate
rm -rf env/

if [ $V2_TEST_STATUS -ne 0 ] || [ $V3_TEST_STATUS -ne 0 ]
then
    exit 1
fi
