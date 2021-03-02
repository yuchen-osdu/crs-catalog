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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

@RunWith(MockitoJUnitRunner.class)
public class AuditLoggerTest {

  @Mock
  private JaxRsDpsLog log;

  @Mock
  private DpsHeaders headers;

  @InjectMocks
  private AuditLogger sut;

  private List<String> resources;

  @Before
  public void setup() {
    when(this.headers.getUserEmail()).thenReturn("test_user@email.com");
    resources = Collections.singletonList("resources");
  }

  @Test
  public void should_writeReadCrsSuccessEvent() {
    this.sut.readCrsSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCrsFailureFailureEvent() {
    this.sut.readCrsFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCrsByEssenceSuccessEvent() {
    this.sut.readCrsByEssenceSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCrsByEssenceFailureEvent() {
    this.sut.readCrsByEssenceFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadLateBoundCrsSuccessEvent() {
    this.sut.readLateBoundCrsSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadLateBoundCrsFailureEvent() {
    this.sut.readLateBoundCrsFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadLateBoundCrsByEssenceSuccessEvent() {
    this.sut.readLateBoundCrsByEssenceSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadLateBoundCrsByEssenceFailureEvent() {
    this.sut.readLateBoundCrsByEssenceFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadEarlyBoundCrsSuccessEvent() {
    this.sut.readEarlyBoundCrsSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadEarlyBoundCrsFailureEvent() {
    this.sut.readEarlyBoundCrsFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadEarlyBoundCrsByEssenceSuccessEvent() {
    this.sut.readEarlyBoundCrsByEssenceSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadEarlyBoundCrsByEssenceFailureEvent() {
    this.sut.readEarlyBoundCrsByEssenceFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCompoundCrsSuccessEvent() {
    this.sut.readCompoundCrsSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCompoundCrsFailureEvent() {
    this.sut.readCompoundCrsFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCompoundCrsByEssenceSuccessEvent() {
    this.sut.readCompoundCrsByEssenceSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCompoundCrsByEssenceFailureEvent() {
    this.sut.readCompoundCrsByEssenceFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeSearchCrsSuccessEvent() {
    this.sut.searchCrsSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeSearchCrsFailureEvent() {
    this.sut.searchCrsFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCtSuccessEvent() {
    this.sut.readCtSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCtFailureEvent() {
    this.sut.readCtFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCtByEssenceSuccessEvent() {
    this.sut.readCtByEssenceSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCtByEssenceFailureEvent() {
    this.sut.readCtByEssenceFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadSingleCtSuccessEvent() {
    this.sut.readSingleCtSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadSingleCtFailureEvent() {
    this.sut.readSingleCtFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadSingleCtByEssenceSuccessEvent() {
    this.sut.readSingleCtByEssenceSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadSingleCtByEssenceFailureEvent() {
    this.sut.readSingleCtByEssenceFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCompoundCtSuccessEvent() {
    this.sut.readCompoundCtSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCompoundCtFailureEvent() {
    this.sut.readCompoundCtFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCompoundCtByEssenceSuccessEvent() {
    this.sut.readCompoundCtByEssenceSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadCompoundCtByEssenceFailureEvent() {
    this.sut.readCompoundCtByEssenceFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeSearchCtSuccessEvent() {
    this.sut.searchCtSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeSearchCtFailureEvent() {
    this.sut.searchCtFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadAreaSuccessEvent() {
    this.sut.readAreaSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadAreaFailureEvent() {
    this.sut.readAreaFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadAreaByEssenceSuccessEvent() {
    this.sut.readAreaByEssenceSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeReadAreaByEssenceFailureEvent() {
    this.sut.readAreaByEssenceFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeSearchAreaSuccessEvent() {
    this.sut.searchAreaSuccess(this.resources);

    verify(this.log, times(1)).audit(any());
  }

  @Test
  public void should_writeSearchAreaFailureEvent() {
    this.sut.searchAreaFailure(this.resources);

    verify(this.log, times(1)).audit(any());
  }
}