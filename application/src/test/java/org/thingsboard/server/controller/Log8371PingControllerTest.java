/**
 * Copyright © 2016-2026 The Thingsboard Authors
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

package org.thingsboard.server.controller;

import org.junit.Assert;
import org.junit.Test;
import org.thingsboard.server.dao.service.DaoSqlTest;

@DaoSqlTest
public class Log8371PingControllerTest extends AbstractControllerTest {

    @Test
    public void testPingWithParams() throws Exception {
        String name = "Mina";
        String msg = "HelloWorld";

        loginSysAdmin();
        String response = doGet("/api/log8371-ping?name=" + name + "&msg=" + msg, String.class);
        Assert.assertEquals("Bonjour, ma fonctionnalité fonctionnelle dit que vous vous appellez Mina et votre message est: HelloWorld"
                            , response);
    }

    @Test
    public void testPingWithNameOnly() throws Exception {
        String name = "Mina";

        loginSysAdmin();
        String response = doGet("/api/log8371-ping?name=" + name, String.class);
        Assert.assertEquals("Bonjour, ma fonctionnalité fonctionnelle dit que vous vous appellez Mina et votre message est: POW!"
                , response);
    }

    @Test
    public void testPingWithMessageOnly() throws Exception {
        String msg = "HelloWorld";

        loginSysAdmin();
        String response = doGet("/api/log8371-ping?&msg=" + msg, String.class);
        Assert.assertEquals("Bonjour, ma fonctionnalité fonctionnelle dit que vous vous appellez JAMES_BOND et votre message est: HelloWorld"
                , response);
    }

    @Test
    public void testPingWithNoParams() throws Exception {
        loginSysAdmin();
        String response = doGet("/api/log8371-ping", String.class);
        Assert.assertEquals("Bonjour, ma fonctionnalité fonctionnelle dit que vous vous appellez JAMES_BOND et votre message est: POW!"
                , response);
    }
}