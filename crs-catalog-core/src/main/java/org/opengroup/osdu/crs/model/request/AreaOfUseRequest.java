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
import org.opengroup.osdu.crs.model.AreaOfUseEssenceImpl;

/**
 *  AreaOfUseRequest is class which encapsulates the request body for an area of use object.
 */
public class AreaOfUseRequest {

	@JsonProperty("essence")
	private AreaOfUseEssenceImpl areaOfUseEssence;

	@JsonProperty("persistableReference")
	private String persistableReference;

	/**
	 * Constructor
	 */
    public AreaOfUseRequest()
    {
    }
	/**
	 * Constructor
	 * @param areaOfUseEssence core essence
	 * @param persistableReference persistable reference string
	 */
	public AreaOfUseRequest(AreaOfUseEssenceImpl areaOfUseEssence, String persistableReference){
		this.areaOfUseEssence = areaOfUseEssence;
		this.persistableReference = persistableReference;
	}

	/**
	 * Constructor
	 */
    public AreaOfUseRequest(AreaOfUseEssenceImpl areaOfUseEssence)
    {
		this.areaOfUseEssence = areaOfUseEssence;
    }

	/**
	 * get the area of use essence
	 * @return the area of use essence
	 */
	public AreaOfUseEssenceImpl getAreaOfUseEssence() {
		return areaOfUseEssence;
	}

	/**
	 * Get the persistable reference string as passed through the request
	 * @return the persistable reference string
	 */
	public String getPersistableReference() { return this.persistableReference; }
}

