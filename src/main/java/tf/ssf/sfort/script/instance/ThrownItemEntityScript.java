package tf.ssf.sfort.script.instance;

import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;
import tf.ssf.sfort.script.util.DefaultParsers;

import java.util.function.Predicate;

public class ThrownItemEntityScript extends AbstractExtendablePredicateProvider<ThrownItemEntity> {
	public ThrownItemEntityScript() {
		help.put("~thrown_item item:ITEM_STACK", "Require the thrown item stack to match");
	}

	@Override
	public Predicate<ThrownItemEntity> getLocalEmbed(String in, String script){
		switch (in) {
			case "thrown_item" : case "item" : {
				final Predicate<ItemStack> predicate = DefaultParsers.ITEM_STACK_PARSER.parse(script);
				if (predicate == null) return null;
				return entity -> predicate.test(entity.getStack());
			}
			default : return null;
		}
	}
}
