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
 *
 */
package org.opengroup.osdu.crs.model.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.opengroup.osdu.crs.model.EarlyBoundCRSEssenceImpl;

/**
 *  EarlyBoundCRSRequest is class which encapsulates the request body for a EarlyBoundCRS object.
 */
public class EarlyBoundCRSRequest {
	@JsonProperty("essence")
	private EarlyBoundCRSEssenceImpl earlyBoundCRSEssence;
	@JsonProperty("persistableReference")
	private String persistableReference;
	/**
	 * Constructor
	 */
    public EarlyBoundCRSRequest()
    {
    }

	/**
	 * Constructor
	 */
    public EarlyBoundCRSRequest(EarlyBoundCRSEssenceImpl earlyBoundCRSEssence)
    {
		this.earlyBoundCRSEssence = earlyBoundCRSEssence;
    }

	/**
	 * Constructor
	 * @param earlyBoundCRSEssence the {@link EarlyBoundCRSEssenceImpl} (can be null, then persistableReference must be defined)
	 * @param persistableReference the persistable reference (can be null, then earlyBoundCRSEssence must be non-null)
	 */
	public EarlyBoundCRSRequest(EarlyBoundCRSEssenceImpl earlyBoundCRSEssence, String persistableReference) {
		this.earlyBoundCRSEssence = earlyBoundCRSEssence;
		this.persistableReference = persistableReference;
	}
	/**
	 * get the EarlyBoundCRS essence
	 * @return the EarlyBoundCRS essence
	 */
	public EarlyBoundCRSEssenceImpl getEarlyBoundCRSEssence() {
		return this.earlyBoundCRSEssence;
	}

	/**
	 * Get the persistable reference string as passed through the request
	 * @return the persistable reference string
	 */
	public String getPersistableReference() { return this.persistableReference; }
}

