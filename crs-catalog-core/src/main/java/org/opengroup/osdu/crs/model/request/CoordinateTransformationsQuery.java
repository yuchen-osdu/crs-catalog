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
public class CoordinateTransformationsQuery implements ISearchQuery {
    private String codeSpace;
    private String name;
    private String id;
    private String code;
    private String kind = "ExcludeVertical";
    private String sourceCRS;
    private String targetCRS;
    private Double latitude;
    private Double longitude;
    private Boolean includeDeprecated = false;
    private Integer offset;
    private Integer limit = 10000;
    private Boolean returnAllFields = false;
    List<String> returnedFields = Arrays.asList(
            "id", "data.Name", "data.Code", "data.InactiveIndicator", "data.Kind", "data.Accuracy",
            "data.Method", "data.CodeSpace", "data.CoordinateTransformationType", "data.SourceCRS",
            "data.TargetCRS", "data.PreferredUsage"
    );

    public String constructQuery() {
        SearchQueryBuilder queryBuilder = new SearchQueryBuilder();
        if (!includeDeprecated) {
            queryBuilder.addQuery("NOT data.InactiveIndicator: true");
        }
        switch (kind.toLowerCase()) {
            case "excludevertical":
                queryBuilder.addNegativePhraseQuery("data.Kind", "VerticalTransformation");
                break;
            case "all":
                break;
            default:
                queryBuilder.addPhraseQuery("data.Kind", kind);
        }
        queryBuilder.addPhraseQuery("data.CodeSpace", codeSpace);
        queryBuilder.addPhraseQuery("data.Name", name);
        queryBuilder.addPhraseQuery("data.ID", id);
        queryBuilder.addPhraseQuery("data.Code", code);
        queryBuilder.addMultiFieldPhraseQuery(sourceCRS, "data.SourceCRS.SourceCRSID", "data.TargetCRS.TargetCRSID");
        queryBuilder.addMultiFieldPhraseQuery(targetCRS, "data.SourceCRS.SourceCRSID", "data.TargetCRS.TargetCRSID");
        return queryBuilder.connectQuery("AND");
    }

    public SpatialFilter constructSpatialFilter() {
        return SpatialQueryBuilder.buildSpatialQuery(latitude, longitude);
    }

    public List<String> getReturnedFields() {
        if (!returnAllFields) {
            return Arrays.asList(
                    "id", "data.Name", "data.Code", "data.InactiveIndicator", "data.Kind", "data.Accuracy",
                    "data.Method", "data.CodeSpace", "data.CoordinateTransformationType", "data.SourceCRS",
                    "data.TargetCRS", "data.PreferredUsage"
            );
        }
        return new ArrayList<>();
    }
}
