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

package org.opengroup.osdu.crs.api;

import javax.inject.Inject;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.crs.logging.AuditLogger;
import org.opengroup.osdu.crs.model.request.CoordinateReferenceSystemsQuery;
import org.opengroup.osdu.crs.model.request.CoordinateTransformationsQuery;
import org.opengroup.osdu.crs.model.request.InPolygonQuery;
import org.opengroup.osdu.crs.model.response.PointsInAouSearchResult;
import org.opengroup.osdu.crs.model.response.SearchResponse;
import org.opengroup.osdu.crs.service.AreaOfUseService;
import org.opengroup.osdu.crs.service.SearchWrapperService;
import org.springframework.web.bind.annotation.*;

import org.opengroup.osdu.crs.model.interfaces.*;

/**
 *  CrsApi is an endpoint class we are exposing so that clients can request or query for
 *  coordinate reference system related catalog data information through its various
 *  exposed API methods
 */

@RestController
@RequestMapping(path= "/v3")
public class CrsCatalogApiV3 {

	private Catalog catalog = null;

	@Inject
	private AuditLogger auditLogger;

	@Inject
	private JaxRsDpsLog log;

	@Inject
	private SearchWrapperService searchWrapperService;

	@Inject
	private AreaOfUseService areaOfUseService;

	@GetMapping("/coordinate-transformation")
	public SearchResponse getCoordinateTransformation(
			@RequestParam(name = "recordId", required = false) String recordId,
			@RequestParam(name = "dataId", required = false) String dataId
			) {
		return searchWrapperService.getSingleCrsOrCt(recordId, dataId, SearchWrapperService.COORDINATE_TRANSFORMATION_KIND);
	}

	@PostMapping("/coordinate-transformation")
	public SearchResponse getCoordinateTransformations(
			@RequestBody CoordinateTransformationsQuery coordinateTransformationsQuery
			) {
		return searchWrapperService.search(coordinateTransformationsQuery, SearchWrapperService.COORDINATE_TRANSFORMATION_KIND);
	}

	@GetMapping("/coordinate-reference-system")
	public SearchResponse getCoordinateReferenceSystem(
			@RequestParam(name = "recordId", required = false) String recordId,
			@RequestParam(name = "dataId", required = false) String dataId
	) {
		return searchWrapperService.getSingleCrsOrCt(recordId, dataId, SearchWrapperService.COORDINATE_REFERENCE_SYSTEM_KIND);
	}

	@PostMapping("/coordinate-reference-system")
	public SearchResponse getCoordinateReferenceSystems(
			@RequestBody CoordinateReferenceSystemsQuery coordinateReferenceSystemsQuery
	) {
		return searchWrapperService.search(coordinateReferenceSystemsQuery, SearchWrapperService.COORDINATE_REFERENCE_SYSTEM_KIND);
	}

	@PostMapping("/points-in-aou")
	public PointsInAouSearchResult getAouInfo(
			@RequestBody InPolygonQuery inPolygonQuery
			) {
		return areaOfUseService.searchAou(inPolygonQuery);
	}

}
