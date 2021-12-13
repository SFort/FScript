package tf.ssf.sfort.script.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import tf.ssf.sfort.script.Default;
import tf.ssf.sfort.script.extended.mixin.MixinExtendedFishingBobberEntityScript;
import tf.ssf.sfort.script.extended.mixin.MixinExtendedItemScript;
import tf.ssf.sfort.script.extended.mixin.MixinExtendedLivingEntityScript;
import tf.ssf.sfort.script.extended.mixin.MixinExtendedServerPlayerEntityScript;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Config implements IMixinConfigPlugin {
    public static boolean extended = false;

    public static final List<String> defaultDesc = Arrays.asList(
            "^-Enable Mixin Extended Flags [false] true | false"
    );
    @Override
    public void onLoad(String mixinPackage) {
        Path confFile = FabricLoader.getInstance().getConfigDir().resolve("lib-FScript.conf");
        List<String> la = new LinkedList<>();
        try {
            la = Files.readAllLines(confFile);
        } catch (IOException ignored) {}
        String[] ls = la.toArray(new String[Math.max(la.size(), defaultDesc.size() * 2)|1]);
        final int hash = Arrays.hashCode(ls);
        for (int i = 0; i<defaultDesc.size();++i)
            ls[i*2+1]= defaultDesc.get(i);

        try{
            int i = 0;
            try {
                extended =ls[i].contains("true");
            }catch (Exception ignored){}
            ls[i]=String.valueOf(extended);
        }catch (Exception ignored){}

        if (extended){
            Default.SERVER_PLAYER_ENTITY.addProvider(new MixinExtendedServerPlayerEntityScript(), 1000);
            Default.LIVING_ENTITY.addProvider(new MixinExtendedLivingEntityScript(), 1000);
            Default.ITEM.addProvider(new MixinExtendedItemScript(), 1000);
            Default.FISHING_BOBBER_ENTITY.addProvider(new MixinExtendedFishingBobberEntityScript(), 1000);
        }

        if (hash != Arrays.hashCode(ls)) {
            try {
                Files.write(confFile, Arrays.asList(ls));
            } catch (IOException ignored) {}
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return extended;
    }

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
