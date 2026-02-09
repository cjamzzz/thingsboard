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
package org.thingsboard.server.transport.http;

import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.async.DeferredResult;
import org.thingsboard.server.common.data.DeviceTransportType;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.DeviceProfileId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.transport.TransportContext;
import org.thingsboard.server.common.transport.TransportService;
import org.thingsboard.server.common.transport.TransportServiceCallback;
import org.thingsboard.server.common.transport.auth.TransportDeviceInfo;
import org.thingsboard.server.common.transport.auth.ValidateDeviceCredentialsResponse;
import org.thingsboard.server.gen.transport.TransportProtos;
import org.thingsboard.server.gen.transport.TransportProtos.PostTelemetryMsg;
import org.thingsboard.server.gen.transport.TransportProtos.SessionInfoProto;
import org.thingsboard.server.gen.transport.TransportProtos.ValidateDeviceTokenRequestMsg;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeviceApiControllerTest {

    private static ValidateDeviceCredentialsResponse newValidCredentials() {
        TransportDeviceInfo deviceInfo = new TransportDeviceInfo();
        deviceInfo.setTenantId(TenantId.fromUUID(UUID.randomUUID()));
        deviceInfo.setCustomerId(new CustomerId(UUID.randomUUID()));
        deviceInfo.setDeviceProfileId(new DeviceProfileId(UUID.randomUUID()));
        deviceInfo.setDeviceId(new DeviceId(UUID.randomUUID()));
        deviceInfo.setDeviceName("Test Device");
        deviceInfo.setDeviceType("default");
        deviceInfo.setGateway(false);
        return ValidateDeviceCredentialsResponse.builder().deviceInfo(deviceInfo).build();
    }

    private static MockMvc newMockMvc(DeviceApiController controller, HttpTransportContext transportContext) {
        ReflectionTestUtils.setField(controller, "transportContext", transportContext);
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void deviceAuthCallbackTest() {
        TransportContext transportContext = Mockito.mock(TransportContext.class);
        DeferredResult<ResponseEntity> responseWriter = Mockito.mock(DeferredResult.class);
        Consumer<TransportProtos.SessionInfoProto> onSuccess = x -> {
        };
        var callback = new DeviceApiController.DeviceAuthCallback(transportContext, responseWriter, onSuccess);

        callback.onError(new HttpMessageNotReadableException("JSON incorrect syntax"));

        callback.onError(new JsonParseException("Json ; expected"));

        callback.onError(new IOException("not found"));

        callback.onError(new RuntimeException("oops it is run time error"));
    }

    @Test
    void deviceProvisionCallbackTest() {
        DeferredResult<ResponseEntity> responseWriter = Mockito.mock(DeferredResult.class);
        var callback = new DeviceApiController.DeviceProvisionCallback(responseWriter);

        callback.onError(new HttpMessageNotReadableException("JSON incorrect syntax"));

        callback.onError(new JsonParseException("Json ; expected"));

        callback.onError(new IOException("not found"));

        callback.onError(new RuntimeException("oops it is run time error"));
    }

    @Test
    void getOtaPackageCallback() {
        TransportContext transportContext = Mockito.mock(TransportContext.class);
        DeferredResult<ResponseEntity> responseWriter = Mockito.mock(DeferredResult.class);
        String title = "Title";
        String version = "version";
        int chunkSize = 11;
        int chunk = 3;

        var callback = new DeviceApiController.GetOtaPackageCallback(transportContext, responseWriter, title, version, chunkSize, chunk);

        callback.onError(new HttpMessageNotReadableException("JSON incorrect syntax"));

        callback.onError(new JsonParseException("Json ; expected"));

        callback.onError(new IOException("not found"));

        callback.onError(new RuntimeException("oops it is run time error"));
    }

    @Test
    void getDeviceAttributes_whenAuthFails_returns401_andCompletes() throws Exception {
        HttpTransportContext transportContext = mock(HttpTransportContext.class);
        TransportService transportService = mock(TransportService.class);
        when(transportContext.getTransportService()).thenReturn(transportService);

        doAnswer(invocation -> {
            TransportServiceCallback<ValidateDeviceCredentialsResponse> callback = invocation.getArgument(2);
            callback.onSuccess(ValidateDeviceCredentialsResponse.builder().deviceInfo(null).build());
            return null;
        }).when(transportService).process(eq(DeviceTransportType.DEFAULT), any(ValidateDeviceTokenRequestMsg.class), any());

        DeviceApiController controller = new DeviceApiController();
        MockMvc mockMvc = newMockMvc(controller, transportContext);

        MvcResult result = mockMvc.perform(get("/api/v1/{deviceToken}/attributes", "bad-token"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postTelemetry_happyPath_returns200_andCompletes() throws Exception {
        HttpTransportContext transportContext = mock(HttpTransportContext.class);
        TransportService transportService = mock(TransportService.class);
        when(transportContext.getTransportService()).thenReturn(transportService);
        when(transportContext.getNodeId()).thenReturn("test-node");

        doAnswer(invocation -> {
            TransportServiceCallback<ValidateDeviceCredentialsResponse> callback = invocation.getArgument(2);
            callback.onSuccess(newValidCredentials());
            return null;
        }).when(transportService).process(eq(DeviceTransportType.DEFAULT), any(ValidateDeviceTokenRequestMsg.class), any());

        doAnswer(invocation -> {
            TransportServiceCallback<Void> callback = invocation.getArgument(2);
            callback.onSuccess(null);
            return null;
        }).when(transportService).process(any(SessionInfoProto.class), any(PostTelemetryMsg.class), any());

        DeviceApiController controller = new DeviceApiController();
        MockMvc mockMvc = newMockMvc(controller, transportContext);

        MvcResult result = mockMvc.perform(post("/api/v1/{deviceToken}/telemetry", "good-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk());
    }

    @Test
    void postTelemetry_whenTransportServiceErrors_returns500_andCompletes() throws Exception {
        HttpTransportContext transportContext = mock(HttpTransportContext.class);
        TransportService transportService = mock(TransportService.class);
        when(transportContext.getTransportService()).thenReturn(transportService);
        when(transportContext.getNodeId()).thenReturn("test-node");

        doAnswer(invocation -> {
            TransportServiceCallback<ValidateDeviceCredentialsResponse> callback = invocation.getArgument(2);
            callback.onSuccess(newValidCredentials());
            return null;
        }).when(transportService).process(eq(DeviceTransportType.DEFAULT), any(ValidateDeviceTokenRequestMsg.class), any());

        doAnswer(invocation -> {
            TransportServiceCallback<Void> callback = invocation.getArgument(2);
            callback.onError(new RuntimeException("boom"));
            return null;
        }).when(transportService).process(any(SessionInfoProto.class), any(PostTelemetryMsg.class), any());

        DeviceApiController controller = new DeviceApiController();
        MockMvc mockMvc = newMockMvc(controller, transportContext);

        MvcResult result = mockMvc.perform(post("/api/v1/{deviceToken}/telemetry", "good-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isInternalServerError());
    }
}
