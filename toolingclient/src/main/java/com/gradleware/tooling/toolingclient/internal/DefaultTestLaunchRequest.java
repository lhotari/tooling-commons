/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gradleware.tooling.toolingclient.internal;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingclient.LongRunningOperationPromise;
import com.gradleware.tooling.toolingclient.TestOperationConfig;

/**
 * Default implementation of the {@link com.gradleware.tooling.toolingclient.TestLaunchRequest}
 * interface.
 *
 * @author Donát Csikós
 */
public final class DefaultTestLaunchRequest extends BaseRequest<Void, DefaultTestLaunchRequest>implements InspectableTestLaunchRequest {

    private final TestOperationConfig testOperations;

    DefaultTestLaunchRequest(ExecutableToolingClient toolingClient, TestOperationConfig testOperations) {
        super(toolingClient);
        this.testOperations = Preconditions.checkNotNull(testOperations);
    }

    @Override
    public TestOperationConfig getTestOperations() {
        return this.testOperations;
    }

    @Override
    public Void executeAndWait() {
        return getToolingClient().executeAndWait(this);
    }

    @Override
    public LongRunningOperationPromise<Void> execute() {
        return getToolingClient().execute(this);
    }

    @Override
    DefaultTestLaunchRequest getThis() {
        return this;
    }

}
