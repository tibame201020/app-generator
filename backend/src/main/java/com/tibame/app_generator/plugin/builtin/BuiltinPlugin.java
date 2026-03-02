package com.tibame.app_generator.plugin.builtin;

import com.tibame.app_generator.plugin.GenerationCapability;
import com.tibame.app_generator.plugin.Plugin;
import com.tibame.app_generator.plugin.PluginContext;
import com.tibame.app_generator.plugin.PluginException;

import java.util.Arrays;
import java.util.List;

public class BuiltinPlugin implements Plugin {

    private PluginContext context;

    @Override
    public String getName() {
        return "Builtin Plugin";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public void initialize(PluginContext context) throws PluginException {
        this.context = context;
        context.logInfo("Initializing Builtin Plugin (PM & SA)...");
    }

    @Override
    public void teardown() throws PluginException {
        if (context != null) {
            context.logInfo("Tearing down Builtin Plugin...");
        }
    }

    @Override
    public List<GenerationCapability> getCapabilities() {
        return Arrays.asList(new PmCapability(), new SaCapability());
    }
}
