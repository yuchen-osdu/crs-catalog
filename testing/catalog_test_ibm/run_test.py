import sys
sys.path.append("..")
from dotenv import load_dotenv
load_dotenv()

from catalog_test_core.test_crs_catalog_v2 import *

if __name__ == '__main__':
    unittest.main()
