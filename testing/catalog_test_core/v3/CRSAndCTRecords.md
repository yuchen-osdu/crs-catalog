# CRSAndCTRecords.json

Documents the reference-data record IDs and payloads used by v3 acceptance tests (EPSG CRS/CT codes from [Data Definitions manifests](https://community.opengroup.org/osdu/data/data-definitions)).

Tests **GET** each record from Storage first. Missing records are **uploaded once** so the suite can run; existing reference data is left unchanged. Tests **never DELETE** reference records on teardown (see issue #141).
