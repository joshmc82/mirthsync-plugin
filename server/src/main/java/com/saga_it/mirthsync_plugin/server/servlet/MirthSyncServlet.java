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

package com.saga_it.mirthsync_plugin.server.servlet;

import com.kaurpalang.mirth.annotationsplugin.annotation.MirthApiProvider;
import com.kaurpalang.mirth.annotationsplugin.type.ApiProviderType;
import com.saga_it.mirthsync_plugin.shared.PluginConstants;
import com.saga_it.mirthsync_plugin.shared.interfaces.MirthSyncApi;
import com.saga_it.mirthsync_plugin.shared.model.MirthSyncInfo;
import com.mirth.connect.server.api.MirthServlet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@MirthApiProvider(type = ApiProviderType.SERVER_CLASS)
public class MirthSyncServlet extends MirthServlet implements MirthSyncApi {

    private static final Logger LOGGER = Logger.getLogger(MirthSyncServlet.class.getName());
    private static final Pattern ANSI_ESCAPE_PATTERN = Pattern.compile("\\x1B\\[[0-9;:]*[ -/]*[@-~]");
    private static final Pattern ANSI_OSC_PATTERN = Pattern.compile("\\x1B\\][^\\x07]*(\\x07|\\x1B\\\\)");
    private static final Pattern INVALID_XML_CHAR_PATTERN = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F\\x{FFFE}\\x{FFFF}]");

    public MirthSyncServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc, PluginConstants.PLUGIN_POINTNAME);
    }

    @Override
    public MirthSyncInfo getRepositoryInfo(String identifier) {
        String data = String.format("Repository metadata placeholder for %s", identifier);
        return new MirthSyncInfo(data);
    }

    @Override
    public String getMirthsyncHelp() {
        StringWriter stdout = new StringWriter();
        StringWriter stderr = new StringWriter();

        try (PrintWriter outWriter = new PrintWriter(stdout, true);
             PrintWriter errWriter = new PrintWriter(stderr, true)) {

            int exitCode = mirthsync.core.mainFunc(new String[]{"-h"}, outWriter, errWriter);
            outWriter.flush();
            errWriter.flush();

            StringBuilder response = new StringBuilder();
            if (stdout.getBuffer().length() > 0) {
                response.append(stdout.toString().trim()).append("\n");
            }
            if (stderr.getBuffer().length() > 0) {
                response.append("stderr:\n").append(stderr.toString().trim()).append("\n");
            }
            response.append("exit code: ").append(exitCode);
            return response.toString().trim();
        } catch (Throwable t) {
            return handleHelpFailure("Failed to invoke mirthsync help", stdout, stderr, t);
        }
    }

    @Override
    public Map<String, Object> executeMirthsync(List<String> arguments) {
        if (arguments == null) {
            throw new BadRequestException("Arguments payload is required.");
        }

        List<String> args = new ArrayList<>();
        for (String argument : arguments) {
            if (argument != null && !argument.isBlank()) {
                args.add(argument);
            }
        }

        if (args.isEmpty()) {
            throw new BadRequestException("At least one argument is required.");
        }

        ensureTokenIfMissing(args);

        StringWriter stdout = new StringWriter();
        StringWriter stderr = new StringWriter();

        try {
            try (PrintWriter outWriter = new PrintWriter(stdout, true);
                 PrintWriter errWriter = new PrintWriter(stderr, true)) {

                int exitCode = mirthsync.core.mainFunc(args.toArray(new String[0]), outWriter, errWriter);
                outWriter.flush();
                errWriter.flush();

                return buildSuccessResponse(args, stdout.toString(), stderr.toString(), exitCode);
            }
        } catch (Throwable t) {
            return handleExecutionFailure("Failed to invoke mirthsync core mainFunc", args, stdout, stderr, t);
        }
    }

    @Override
    public String getSessionToken() {
        String token = resolveSessionToken();
        if (isBlank(token)) {
            throw new BadRequestException("Session token is not available for the current user.");
        }
        return token;
    }

    private Map<String, Object> buildSuccessResponse(List<String> args, String stdout, String stderr, int exitCode) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("command", redactSensitiveArgs(args));
        response.put("stdout", sanitizeConsoleValue(stdout));
        response.put("stderr", sanitizeConsoleValue(stderr));
        response.put("exitCode", exitCode);
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String handleHelpFailure(String summary, StringWriter stdout, StringWriter stderr, Throwable cause) {
        String message = buildErrorMessage(summary, cause);
        LOGGER.log(Level.SEVERE, message, cause);
        if (cause != null) {
            cause.printStackTrace(System.err);
        }

        StringBuilder response = new StringBuilder();
        String stdOutText = sanitizeConsoleValue(stdout.toString());
        if (stdOutText != null) {
            response.append(stdOutText).append("\n\n");
        }

        String stdErrText = sanitizeConsoleValue(stderr.toString());
        if (stdErrText != null) {
            response.append("stderr:\n").append(stdErrText).append("\n\n");
        }

        response.append(message);
        if (cause != null) {
            response.append("\n").append(stackTraceToString(cause));
        }

        return response.toString();
    }

    private Map<String, Object> handleExecutionFailure(String summary,
                                                       List<String> args,
                                                       StringWriter stdout,
                                                       StringWriter stderr,
                                                       Throwable cause) {
        String message = buildErrorMessage(summary, cause);
        LOGGER.log(Level.SEVERE, message, cause);
        if (cause != null) {
            cause.printStackTrace(System.err);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("command", redactSensitiveArgs(args));
        response.put("stdout", sanitizeConsoleValue(stdout.toString()));
        String stderrText = sanitizeConsoleValue(stderr.toString());
        StringBuilder errorBuilder = new StringBuilder();
        if (stderrText != null && !stderrText.isEmpty()) {
            errorBuilder.append(stderrText).append("\n\n");
        }
        errorBuilder.append(message);
        if (cause != null) {
            errorBuilder.append("\n").append(stackTraceToString(cause));
        }
        response.put("stderr", errorBuilder.toString());
        response.put("exitCode", -1);
        return response;
    }

    private String sanitizeConsoleValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replace("\r\n", "\n").replace('\r', '\n');
        normalized = ANSI_OSC_PATTERN.matcher(normalized).replaceAll("");
        normalized = ANSI_ESCAPE_PATTERN.matcher(normalized).replaceAll("");
        normalized = normalized.replace("\u001B", "");
        normalized = INVALID_XML_CHAR_PATTERN.matcher(normalized).replaceAll("");
        if (normalized.trim().isEmpty()) {
            return null;
        }
        return normalized;
    }

    private List<String> redactSensitiveArgs(List<String> args) {
        List<String> sanitized = new ArrayList<>();
        if (args == null) {
            return sanitized;
        }
        boolean maskNext = false;
        for (String arg : args) {
            if (maskNext) {
                sanitized.add("****");
                maskNext = false;
                continue;
            }
            if ("--password".equals(arg) || "-p".equals(arg) || "--token".equals(arg)) {
                sanitized.add(arg);
                maskNext = true;
                continue;
            }
            sanitized.add(arg);
        }
        if (maskNext) {
            sanitized.add("****");
        }
        return sanitized;
    }

    private void ensureTokenIfMissing(List<String> args) {
        if (args == null || args.isEmpty()) {
            return;
        }

        boolean hasToken = false;
        boolean hasUsername = false;
        boolean hasPassword = false;

        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if ("--token".equals(arg)) {
                hasToken = true;
                break;
            }
            if ("--username".equals(arg) || "-u".equals(arg)) {
                hasUsername = true;
                i++;
                continue;
            }
            if ("--password".equals(arg) || "-p".equals(arg)) {
                hasPassword = true;
                i++;
            }
        }

        if (hasToken || (hasUsername && hasPassword)) {
            return;
        }

        String sessionId = resolveSessionToken();
        if (!isBlank(sessionId)) {
            args.add("--token");
            args.add(sessionId);
        }
    }

    private String resolveSessionToken() {
        if (request == null) {
            return null;
        }

        String requestedId = request.getRequestedSessionId();
        if (!isBlank(requestedId)) {
            return requestedId;
        }

        String fallback = getSessionId();
        return isBlank(fallback) ? null : fallback;
    }

    private String buildErrorMessage(String summary, Throwable cause) {
        if (cause == null || cause.getMessage() == null || cause.getMessage().isEmpty()) {
            return summary;
        }
        return summary + ": " + cause.getMessage();
    }

    private String stackTraceToString(Throwable cause) {
        StringWriter stringWriter = new StringWriter();
        cause.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
