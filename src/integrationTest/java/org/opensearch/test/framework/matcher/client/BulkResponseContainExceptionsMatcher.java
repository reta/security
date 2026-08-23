/*
* Copyright OpenSearch Contributors
* SPDX-License-Identifier: Apache-2.0
*
* The OpenSearch Contributors require contributions made to
* this file be licensed under the Apache-2.0 license or a
* compatible open source license.
*
*/
package org.opensearch.test.framework.matcher.client;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;

import static java.util.Objects.requireNonNull;

class BulkResponseContainExceptionsMatcher extends TypeSafeDiagnosingMatcher<BulkResponse> {

    private final Matcher<Throwable> exceptionMatcher;

    public BulkResponseContainExceptionsMatcher(Matcher<Throwable> exceptionMatcher) {
        this.exceptionMatcher = requireNonNull(exceptionMatcher, "Exception matcher is required.");
    }

    @Override
    protected boolean matchesSafely(BulkResponse response, Description mismatchDescription) {
        if (response.errors() == false) {
            mismatchDescription.appendText("received successful bulk response what is not expected.");
            return false;
        }
        List<BulkResponseItem> items = response.items();
        if ((items == null) || (items.isEmpty())) {
            mismatchDescription.appendText("bulk response does not contain items ").appendValue(items);
            return false;
        }
        for (int i = 0; i < items.size(); ++i) {
            BulkResponseItem item = items.get(i);
            if (item == null) {
                mismatchDescription.appendText("bulk item response with index ").appendValue(i).appendText(" is null.");
                return false;
            }
            ErrorCause failure = item.error();
            if (failure == null) {
                mismatchDescription.appendText("bulk response item with index ").appendValue(i).appendText(" does not contain failure");
                return false;
            }
            ErrorCause exception = failure.causedBy();
            if (exception == null) {
                mismatchDescription.appendText("bulk response item with index ").appendValue(i).appendText(" does not contain exception.");
                return false;
            }
            if (exceptionMatcher.matches(exception) == false) {
                mismatchDescription.appendText("bulk response item with index ")
                    .appendValue(i)
                    .appendText(" contains incorrect exception which is ")
                    .appendValue(exception);
                return false;
            }
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("bulk response should contain exceptions which indicate failure");
    }
}
