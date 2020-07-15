// Copyright 2017-2019, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Copyright 2018 Schlumberger. All Rights Reserved.
 */
package org.opengroup.osdu.crs.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.opengroup.osdu.crs.model.CRSEssenceImpl;

/**
 *  CRSRequest is class which encapsulates the request body for a CRS object.
 */
public class CRSRequest {
    @JsonProperty("essence")
    private CRSEssenceImpl crsEssenceJson;

    @JsonProperty("persistableReference")
    private String persistableReference;

    /**
     * Constructor
     */
    public CRSRequest() {
    }

    /**
     * Constructor
     */
    public CRSRequest(CRSEssenceImpl crsEssenceJson) {
        this.crsEssenceJson = crsEssenceJson;
    }

    /**
     * Completely initializing constructor
     * @param crsEssenceJson JSON essence string
     * @param persistableReference persistable reference string
     */
    public CRSRequest(CRSEssenceImpl crsEssenceJson, String persistableReference) {
        this.crsEssenceJson = crsEssenceJson;
        this.persistableReference = persistableReference;
    }

    /**
     * get the json representation of the CRS essence
     * @return the json representation of the CRS essence as {@link CRSEssenceImpl}
     */
    public CRSEssenceImpl getCRSEssenceJson() {
        return crsEssenceJson;
    }

    /**
     * Get the persistable reference string as passed through the request
     * @return the persistable reference string
     */
    public String getPersistableReference() {
        return this.persistableReference;
    }
}

