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
import org.opengroup.osdu.crs.model.LateBoundCRSEssenceImpl;

/**
 *  LateBoundCRSRequest is class which encapsulates the request body for an LateBoundCRS object.
 */
public class LateBoundCRSRequest {

	private LateBoundCRSEssenceImpl lateBoundCRSEssence;
	private String persistableReference;

	/**
	 * Constructor
	 */
    public LateBoundCRSRequest()
    {
    }

	/**
	 * Constructor
	 */
    public LateBoundCRSRequest(LateBoundCRSEssenceImpl lateBoundCRSEssence)
    {
		this.lateBoundCRSEssence = lateBoundCRSEssence;
    }
	/**
	 * Constructor
	 */
	public LateBoundCRSRequest(String persistableReference)
	{
		this.persistableReference = persistableReference;
	}
	/**
	 * Constructor
	 */
	public LateBoundCRSRequest(LateBoundCRSEssenceImpl lateBoundCRSEssence, String persistableReference)
	{
		this.lateBoundCRSEssence = lateBoundCRSEssence;
		this.persistableReference = persistableReference;
	}

	/**
	 * get the LateBoundCRS essence
	 * @return the LateBoundCRS essence
	 */
	@JsonProperty("essence")
	public LateBoundCRSEssenceImpl getLateBoundCRSEssence() {
		return lateBoundCRSEssence;
	}
	/**
	 * get the LateBoundCRS essence
	 * @return the LateBoundCRS essence
	 */
	@JsonProperty("persistableReference")
	public String getPersistableReference() {
		return persistableReference;
	}
}

