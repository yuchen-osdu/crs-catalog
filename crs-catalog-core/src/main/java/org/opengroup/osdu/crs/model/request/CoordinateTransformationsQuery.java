// Copyright © Amazon
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http:#www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.crs.model.request;

import lombok.Data;
import org.opengroup.osdu.core.common.model.search.Point;
import org.opengroup.osdu.core.common.model.search.SpatialFilter;
import org.opengroup.osdu.crs.util.AppException;

import java.util.Arrays;
import java.util.List;

@Data
public class CoordinateTransformationsQuery implements ISearchQuery {
	private boolean includeHorizontal;
	private boolean includeVertical;
	private String codeSpace;
	private String name;
	private String sourceCRS;
	private String targetCRS;
	private Double latitude;
	private Double longitude;
	private boolean includeDeprecated;
	private Integer offset;
	private Integer limit;

	public String constructQuery(){
		checkRequiredFields();

		String query = String.format("data.CodeSpace: %s", codeSpace);

		query = buildQuerySearch(query, "data.Name", name);
		query = buildQuerySearch(query, "data.SourceCRS.SourceCRSID", sourceCRS);
		query = buildQuerySearch(query, "data.TargetCRS.TargetCRSID", targetCRS);

		if(includeVertical && includeHorizontal){
			throw AppException.createBadRequest("Cannot have both includeVertical and includeHorizontal set to true");
		}
		if(includeVertical) {
			query = String.format("%s AND (data.SourceCRS.SourceCRSID: \"osdu:reference-data--CoordinateReferenceSystem:Vertical:%s::*:\")"
				, query, codeSpace);
		} else if(includeHorizontal) {
			query = String.format("%s AND NOT (data.SourceCRS.SourceCRSID: \"osdu:reference-data--CoordinateReferenceSystem:Vertical:%s::*:\")"
					, query, codeSpace);
		}

		return query;
	}

	public SpatialFilter constructSpatialFilter(){
		SpatialFilter spatialFilter = null;

		if(latitude != null && longitude != null) {
			spatialFilter = new SpatialFilter();
			spatialFilter.setField("data.Wgs84Coordinates");
			SpatialFilter.ByWithinPolygon byWithinPolygon = new SpatialFilter.ByWithinPolygon();
			List<Point> points = Arrays.asList(new Point(latitude, longitude));
			byWithinPolygon.setPoints(points);
			spatialFilter.setByWithinPolygon(byWithinPolygon);
		} else if((latitude != null && longitude == null) || (latitude == null && longitude != null)){
			throw AppException.createBadRequest(String.format("Must supply both latitude and longitude when specifying either one"));
		}

		return spatialFilter;
	}

	private void checkRequiredFields(){
		if (codeSpace == null){
			throw AppException.createBadRequest(String.format("Missing required fields codeSpace"));
		}
	}

	private String buildQueryExactSearch(String query, String recordBodyAttrName, Object attribute){
		if(attribute != null){
			query = String.format("%s AND (%s: \"%s\")", query, recordBodyAttrName, attribute);
		}
		return query;
	}

	private String buildQuerySearch(String query, String recordBodyAttrName, Object attribute){
		if(attribute != null){
			query = String.format("%s AND (%s: \"%s\"*)", query, recordBodyAttrName, attribute);
		}
		return query;
	}
}
