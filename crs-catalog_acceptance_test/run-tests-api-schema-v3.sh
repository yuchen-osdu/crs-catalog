#!/bin/bash -eu
#
#  Copyright 2020-2024 Google LLC
#  Copyright 2020-2024 EPAM Systems, Inc
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

#!/usr/bin/env bash

python3 -m pip install --upgrade pip

python3 -m venv env
source env/bin/activate

python3 -m pip install -r requirements.txt

echo ""
export API_VER="v3"
echo ***RUNNING CATALOG API $API_VER SCHEMA TESTS***
pytest -v test_api_v3.py
TEST_STATUS=$?
echo ***FINISHED CATALOG API $API_VER SCHEMA TESTS***

echo "TEST STATUS: $TEST_STATUS"

deactivate
rm -rf env/

if [ $TEST_STATUS -ne 0 ]
then
    exit 1
fi
