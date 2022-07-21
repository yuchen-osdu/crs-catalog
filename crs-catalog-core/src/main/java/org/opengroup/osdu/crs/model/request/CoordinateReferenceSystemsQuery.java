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
import org.opengroup.osdu.core.common.model.search.SpatialFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Data
public class CoordinateReferenceSystemsQuery implements ISearchQuery {
    private String codeSpace;
    private String code;
    private String name;
    private String id;
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
    private Boolean includeDeprecated = false;
    private Integer offset;
    private Integer limit = 10000;
    private Boolean returnAllFields = false;
    private List<String> returnedFields;

    public String constructQuery() {
        SearchQueryBuilder queryBuilder = new SearchQueryBuilder();
        if (!includeDeprecated) {
            queryBuilder.addQuery("NOT data.InactiveIndicator: true");
        }
        queryBuilder.addPhraseQuery("data.CodeSpace", codeSpace);
        queryBuilder.addPhraseQuery("data.Code", code);
        queryBuilder.addPhraseQuery("data.Name", name);
        queryBuilder.addPhraseQuery("data.ID", id);
        queryBuilder.addPhraseQuery("data.Kind", kind);
        queryBuilder.addPhraseQuery("data.CoordinateReferenceSystemType", coordinateReferenceSystemType);
        queryBuilder.addPhraseQuery("data.CoordinateSystem.HorizontalAxisUnitID", horizontalAxisUnitId);
        queryBuilder.addPhraseQuery("data.CoordinateSystem.VerticalAxisUnitID", verticalAxisUnitId);

        if (baseCRS != null) {
            queryBuilder.addPhraseQuery("data.BaseCRS.BaseCRSID", baseCRS.getId());
            queryBuilder.addPhraseQuery("data.BaseCRS.Name", baseCRS.getName());
        }

        if (datum != null) {
            queryBuilder.addPhraseQuery("data.Datum.Name", datum.getName());
            queryBuilder.addPhraseQuery("data.Datum.AuthorityCode.Authority", datum.getCodeSpace());
            queryBuilder.addPhraseQuery("data.Datum.AuthorityCode.Code", datum.getCode());
        }

        if (extent != null) {
            queryBuilder.addPhraseQuery("data.PreferredUsage.Extent.Description", extent.getDescription());
        }

        if (returnBoundProjectedAndProjectedBasedOnWgs84) {
            queryBuilder.addQuery(
                    SearchQueryBuilder.connectMultipleQueries("OR",
                            SearchQueryBuilder.buildPhraseQuery("data.Kind", "BoundProjected"),
                            SearchQueryBuilder.connectMultipleQueries("AND",
                                    SearchQueryBuilder.buildPhraseQuery("data.Kind", "projected"),
                                    SearchQueryBuilder.buildPhraseQuery("data.BaseCRS.AuthorityCode.Code", "4326"))));
        }

        if (returnBoundGeographic2DAndWgs84) {
            queryBuilder.addQuery(
                    SearchQueryBuilder.connectMultipleQueries("OR",
                            SearchQueryBuilder.buildPhraseQuery("data.Kind", "BoundGeographic2D"),
                            SearchQueryBuilder.connectMultipleQueries("AND",
                                    SearchQueryBuilder.buildPhraseQuery("data.Kind", "geographic 2D"),
                                    SearchQueryBuilder.buildPhraseQuery("data.Code", "4326"),
                                    SearchQueryBuilder.buildPhraseQuery("data.CodeSpace", "EPSG"))));
        }

        return queryBuilder.connectQuery("AND");
    }

    public SpatialFilter constructSpatialFilter() {
        return SpatialQueryBuilder.buildSpatialQuery(latitude, longitude);
    }

    public List<String> getReturnedFields() {
        if (!returnAllFields) {
            return Arrays.asList(
                    "id", "data.Name", "data.Code", "data.InactiveIndicator", "data.Kind", "data.CodeSpace",
                    "data.Datum", "data.CoordinateSystem", "data.PreferredUsage", "data.Description",
                    "data.RevisionDate");
        }
        return new ArrayList<>();
    }
}