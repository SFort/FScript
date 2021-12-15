package tf.ssf.sfort.script;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class FScriptInit implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricLoader.getInstance().getEntrypoints("fscript", ModInitializer.class).forEach(ModInitializer::onInitialize);
    }
}
