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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api")
public class Log8371PingController extends BaseController {

    @GetMapping("/log8371-ping")
    @PreAuthorize("permitAll()")
    public String ping(@RequestParam(required = false, defaultValue = "JAMES_BOND") String name,@RequestParam(required = false, defaultValue = "POW!") String msg) {
        return "Bonjour, ma fonctionnalité fonctionnelle dit que vous vous appellez " + name + " et votre message est: " + msg;
    }
}