// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc.encoding;

import fi.jumi.core.ipc.api.ResponseListener;

import java.nio.file.Paths;

public class ResponseListenerEncodingTest extends EncodingContract<ResponseListener> {

    public ResponseListenerEncodingTest() {
        super(ResponseListenerEncoding::new);
    }

    @Override
    protected void exampleUsage(ResponseListener listener) throws Exception {
        listener.onSuiteStarted(Paths.get("foo", "bar"));
    }
}
