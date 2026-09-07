#!/usr/bin/env python3
#
# Copyright 2020-2026 Google LLC
# Copyright 2020-2026 EPAM Systems, Inc
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""CIMPL Search health gate used before cimpl-acceptance-test-python."""

import json
import sys
import time

import constants
import jwt_client
from v3.HttpClient import HttpClient

MAX_ATTEMPTS = 3
SLEEP_SECONDS = 10


def main() -> int:
    if not constants.ROOT_URL:
        print("ERROR: VIRTUAL_SERVICE_HOST_NAME is not set", file=sys.stderr)
        return 1

    kind = f"{constants.SCHEMA_AUTHORITY}:wks:reference-data--Coordinate*:1.1.0"
    payload = json.dumps({
        "kind": kind,
        "limit": 1,
        "returnedFields": ["id"],
    })

    client = HttpClient(constants.ROOT_URL, constants.MY_TENANT, jwt_client)
    print(f"Checking Search query_with_cursor for kind={kind} on {constants.ROOT_URL}")

    last_status = None
    last_body = ""
    for attempt in range(1, MAX_ATTEMPTS + 1):
        response = client.make_request("POST", "/api/search/v2/query_with_cursor", payload)
        last_status = response.status_code
        last_body = (response.content or b"").decode("utf-8", "ignore")[:500]
        if response.status_code == 200:
            print(f"Search OK on attempt {attempt} (HTTP 200)")
            return 0
        print(
            f"attempt {attempt}/{MAX_ATTEMPTS}: Search returned HTTP {response.status_code}: {last_body}",
            file=sys.stderr,
        )
        if attempt < MAX_ATTEMPTS:
            time.sleep(SLEEP_SECONDS)

    print(
        f"ERROR: Search unhealthy after {MAX_ATTEMPTS} attempts "
        f"(last HTTP {last_status}): {last_body}",
        file=sys.stderr,
    )
    return 1


if __name__ == "__main__":
    sys.exit(main())
