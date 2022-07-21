package tf.ssf.sfort.script.instance;

import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import tf.ssf.sfort.script.util.AbstractExtendablePredicateProvider;
import tf.ssf.sfort.script.util.DefaultParsers;

import java.util.function.Predicate;

public class NbtElementScript extends AbstractExtendablePredicateProvider<NbtElement> {

	public NbtElementScript() {
		help.put("byte:byte", "Has to be matching byte");
		help.put("short:short", "Has to be matching short");
		help.put("int:int", "Has to be matching int");
		help.put("long:long", "Has to be matching long");
		help.put("float:float", "Has to be matching float");
		help.put("double:double", "Has to be matching double");
		help.put("string:String", "Has to be matching string");
		help.put("~nbt~String:NBT_COMPOUND", "Has to have matching nbt");
	}

	@Override
	public Predicate<NbtElement> getLocalPredicate(String in, String val){
		switch (in){
			case "byte" : {
				final byte arg = Byte.parseByte(val);
				return nbt -> nbt instanceof NbtByte && ((NbtByte)nbt).byteValue() == arg;
			}
			case "short" : {
				final short arg = Short.parseShort(val);
				return nbt -> nbt instanceof NbtShort && ((NbtShort)nbt).shortValue() == arg;
			}
			case "int" : {
				final int arg = Integer.parseInt(val);
				return nbt -> nbt instanceof NbtInt && ((NbtInt)nbt).intValue() == arg;
			}
			case "long" : {
				final long arg = Long.parseLong(val);
				return nbt -> nbt instanceof NbtLong && ((NbtLong)nbt).longValue() == arg;
			}
			case "float" : {
				final float arg = Float.parseFloat(val);
				return nbt -> nbt instanceof NbtFloat && ((NbtFloat)nbt).floatValue() == arg;
			}
			case "double" : {
				final double arg = Double.parseDouble(val);
				return nbt -> nbt instanceof NbtDouble && ((NbtDouble)nbt).doubleValue() == arg;
			}
			case "string" : {
				return nbt -> nbt instanceof NbtString && val.equals(nbt.asString());
			}
			default : return null;
		}
	}

	@Override
	public Predicate<NbtElement> getLocalEmbed(String in, String val, String script){
		switch (in){
			case "nbt": {
				final Predicate<NbtElement> predicate = DefaultParsers.NBT_ELEMENT_PARSER.parse(script);
				if (predicate == null) return null;
				return entity -> entity instanceof NbtCompound && predicate.test(((NbtCompound) entity).get(val));
			}
			default : return null;
		}
	}


}
