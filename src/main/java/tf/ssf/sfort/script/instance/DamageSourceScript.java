package tf.ssf.sfort.script.instance;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;
import tf.ssf.sfort.script.util.DefaultParsers;

import java.util.function.Predicate;

public class DamageSourceScript extends AbstractExtendablePredicateProvider<DamageSource> {

	public DamageSourceScript() {
		help.put("is_scaled_with_difficulty scaled_with_difficulty", "Damage source scales with difficulty");
		help.put("is_source_creative_player source_creative_player", "Damage source must be from a creative player");
		help.put("name damage_source_name:DamageSourceID", "Damage source must have a matching name");
		help.put("type damage_source_type:DamageSourceType", "Damage source must be the specified type");
		help.put("~source:ENTITY", "Damage source's source entity must match");
		help.put("~attacker:ENTITY", "Damage source's attacker must match");
	}

	@Override
	public Predicate<DamageSource> getLocalPredicate(String in, String val){
		switch (in){
			case "name": case "damage_source_name": return ds -> val.equals(ds.getName());
			case "type": case "damage_source_type": {
				TagKey<DamageType> tag = TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(val));
				return ds -> ds.isIn(tag);
			}
			default: return null;
		}
	}

	@Override
	public Predicate<DamageSource> getLocalEmbed(String in, String script){
		switch (in){
			case "source":
			case "attacker": {
				final Predicate<Entity> predicate = DefaultParsers.ENTITY_PARSER.parse(script);
				if (predicate == null) return null;
				switch (in) {
					case "source": return ds -> ds.getSource() != null && predicate.test(ds.getSource());
					case "attacker": return ds -> ds.getAttacker() != null && predicate.test(ds.getAttacker());
				}
			}
			default: return null;
		}
	}


	@Override
	public Predicate<DamageSource> getLocalPredicate(String in){
		switch (in){
			case "is_scaled_with_difficulty": case  "scaled_with_difficulty" : return DamageSource::isScaledWithDifficulty;
			case "is_source_creative_player": case  "source_creative_player" : return DamageSource::isSourceCreativePlayer;
			default: return null;
		}
	}

}
