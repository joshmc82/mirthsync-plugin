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

package com.saga_it.mirthsync_plugin.client;

import com.kaurpalang.mirth.annotationsplugin.annotation.MirthClientClass;
import com.saga_it.mirthsync_plugin.client.panel.MainSettingsPanel;
import com.saga_it.mirthsync_plugin.shared.PluginConstants;
import com.mirth.connect.client.ui.AbstractSettingsPanel;
import com.mirth.connect.plugins.SettingsPanelPlugin;

@MirthClientClass
public class MirthSyncSettingsPlugin extends SettingsPanelPlugin {

    private MainSettingsPanel mainSettingsPanel;

    public MirthSyncSettingsPlugin(String name) {
        super(name);
    }

    @Override
    public AbstractSettingsPanel getSettingsPanel() {
        return this.mainSettingsPanel;
    }

    @Override
    public String getPluginPointName() {
        return PluginConstants.PLUGIN_POINTNAME;
    }

    @Override
    public void start() {
        System.out.println("Loading MirthSync settings panel");
        this.mainSettingsPanel = new MainSettingsPanel();
    }

    @Override
    public void stop() {

    }

    @Override
    public void reset() {

    }

}
