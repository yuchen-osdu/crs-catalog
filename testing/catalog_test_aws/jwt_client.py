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

import os
import boto3
import jwt


# export AWS_REGION=us-east-1
# export AWS_COGNITO_CLIENT_ID=1frjn6q9a5itekric4ubcjivi8
# export AWS_COGNITO_AUTH_FLOW=USER_PASSWORD_AUTH
# export AWS_COGNITO_AUTH_PARAMS_USER=admin@testing.com
# export AWS_COGNITO_AUTH_PARAMS_PASSWORD=Osdu123123@
# export VIRTUAL_SERVICE_HOST_NAME=suttonsp.dev.osdu.aws
# export MY_TENANT=opendes
#
# export ACL_DOMAIN=opendes.example.com
def get_id_token():
    region = os.getenv("AWS_COGNITO_REGION")
    if region:
        client = boto3.client('cognito-idp', region_name=region)
    else:
        client = boto3.client('cognito-idp',
                              region_name=os.environ["AWS_REGION"])
    userAuth = client.initiate_auth(
            ClientId=os.environ['AWS_COGNITO_CLIENT_ID'],
            AuthFlow=os.environ['AWS_COGNITO_AUTH_FLOW'],
            AuthParameters={
                "USERNAME": os.environ['AWS_COGNITO_AUTH_PARAMS_USER'],
                "PASSWORD": os.environ['AWS_COGNITO_AUTH_PARAMS_PASSWORD']
            })

    return userAuth['AuthenticationResult']['AccessToken']


def get_invalid_token():
    # generate a dummy jwt
    return jwt.encode({'some': 'payload'}, 'secret', algorithm='HS256').decode("utf-8")
