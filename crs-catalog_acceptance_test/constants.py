#!/usr/bin/python

import sys, os


def main(argv):
    pass


if __name__ == '__main__':
    main(sys.argv)

VENDOR = os.getenv("VENDOR")
BASE_URL = os.getenv("BASE_URL", '/api/crs/catalog/v2')
ROOT_URL = os.getenv("HOST_URL")
MY_TENANT = os.getenv("MY_TENANT", "osdu")
ACL_DOMAIN = os.getenv("ACL_DOMAIN", "osdu.example.com")
SCHEMA_AUTHORITY = os.getenv("SCHEMA_AUTHORITY", "osdu")
LOCAL_MODE = os.getenv("LOCAL_MODE", "false")

