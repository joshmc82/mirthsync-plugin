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

package com.saga_it.mirthsync_plugin.server;

import com.kaurpalang.mirth.annotationsplugin.annotation.MirthServerClass;
import com.saga_it.mirthsync_plugin.shared.PluginConstants;
import com.saga_it.mirthsync_plugin.shared.PluginPermissions;
import com.saga_it.mirthsync_plugin.shared.interfaces.MirthSyncApi;
import com.mirth.connect.client.core.api.util.OperationUtil;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.plugins.ServicePlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@MirthServerClass
public class MirthSyncServicePlugin implements ServicePlugin {

    @Override
    public void init(Properties properties) {
    }

    @Override
    public void update(Properties properties) {
        // We don't need to do anything here.
    }

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        ExtensionPermission getPermission = new ExtensionPermission (
                PluginConstants.PLUGIN_POINTNAME,
                PluginPermissions.GETSTH,
                "Allows retrieving configuration data from the plugin",
                OperationUtil.getOperationNamesForPermission(PluginPermissions.GETSTH, MirthSyncApi.class), new String[] {}
        );

        ExtensionPermission helpPermission = new ExtensionPermission(
                PluginConstants.PLUGIN_POINTNAME,
                PluginPermissions.SHOWHELP,
                "Allows retrieving MirthSync help output",
                OperationUtil.getOperationNamesForPermission(PluginPermissions.SHOWHELP, MirthSyncApi.class), new String[] {}
        );

        ExtensionPermission executePermission = new ExtensionPermission(
                PluginConstants.PLUGIN_POINTNAME,
                PluginPermissions.EXECUTE,
                "Allows executing mirthsync commands",
                OperationUtil.getOperationNamesForPermission(PluginPermissions.EXECUTE, MirthSyncApi.class), new String[] {}
        );

        return new ExtensionPermission[] {getPermission, helpPermission, executePermission};
    }

    @Override
    public Map<String, Object> getObjectsForSwaggerExamples() {
        return new HashMap<>();
    }

    @Override
    public String getPluginPointName() {
        return PluginConstants.PLUGIN_POINTNAME;
    }

    @Override
    public void start() {
        System.out.println("Starting MirthSync Plugin");
    }

    @Override
    public void stop() {
        System.out.println("Stopping MirthSync Plugin");
    }

}
