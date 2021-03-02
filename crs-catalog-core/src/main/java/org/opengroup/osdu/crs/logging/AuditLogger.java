/*
  Copyright 2021 Google LLC
  Copyright 2021 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.opengroup.osdu.crs.logging;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
public class AuditLogger {

  private final JaxRsDpsLog logger;
  private final DpsHeaders headers;
  private AuditEvents auditEvents;

  private AuditEvents getAuditEvents() {
    if (this.auditEvents == null) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (Objects.nonNull(authentication) && authentication.getPrincipal() instanceof Groups) {
        Groups groups = (Groups) authentication.getPrincipal();
        this.auditEvents = new AuditEvents(groups.getMemberEmail());
      } else {
        this.auditEvents = new AuditEvents(this.headers.getUserEmail());
      }
    }
    return this.auditEvents;
  }

  public void readCrsSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadCrsEvent(AuditStatus.SUCCESS, resources));
  }

  public void readCrsFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadCrsEvent(AuditStatus.FAILURE, resources));
  }

  public void readCrsByEssenceSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadCrsByEssenceEvent(AuditStatus.SUCCESS, resources));
  }

  public void readCrsByEssenceFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadCrsByEssenceEvent(AuditStatus.FAILURE, resources));
  }

  public void readLateBoundCrsSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadLateBoundCrsEvent(AuditStatus.SUCCESS, resources));
  }

  public void readLateBoundCrsFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadLateBoundCrsEvent(AuditStatus.FAILURE, resources));
  }

  public void readLateBoundCrsByEssenceSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadLateBoundCrsByEssenceEvent(AuditStatus.SUCCESS, resources));
  }

  public void readLateBoundCrsByEssenceFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadLateBoundCrsByEssenceEvent(AuditStatus.FAILURE, resources));
  }

  public void readEarlyBoundCrsSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadEarlyBoundCrsEvent(AuditStatus.SUCCESS, resources));
  }

  public void readEarlyBoundCrsFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadEarlyBoundCrsEvent(AuditStatus.FAILURE, resources));
  }

  public void readEarlyBoundCrsByEssenceSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadEarlyBoundCrsByEssenceEvent(AuditStatus.SUCCESS, resources));
  }

  public void readEarlyBoundCrsByEssenceFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadLateBoundCrsByEssenceEvent(AuditStatus.FAILURE, resources));
  }

  public void readCompoundCrsSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadCompoundCrsEvent(AuditStatus.SUCCESS, resources));
  }

  public void readCompoundCrsFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadCompoundCrsEvent(AuditStatus.FAILURE, resources));
  }

  public void readCompoundCrsByEssenceSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadCompoundCrsByEssenceEvent(AuditStatus.SUCCESS, resources));
  }

  public void readCompoundCrsByEssenceFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadCompoundCrsByEssenceEvent(AuditStatus.FAILURE, resources));
  }

  public void searchCrsSuccess(List<String> resources) {
    writeLog(getAuditEvents().getSearchCrsEvent(AuditStatus.SUCCESS, resources));
  }

  public void searchCrsFailure(List<String> resources) {
    writeLog(getAuditEvents().getSearchCrsEvent(AuditStatus.FAILURE, resources));
  }

  public void readCtSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadCtEvent(AuditStatus.SUCCESS, resources));
  }

  public void readCtFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadCtEvent(AuditStatus.FAILURE, resources));
  }

  public void readCtByEssenceSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadCtByEssenceEvent(AuditStatus.SUCCESS, resources));
  }

  public void readCtByEssenceFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadCtByEssenceEvent(AuditStatus.FAILURE, resources));
  }

  public void readSingleCtSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadSingleCtEvent(AuditStatus.SUCCESS, resources));
  }

  public void readSingleCtFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadSingleCtEvent(AuditStatus.FAILURE, resources));
  }

  public void readSingleCtByEssenceSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadSingleCtByEssenceEvent(AuditStatus.SUCCESS, resources));
  }

  public void readSingleCtByEssenceFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadSingleCtByEssenceEvent(AuditStatus.FAILURE, resources));
  }

  public void readCompoundCtSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadCompoundCtEvent(AuditStatus.SUCCESS, resources));
  }

  public void readCompoundCtFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadCompoundCtEvent(AuditStatus.FAILURE, resources));
  }

  public void readCompoundCtByEssenceSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadCompoundCtByEssenceEvent(AuditStatus.SUCCESS, resources));
  }

  public void readCompoundCtByEssenceFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadCompoundCtByEssenceEvent(AuditStatus.FAILURE, resources));
  }

  public void searchCtSuccess(List<String> resources) {
    writeLog(getAuditEvents().getSearchCtEvent(AuditStatus.SUCCESS, resources));
  }

  public void searchCtFailure(List<String> resources) {
    writeLog(getAuditEvents().getSearchCtEvent(AuditStatus.FAILURE, resources));
  }

  public void readAreaSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadAreaEvent(AuditStatus.SUCCESS, resources));
  }

  public void readAreaFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadAreaEvent(AuditStatus.FAILURE, resources));
  }

  public void readAreaByEssenceSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadAreaByEssenceEvent(AuditStatus.SUCCESS, resources));
  }

  public void readAreaByEssenceFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadAreaByEssenceEvent(AuditStatus.FAILURE, resources));
  }

  public void searchAreaSuccess(List<String> resources) {
    writeLog(getAuditEvents().getSearchAreaEvent(AuditStatus.SUCCESS, resources));
  }

  public void searchAreaFailure(List<String> resources) {
    writeLog(getAuditEvents().getSearchAreaEvent(AuditStatus.FAILURE, resources));
  }

  private void writeLog(AuditPayload log) {
    this.logger.audit(log);
  }
}
