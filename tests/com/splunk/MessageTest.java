/*
 * Copyright 2011 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.splunk;

import org.junit.Test;

public class MessageTest extends SplunkTestCase {
    @Test public void testMessage() throws Exception {
        Service service = connect();

        MessageCollection messageCollection = service.getMessages();

        if (messageCollection.containsKey("sdk-test-message1"))
            messageCollection.remove("sdk-test-message1");
        assertFalse(messageCollection.containsKey("sdk-test-message1"));

        if (messageCollection.containsKey("sdk-test-message2"))
            messageCollection.remove("sdk-test-message2");
        assertFalse(messageCollection.containsKey("sdk-test-message2"));

        messageCollection.create("sdk-test-message1", "hello.");
        assertTrue(messageCollection.containsKey("sdk-test-message1"));
        Message message = messageCollection.get("sdk-test-message1");
        assertTrue(message.getKey().equals("sdk-test-message1"));
        assertTrue(message.getValue().equals("hello."));

        Args args2 = new Args();
        args2.put("value", "world.");
        messageCollection.create("sdk-test-message2", args2);

        assertTrue(messageCollection.containsKey("sdk-test-message2"));
        message = messageCollection.get("sdk-test-message2");
        assertTrue(message.getKey().equals("sdk-test-message2"));
        assertTrue(message.getValue().equals("world."));

        messageCollection.remove("sdk-test-message1");
        assertFalse(messageCollection.containsKey("sdk-test-message1"));

        messageCollection.remove("sdk-test-message2");
        assertFalse(messageCollection.containsKey("sdk-test-message2"));
    }
}
