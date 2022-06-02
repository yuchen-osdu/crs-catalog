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
public class CoordinateReferenceSystemsQuery implements ISearchQuery {
	private String codeSpace;
	private String name;
	private String id;
	private String code;
	private String kind;
	private String coordinateReferenceSystemType;
	private boolean returnBoundProjectedAndProjectedBasedOnWgs84;
	private boolean returnBoundGeographic2DAndWgs84;
	private BaseCRS baseCRS;
	private Datum datum;
	private Extent extent;
	private String persistableReferenceSearch;
	private String horizontalAxisUnitId;
	private String verticalAxisUnitId;
	private Double latitude;
	private Double longitude;
	private Integer offset;
	private Integer limit;

	public String constructQuery(){
		String query = initQuery();
		query = buildQueryComplexUseCases(query);
		query = buildQuerySimpleUseCases(query);
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
			throw AppException.createBadRequest("Must supply both latitude and longitude when specifying either one");
		}

		return spatialFilter;
	}

	private String initQuery(){
		if (codeSpace == null){
			return "";
		}

		String query = String.format("(data.CodeSpace: %s)", codeSpace);

		if(returnBoundProjectedAndProjectedBasedOnWgs84 || returnBoundGeographic2DAndWgs84){
			query = String.format("(data.CodeSpace: %s OR data.Codespace: EPSG OR data.PreferredUsage.Extent.AuthorityCode.Authority: EPSG)", codeSpace);
		}

		return query;
	}

	private String buildQueryComplexUseCases(String query){
		if(code != null && (!returnBoundProjectedAndProjectedBasedOnWgs84 && !returnBoundGeographic2DAndWgs84)) {
			query = buildQueryExactSearch(query, "data.Code", code);
		} else if (code != null && (returnBoundProjectedAndProjectedBasedOnWgs84 || returnBoundGeographic2DAndWgs84)) {
			query = appendQuery(query, String.format("(data.Code: %s OR data.Code: 4326)", code));
		} else if (code == null && (returnBoundProjectedAndProjectedBasedOnWgs84 || returnBoundGeographic2DAndWgs84)) {
			query = appendQuery(query, "(data.Code: 4326 OR data.PreferredUsage.Extent.AuthorityCode.Authority: EPSG)");
		}

		if(returnBoundProjectedAndProjectedBasedOnWgs84 && returnBoundGeographic2DAndWgs84){
			query = appendQuery(query, "(data.Kind: BoundGeographic2d OR data.Kind: BoundProjected)");
		} else if(returnBoundProjectedAndProjectedBasedOnWgs84) {
			query = appendQuery(query, "(data.Kind: BoundProjected)");
		} else if(returnBoundGeographic2DAndWgs84) {
			query = appendQuery(query, "(data.Kind: BoundGeographic2d)");
		}

		return query;
	}

	private String buildQuerySimpleUseCases(String query){
		query = buildQuerySearch(query, "data.Name", name);
		query = buildQuerySearch(query, "data.ID", id);
		query = buildQuerySearch(query, "data.Kind", kind);
		query = buildQuerySearch(query, "data.CoordinateReferenceSystemType", coordinateReferenceSystemType);
		query = buildQuerySearch(query, "data.CoordinateSystem.HorizontalAxisUnitID", horizontalAxisUnitId);
		query = buildQuerySearch(query, "data.CoordinateSystem.VerticalAxisUnitID", verticalAxisUnitId);

		if(baseCRS != null) {
			query = buildQuerySearch(query, "data.BaseCRS.BaseCRSID", baseCRS.getId());
			query = buildQuerySearch(query, "data.BaseCRS.Name", baseCRS.getName());
		}

		if(datum != null) {
			query = buildQuerySearch(query, "data.Datum.Name", datum.getName());
			query = buildQueryExactSearch(query, "data.Datum.AuthorityCode.Authority", datum.getCodeSpace());
			query = buildQueryExactSearch(query, "data.Datum.AuthorityCode.Code", datum.getCode());
		}

		if(extent != null) {
			query = buildQuerySearch(query, "data.PreferredUsage.Extent.Name", extent.getName());
		}

		return query;
	}

	private String buildQueryExactSearch(String query, String recordBodyAttrName, Object attribute){
		if (attribute != null) {
			query = appendQuery(query, String.format("(%s: \"%s\")", recordBodyAttrName, attribute));
		}
		return query;
	}

	private String buildQuerySearch(String query, String recordBodyAttrName, Object attribute){
		if (attribute != null) {
			query = appendQuery(query, String.format("(%s: \"%s\")", recordBodyAttrName, attribute));
		}
		return query;
	}

	private String appendQuery(String query, String toAppend){
		if (query.isEmpty()) {
			return toAppend;
		} else {
			return String.format("%s AND %s", query, toAppend);
		}
	}
}
