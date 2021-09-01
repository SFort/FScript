package tf.ssf.sfort.script.mixin_extended;

import net.minecraft.item.Item;
import net.minecraft.util.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemExtended {
    @Accessor("rarity")
    Rarity fscript$rarity();
}