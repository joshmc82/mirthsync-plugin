/*
 * Copyright 2021 Kaur Palang (original template)
 * Copyright 2025 Saga IT, LLC
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

package com.saga_it.mirthsync_plugin.shared.interfaces;

import com.kaurpalang.mirth.annotationsplugin.annotation.MirthApiProvider;
import com.kaurpalang.mirth.annotationsplugin.type.ApiProviderType;
import com.saga_it.mirthsync_plugin.shared.PluginPermissions;
import com.saga_it.mirthsync_plugin.shared.model.MirthSyncInfo;
import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.core.api.BaseServletInterface;
import com.mirth.connect.client.core.api.MirthOperation;
import com.mirth.connect.client.core.api.Param;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/mirthsync")
@Tag(name = "MirthSync operations")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@MirthApiProvider(type = ApiProviderType.SERVLET_INTERFACE)
public interface MirthSyncApi extends BaseServletInterface {

    @GET
    @Path("/info")
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiResponse(responseCode = "200", description = "Returns summary information for the requested identifier",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = MirthSyncInfo.class)),
                    @Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = MirthSyncInfo.class))
            })
    @MirthOperation(
            name = "getRepositoryInfo",
            display = "Get MirthSync info",
            permission = PluginPermissions.GETSTH
    )
    MirthSyncInfo getRepositoryInfo(
            @Param("identifier") @Parameter(description = "Identifier used to retrieve MirthSync info.", required = true) @QueryParam("identifier") String identifier)
            throws ClientException;

    @GET
    @Path("/help")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponse(responseCode = "200", description = "MirthSync help output",
            content = {
                    @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class))
            })
    @MirthOperation(
            name = "getMirthsyncHelp",
            display = "Get MirthSync Help",
            permission = PluginPermissions.SHOWHELP
    )
    String getMirthsyncHelp() throws ClientException;

    @POST
    @Path("/execute")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "Result of mirthsync invocation",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(type = "object")))
    @MirthOperation(
            name = "executeMirthsync",
            display = "Execute mirthsync command",
            permission = PluginPermissions.EXECUTE
    )
    Map<String, Object> executeMirthsync(
            @Param("arguments") @RequestBody(required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(type = "string"))))
            List<String> arguments) throws ClientException;

    @GET
    @Path("/session-token")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponse(responseCode = "200", description = "Current session token",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class)))
    @MirthOperation(
            name = "getSessionToken",
            display = "Get session token",
            permission = PluginPermissions.EXECUTE
    )
    String getSessionToken() throws ClientException;
}
