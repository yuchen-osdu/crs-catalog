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

import requests

class HttpClient(object):

    def __init__(self, root_url, data_partition_id, jwt_client):
        self.data_partition_id = data_partition_id
        self.jwt_client = jwt_client
        self.bearer_token = self.jwt_client.get_id_token()
        self.unauth_retries = 0
        self.root_url = root_url

    def make_request(self, method: str, path: str, data = ''):

        if 'localhost' in self.root_url:
            url = 'http://' + self.root_url + path
        else:
            url = 'https://' + self.root_url + path

        if 'Bearer ' not in self.bearer_token:
            self.bearer_token = f"Bearer {self.bearer_token}"

        headers = {
            'content-type': 'application/json',
            'data-partition-id': self.data_partition_id,
            'Authorization': self.bearer_token
        }

        response = object

        if (method == 'GET'):
            response = requests.get(url=url, headers=headers, verify=False)
        elif (method == 'DELETE'):
            response = requests.delete(url=url, headers=headers, verify=False)
        elif (method == 'POST'):
            response = requests.post(url=url, data=data, headers=headers, verify=False)
        elif (method == 'PUT'):
            response = requests.put(url=url, data=data, headers=headers, verify=False)

        if (response.status_code == 401 or response.status_code == 403) and self.unauth_retries < 1:
            self.bearer_token = self.jwt_client.get_id_token()
            self.unauth_retries += 1
            self.make_request(method, data)

        self.unauth_retries = 0

        return response