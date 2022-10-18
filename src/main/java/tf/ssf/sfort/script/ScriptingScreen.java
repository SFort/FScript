package tf.ssf.sfort.script;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import tf.ssf.sfort.script.util.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//A good chunk of this class was copied from github.com/unascribed/fabrication

public class ScriptingScreen extends Screen {
    protected static final Map<String, Help> default_embed = new HashMap<>();
    protected final Screen parent;
    protected final Script script;

    protected Line valMake;
    protected String last_par = "";
    protected List<Line> lines = new ArrayList<>();
    protected int cursor = 0;
    protected float sidebarScrollTarget;
    protected float sidebarScroll;
    protected float sidebarLastScroll;
    protected float sidebarHeight;
    protected float sidebar2ScrollTarget;
    protected float sidebar2Scroll;
    protected float sidebar2LastScroll;
    protected float sidebar2Height;
    protected float sidebar3ScrollTarget;
    protected float sidebar3Scroll;
    protected float sidebar3LastScroll;
    protected float sidebar3Height;
    protected boolean tick = false;
    protected int ticks;

    protected boolean didClick;
    protected boolean didRightClick;
    protected boolean renderHelp = false;
    protected boolean renderTips = false;

    protected int tooltipBlinkTicks = 0;

    protected List<Tip> tip = new ArrayList<>();

    protected boolean bufferTooltips = false;
    protected final List<Runnable> bufferedTooltips = Lists.newArrayList();

    protected TextFieldWidget searchField;

    public ScriptingScreen(Text title, Screen parent, Script script) {
        super(title);
        this.parent = parent;
        this.script = script;
        setTip();
    }

    @Override
    public void init(){
        super.init();
        searchField = new TextFieldWidget(textRenderer, 1, 1, 128, 14, searchField, Text.of("Search"));
        searchField.setChangedListener((s) -> s = s.trim());
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
        matrix.push();
        if (client.world == null) this.renderBackground(matrix);
        else renderWorldBackground(matrix);
        if (renderHelp) drawHelp(matrix);
        else {
            drawBackgroundShade(matrix, mouseX, mouseY, delta);
            drawForeground(matrix, mouseX, mouseY, delta);
        }
        matrix.pop();
    }
    
    protected void renderWorldBackground(MatrixStack matrix){
        fill(matrix, 0, 0, width, height, 0x44000000);
    }

    protected void clearTip(){
        sidebarScrollTarget = -20;
        tip = new ArrayList<>();
        last_par = "";
    }
    protected void setTip(String name, List<Supplier<Set<String>>> par){
        clearTip();
        for (Supplier<Set<String>> pa : par) {
            last_par += name;
            tip.addAll(pa.get().stream().map(p -> new Tip(p, "", new ArrayList<>(), "", null)).collect(Collectors.toSet()));
        }
    }

    protected void setTip(){
        setTip(getCursorHelp());
    }
    protected void setTip(Help help){
        setTip(Help.recurseImported(help, new HashSet<>()));
    }
    protected void setTip(Map<String, String> help){
        clearTip();
        for (Map.Entry<String, String> as : help.entrySet()) {
            String os = as.getKey();
            int colon = os.indexOf(':');
            List<Supplier<Set<String>>> val = new ArrayList<>();
            StringBuilder valName = new StringBuilder();
            Help embed = null;
            if (colon != -1) {
                String arg = os.substring(colon+1);
                os = os.substring(0, colon);
                if (os.startsWith("~")){
                    if (script.embedable != null) embed = script.embedable.getOrDefault(arg, null);
                    if (embed == null) continue;
                    os = os.substring(1);
                    int i = os.indexOf('~');
                    if (i != -1){
                        for (String s :os.substring(i+1).split(" ")){
                            val.add(script.parameters.getSupplier(s));
                            valName.append(s);
                        }
                        os = os.substring(0, i);
                    }
                }else for (String s :arg.split(" ")){
                    val.add(script.parameters.getSupplier(s));
                    valName.append(s);
                }
            }
            tip.add(new Tip(os.split(" "), as.getValue(), val, valName.toString(), embed));
        }
    }

    protected void negateVal(){
        if (!lines.isEmpty() && !isCloseBracket(lines.get(cursor))) {
            lines.get(cursor).negate();
        }
    }
    protected void pushValMake(Tip os){
        if (valMake == null) {
            if (os.par.size() > 0) {
                setTip(os.parName, os.par);
                valMake = new Line(os, getCursorHelp(os));
                searchField.setTextFieldFocused(true);
            } else {
                Help h2 = getCursorHelp();
                if (lines.size()>0 && lines.get(cursor).tip.embed != null) cursor++;
                lines.add(cursor+(lines.isEmpty()?0:1), new Line(os, getCursorHelp(os)));
                if (lines.size()>1) cursor++;
                if (os.embed != null) bracketLine('[', ']', h2);
                setTip();
                valMake = null;
            }
            searchField.setText("");
        }else {
           pushValMake(os.name[0]);
        }
    }
    protected void pushValMake(String os){
        if (valMake == null) {
            if (lines.size()>0 && lines.get(cursor).tip.embed != null) cursor++;
            lines.add(cursor+(lines.isEmpty()?0:1), new Line(new Tip(os, "", new ArrayList<>(), "", null), getCursorHelp()));
            if (lines.size()>1) cursor++;
        }else {
            Help h2 = getCursorHelp();
            boolean bl = valMake.tip.embed == null;
            valMake.val = bl ? os : os.replace(':', ';');
            if (lines.size()>0 && lines.get(cursor).tip.embed != null) cursor++;
            lines.add(cursor+(lines.size()>0?1:0), valMake);
            if (lines.size()>1) cursor++;
            if (!bl) bracketLine('[', ']', h2);
            setTip(valMake.help);
            valMake = null;
        }
        searchField.setText("");
    }
    protected Help getCursorHelp(Tip os) {
        return getCursorHelp(os.embed);
    }
    protected Help getCursorHelp(Help embed) {
        return embed == null ? getCursorHelp() : embed;
    }
    protected Help getCursorHelp() {
        if (!lines.isEmpty()){
            return lines.get(cursor).help;
        }
        return script.help;
    }
    protected void drawBackgroundShade(MatrixStack matrix, int mouseX, int mouseY, float delta) {
        fill(matrix, width-12, 10, width-10, (ticks>>3)%8+2, -1);
        fill(matrix, 0, 16, 130, height, 0x44000000);
    }
    protected void drawOptionButtons(MatrixStack matrix, int mouseX, int mouseY, float delta) {
        if (drawToggleButton(matrix, width-16, 1, 10, 10, null, "Switch through alternative names", mouseX, mouseY, tick)){
            ticks = 0;
            tick = !tick;
        }
        if (drawToggleButton(matrix, width-28, 1, 10, 10, "?", "Show descriptions as tooltips", mouseX, mouseY, renderTips)){
            renderTips = !renderTips;
        }
        if (drawButton(matrix, width-46, 1, 16, 10, "F1", "Show Help", mouseX, mouseY)){
            renderHelp = !renderHelp;
        }
    }
    protected void drawTips(MatrixStack matrix, int mouseX, int mouseY, float delta){
        boolean shortened = 400>width;
        float scroll = sidebarHeight < height ? 0 : ((sidebarScroll-sidebarLastScroll)*client.getTickDelta()+sidebarLastScroll);
        scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
        float y = 22-scroll;
        int newHeight = 8;
        for (Tip os : tip) {
            {
                final String match = searchField.getText().toLowerCase();
                if (!os.desc.toLowerCase().contains(match) && Arrays.stream(os.name).noneMatch(s -> s.toLowerCase().contains(match)))
                    continue;
            }
            String s = os.name[os.name.length == 1? 0 : (ticks>>5) %os.name.length];
            s = formatTitleCase(s);
            if (os.par.size() == 0) {
                if (os.embed != null) s = "~" + s + ":";
            }else {
                if (os.embed != null) s = "~" + s + "~";
                else s = s + ":";
            }
            int thisHeight = 0;
            float startY = y;
            thisHeight += 12;
            {
                int x = 8;
                int line = 0;
                for (String word : Splitter.on(CharMatcher.whitespace()).split(s)) {
                    if (textRenderer.getWidth(word)+x > 115 && line == 0) {
                        x = 8;
                        y += 12;
                        newHeight += 12;
                        line = 1;
                    }
                    if(y<22 || (shortened && y>height-30)) continue;
                    x = textRenderer.drawWithShadow(matrix, word+" ", x, y, -1);
                }
                if(line == 0 && Arrays.stream(os.name).anyMatch(st -> textRenderer.getWidth(st)>115)){
                    y += 12;
                    newHeight += 12;
                }
                y += 12;
                thisHeight += 12;
            }
            if (mouseX >= 0 && mouseX <= 130 && mouseY > startY-4 && mouseY < y && mouseY > 22 && (!shortened || y < height - 20)) {
                if (didClick) {
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
                    pushValMake(os);
                }
                if (renderTips && !os.desc.isEmpty()) {
                    renderTooltip(matrix, Text.of(os.desc), mouseX, mouseY);
                }
            }
            thisHeight += 8;

            y += 8;
            newHeight += thisHeight;
            if(y>height){
                int xOffset = shortened && y>width-20 ? 27 : 7;
                textRenderer.draw(matrix, "v", 0, height-xOffset, -1);
                textRenderer.draw(matrix, "v", 124, height-xOffset, -1);
                break;
            }
        }
        sidebarHeight = newHeight;
        if(sidebarScroll>0){
            textRenderer.draw(matrix, "^", 0, 17, -1);
            textRenderer.draw(matrix, "^", 124, 17, -1);
        }
    }
    protected void drawScript(MatrixStack matrix, int mouseX, int mouseY, float delta){
        float scroll = sidebar2Height < height ? 0 : ((sidebar2Scroll-sidebar2LastScroll)*client.getTickDelta()+sidebar2LastScroll);
        scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
        float y = 22-scroll;
        int newHeight = 8;
        int x = 140;
        if(valMake != null && lines.isEmpty()){
            textRenderer.drawWithShadow(matrix, valMake + " §7"+last_par, x, y, -2000);
            if(didClick && mouseX < width && mouseX > 132 && mouseY < y+12 && mouseY > 20){
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
                valMake = null;
                setTip();
            }
        }
        for (int i = 0; i< lines.size(); i++) {
            Line s = lines.get(i);
            int thisHeight = 0;
            float startY = y;
            thisHeight += 12;
            if (isCloseBracket(s)) x -= 8;
            int lx = 0;
            if(cursor == i){
                if(valMake != null) lx = textRenderer.drawWithShadow(matrix, valMake + " §7"+last_par, x-8, y, -2000) - x +16;
                textRenderer.drawWithShadow(matrix, ">", x+lx-8, y, -1);
            }
            if(y>20 && y<height-30) textRenderer.drawWithShadow(matrix, s.toString(), x+lx, y, -1);
            if (isOpenBracket(s)) x += 8;
            y += 12;
            thisHeight += 12;
            if (didClick) {
                if (mouseX < width && mouseX > 132 && mouseY > startY-4 && mouseY < y && mouseY > 20) {
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
                    if (didRightClick){
                        int rmStart = 0;
                        int rmEnd = 0;
                        if (lines.get(i).tip.embed != null){
                            if (i+2 < lines.size() && isOpenBracket(i+1) && isCloseBracket(i+2))
                                rmEnd = 3;
                        }else if(!isBracket(i)) {
                            rmEnd = 1;
                        }else if (isOpenBracket(i)){
                            if (i > 0 && i+1 < lines.size() && lines.get(i-1).tip.embed != null && isCloseBracket(i+1)){
                                rmStart = -1;
                                rmEnd = 2;
                            }else if (i+1 < lines.size() && isCloseBracket(i+1)){
                                rmEnd = 2;
                            }
                        }else{
                            if (i > 1 && lines.get(i-2).tip.embed != null && isOpenBracket(i-1)){
                                rmStart = -2;
                                rmEnd = 1;
                            }else if (i > 0 && isOpenBracket(i-1)){
                                rmStart = -1;
                                rmEnd = 1;
                            }
                        }
                        lines.subList(rmStart+i,rmEnd+i).clear();
                        if (i+rmStart <= cursor) cursor+=rmStart-rmEnd;
                        if (cursor<0) cursor = 0;

                    } else{
                        valMake = null;
                        cursor = i;
                    }
                    setTip();
                }
            }
            thisHeight += 8;

            y += 8;
            newHeight += thisHeight;
            if(y>height-31){
                textRenderer.draw(matrix, "v", 130, height-27, -1);
                textRenderer.draw(matrix, "v", width-8, height-27, -1);
                break;
            }
        }
        sidebar2Height = newHeight;
        if(sidebar2Scroll>0){
            textRenderer.draw(matrix, "^", 130, 17, -1);
            textRenderer.draw(matrix, "^", width-8, 17, -1);
        }

    }
    protected void drawButtons(MatrixStack matrix, int mouseX, int mouseY, float delta){
        if (drawButton(matrix, width-50, height-20, 50, 20, "Done", null, mouseX, mouseY)) {
            close();
        }
        int x = 130;
        if (400>width){
            x=0;
        }
        if (drawButton(matrix, x, height - 20, 20, 20, "!", "Negate selected", mouseX, mouseY))
            negateVal();
        x += 20;
        if (drawButton(matrix, x, height - 20, 20, 20, "[]", "AND", mouseX, mouseY))
            bracketLine('[', ']');
        x += 20;
        if (drawButton(matrix, x, height - 20, 20, 20, "()", "OR", mouseX, mouseY))
            bracketLine('(', ')');
        x += 20;
        if (drawButton(matrix, x, height - 20, 20, 20, "{}", "XOR", mouseX, mouseY))
            bracketLine('{', '}');
    }
    protected void drawScriptButtons(MatrixStack matrix, int mouseX, int mouseY, float delta){
        int x = width-100;
        if (script.save != null) {
            if (drawButton(matrix, x, height - 20, 50, 20, "Save", null, mouseX, mouseY))
                script.save.accept(unloadScript());
            x -= 50;
        }
        if (script.apply != null) {
            if (drawButton(matrix, x, height - 20, 50, 20, "Apply", null, mouseX, mouseY))
                script.apply.accept(unloadScript());
            x -= 50;
        }
        if (script.load != null) {
            if (drawButton(matrix, x, height - 20, 50, 20, "Load", null, mouseX, mouseY))
                loadScript(script.load.get());
        }
    }
    protected void drawForeground(MatrixStack matrix, int mouseX, int mouseY, float delta) {
        drawOptionButtons(matrix, mouseX, mouseY, delta);
        textRenderer.drawWithShadow(matrix, script.name, 136, 4, -1);
        drawTips(matrix, mouseX, mouseY, delta);
        drawScript(matrix, mouseX, mouseY, delta);

        bufferTooltips = true;

        RenderSystem.setShaderColor(1, 1, 1, 0.2f);
        searchField.render(matrix, mouseX, mouseY, delta);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        drawButtons(matrix, mouseX, mouseY, delta);
        drawScriptButtons(matrix, mouseX, mouseY, delta);
        super.render(matrix, mouseX, mouseY, delta);

        didClick = false;
        didRightClick = false;
        bufferTooltips = false;
        for (Runnable r : bufferedTooltips) {
            r.run();
        }
        bufferedTooltips.clear();

        matrix.pop();
    }

    protected void drawHelp(MatrixStack matrices) {
        String[] hlp = new String[]{
                "Keybinds:",
                "\tF1 - Toggles Help",
                "\tF5 - Load Script",
                "\tF6 - Apply Script",
                "\tF7 - Save Script",
                "\tCtrl - F - Selects Search field",
                "\tRMB - Clears Search / Removes elements",
                "\tESC - Closes the UI",
                "",
                "Buttons:",
                "\t! - Negates selected condition",
                "\t[] - AND on conditions inside",
                "\t() - OR on conditions inside",
                "\t{} - XOR on conditions inside",
                "",
                "Note that some suggestions like BiomeID can only be obtained while in a world"
        };
        float scroll = sidebar3Height < height ? 0 : ((sidebar3Scroll-sidebar3LastScroll)*client.getTickDelta()+sidebar3LastScroll);
        sidebar3Height = 20;
        scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
        float y = 22-scroll;
        for (String h : hlp) {
            int x = 16;
            for (String word : Splitter.on(CharMatcher.whitespace()).split(h)) {
                if (textRenderer.getWidth(word) + x > width) {
                    x = 16;
                    y += 12;
                    sidebar3Height += 12;
                }
                if (y < 4) continue;
                x = textRenderer.drawWithShadow(matrices, word + " ", x, y, -1);
            }
            sidebar3Height+=20;
            y+=20;
        }
        textRenderer.drawWithShadow(matrices, "|", 6, (scroll/(sidebar3Height-height))*(height-20)+5, -1);
    }
    protected String unloadScript(){
        StringBuilder out = new StringBuilder();
        for (int i = 0; i< lines.size(); i++) {
            Line s = lines.get(i);
            out.append(s);
            if (!isOpenBracket(s) && s.tip.embed == null && i+1<lines.size() && !isCloseBracket(lines.get(i+1)))
                out.append(';');
        }
        return out.toString();
    }
    //TODO this should have probably used pushValMake
    public void loadScript(String in){
        lines.clear();
        cursor = 0;
        boolean negate = false;
        Help prev_help = null;
        for (int i = 0; i<in.length(); i++) {
            char chr = in.charAt(i);
            switch (chr){
                case '!':
                    negate = !negate;
                    break;
                case '~':
                    int colon = in.indexOf(':', i);
                    int tilde = findChr(in, '~', i+1, colon);
                    boolean noTilde = tilde == -1;
                    String name = in.substring(i + 1, noTilde ? colon : tilde);
                    AtomicReference<Help> hlp = new AtomicReference<>();
                    Help.recurseAcceptor(getCursorHelp(), new HashSet<>(),
                            s -> {
                                final Triple<String, List<String>, String> triple = Help.dismantle(s.getKey());
                                if (hlp.get() != null || !triple.a.startsWith("~") || !triple.b.contains(name)) return;
                                String sc = triple.c;
                                int si = sc.indexOf(':');
                                hlp.set(script.embedable.get(si == -1 ? sc : sc.substring(si+1)));
                    });

                    prev_help = getCursorHelp();
                    if(!lines.isEmpty())cursor++;
                    lines.add(cursor,
                            new Line(
                                    new Tip(name, "", noTilde ? Collections.emptyList() : Collections.singletonList(null), "", hlp.get()),
                                    hlp.get(),
                                    noTilde ? null : in.substring(tilde+1, colon),
                                    negate
                            )
                    );
                    i = colon;
                    if(!isOpenBracket(in.charAt(i+1))) {
                        bracketLine('[', ']', prev_help);
                        prev_help = null;
                    }
                    break;
                case '[':
                    bracketLine('[', ']', prev_help, negate);
                    break;
                case '{':
                    bracketLine('{', '}', prev_help, negate);
                    break;
                case '(':
                    bracketLine('(', ')', prev_help, negate);
                    break;
                case ']': case '}': case')':
                    cursor++;
                    break;
                default:
                    int scolon = findEndChr(in, i, in.length());
                    int colond = findChr(in, ':', i, scolon);
                    if (i != (colond == -1 ? scolon : colond))
                        lines.add(cursor+(lines.isEmpty()?0:1), new Line(new Tip(in.substring(i, colond == -1 ? scolon : colond), "", new ArrayList<>(), "", null), getCursorHelp(), colond == -1 ? null : in.substring(colond, scolon), negate));
                    negate = false;
                    i = scolon;
                    if (lines.size()>1) cursor++;
                    break;
            }
            if(chr != '!'){
                negate = false;
            }

        }
        setTip();
    }
    protected int findChr(String str, int chr, int from, int to){
        for(int i = from; i < to; ++i)
            if (str.charAt(i) == chr) return i;
        return -1;
    }
    protected int findEndChr(String str, int from, int to){
        for(int i = from; i < to; ++i) {
            char chr = str.charAt(i);
            if (chr == ';' || isCloseBracket(chr)) return i;
        }
        return to;
    }
    protected boolean drawButton(MatrixStack matrix, int x, int y, int w, int h, String text, String desc, int mouseX, int mouseY) {
        boolean hovering = mouseIn(x, y, w, h, mouseX, mouseY);
        if (hovering) {
            if (didClick) {
                x+=2;
                y+=2;
                w-=4;
                h-=4;
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
            }
            drawOutlineBox(matrix, x, y, w, h, -1);
            if (desc != null && renderTips)
                renderTooltip(matrix, Text.of(desc), mouseX, mouseY);
        }
        if (text != null)
            textRenderer.drawWithShadow(matrix, text, x + ((w - textRenderer.getWidth(text)) / 2f), y + ((h - 8) / 2f), -1);
        return hovering && didClick;
    }
    protected boolean drawToggleButton(MatrixStack matrix, int x, int y, int w, int h, String text, String desc, int mouseX, int mouseY, boolean toggled) {
        boolean hovering = mouseIn(x, y, w, h, mouseX, mouseY);
        if (hovering) {
            if (didClick) {
                x+=2;
                y+=2;
                w-=4;
                h-=4;
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
            }
            if (desc != null && renderTips)
                renderTooltip(matrix, Text.of(desc), mouseX, mouseY);
        }
        if (text != null)
            textRenderer.drawWithShadow(matrix, text, x + ((w - textRenderer.getWidth(text)) / 2), y + ((h - 8) / 2), -1);
        if (hovering^toggled) drawOutlineBox(matrix, x, y, w, h, -1);

        return hovering && didClick;
    }
    protected boolean mouseIn(int x, int y, int w, int h, float mouseX, float mouseY){
        return mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+h;
    }
    protected void drawOutlineBox(MatrixStack matrix, int x, int y, int w, int h, int color){
        fill(matrix, x, y, x+w, y+1, color);
        fill(matrix, x, y, x+1, y+h, color);
        fill(matrix, x, y+h-1, x+w, y+h, color);
        fill(matrix, x+w-1, y, x+w, y+h, color);
    }
    public static String formatTitleCase(String in) {
        String[] pieces = in.toLowerCase().split("[_ ;:/]");
        StringBuilder result = new StringBuilder();
        for (String s : pieces) {
            if (s == null)
                continue;
            String t = s.trim();
            if (t.isEmpty())
                continue;
            result.append(Character.toUpperCase(t.charAt(0)));
            if (t.length() > 1)
                result.append(t.substring(1));
            result.append(" ");
        }
        return result.toString().trim();
    }


    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void tick() {
        if(tick)ticks++;
        super.tick();
        if (sidebarHeight > height) {
            sidebarLastScroll = sidebarScroll;
            sidebarScroll += (sidebarScrollTarget-sidebarScroll)/2;
            if (sidebarScrollTarget < 0) sidebarScrollTarget /= 2;
            float h = sidebarHeight-height;
            if (sidebarScrollTarget > h) sidebarScrollTarget = h+((sidebarScrollTarget-h)/2);
        }
        if (sidebar2Height > height) {
            sidebar2LastScroll = sidebar2Scroll;
            sidebar2Scroll += (sidebar2ScrollTarget-sidebar2Scroll)/2;
            if (sidebar2ScrollTarget < 0) sidebar2ScrollTarget /= 2;
            float h = sidebar2Height-height;
            if (sidebar2ScrollTarget > h) sidebar2ScrollTarget = h+((sidebar2ScrollTarget-h)/2);
        }
        if (sidebar3Height > height) {
            sidebar3LastScroll = sidebar3Scroll;
            sidebar3Scroll += (sidebar3ScrollTarget-sidebar3Scroll)/2;
            if (sidebar3ScrollTarget < 0) sidebar3ScrollTarget /= 2;
            float h = sidebar3Height-height;
            if (sidebar3ScrollTarget > h) sidebar3ScrollTarget = h+((sidebar3ScrollTarget-h)/2);
        }

        if (tooltipBlinkTicks > 0) {
            tooltipBlinkTicks--;
        }
        searchField.tick();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if(renderHelp){
            sidebar3ScrollTarget -= amount*20;
            return super.mouseScrolled(mouseX, mouseY, amount);
        }
        if (mouseX <= 120) {
            sidebarScrollTarget -= amount*20;
        }else if (mouseY > 22){
            sidebar2ScrollTarget -= amount*20;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(renderHelp) return super.mouseClicked(mouseX, mouseY, button);

        if (button == 0 || button == 1) {
            if (button == 1) didRightClick = true;
            didClick = true;
        }
        if (mouseX<=128 && mouseY<=14 && button == 1) searchField.setText("");
        searchField.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (renderHelp) return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        searchField.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (renderHelp) super.mouseMoved(mouseX, mouseY);
        searchField.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (renderHelp) return super.mouseReleased(mouseX, mouseY, button);
        searchField.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (renderHelp) return super.charTyped(chr, modifiers);
        searchField.charTyped(chr, modifiers);
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 290/*F1*/) renderHelp = !renderHelp;
        if (renderHelp) return super.keyPressed(keyCode, scanCode, modifiers);

        switch (keyCode){
            case GLFW.GLFW_KEY_ENTER: case GLFW.GLFW_KEY_KP_ENTER:
                if (searchField.isActive()) pushValMake(searchField.getText());
                break;
            case GLFW.GLFW_KEY_F:
                if (hasControlDown()) searchField.setTextFieldFocused(true);
                break;
            case GLFW.GLFW_KEY_F5:
                if (script.load != null) loadScript(script.load.get());
                break;
            case GLFW.GLFW_KEY_F6:
                if (script.apply != null) script.apply.accept(unloadScript());
                break;
            case GLFW.GLFW_KEY_F7:
                if (script.save != null) script.save.accept(unloadScript());
                break;
        }

        searchField.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (renderHelp) return super.keyReleased(keyCode, scanCode, modifiers);
        searchField.keyReleased(keyCode, scanCode, modifiers);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderOrderedTooltip(MatrixStack matrices, List<? extends OrderedText> lines, int x, int y) {
        if (!lines.isEmpty()) {
            if (bufferTooltips) {
                final int yf = y;
                bufferedTooltips.add(() -> renderOrderedTooltip(matrices, lines, x, yf));
                return;
            }
            if (y < 20) {
                y += 20;
            }
            int maxWidth = 0;

            for (OrderedText line : lines) {
                int width = textRenderer.getWidth(line);
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }

            int innerX = x + 12;
            int innerY = y - 12;
            int totalHeight = 8;
            if (lines.size() > 1) {
                totalHeight += (lines.size() - 1) * 10;
            }

            if (innerX + maxWidth > width) {
                innerX -= 28 + maxWidth;
            }

            if (innerY + totalHeight + 6 > height) {
                innerY = height - totalHeight - 6;
            }

            matrices.push();
            fill(matrices, innerX-3, innerY-3, innerX+maxWidth+3, innerY+totalHeight+3, 0xAA000000);
            VertexConsumerProvider.Immediate vcp = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            matrices.translate(0, 0, 400);

            for (OrderedText line : lines) {
                if (line != null) {
                    textRenderer.draw(line, innerX, innerY, -1, false, matrices.peek().getPositionMatrix(), vcp, false, 0, 0xF000F0);
                }
                innerY += 10;
            }

            vcp.draw();
            matrices.pop();
        }
    }
    protected void bracketLine(char c1, char c2, Help h2){
        Help help = getCursorHelp();
        if(!lines.isEmpty()) cursor++;
        lines.add(cursor, new Line(new Tip(String.valueOf(c1), "", new ArrayList<>(), "", null), help, null));
        lines.add(cursor + 1, new Line(new Tip(String.valueOf(c2), "", new ArrayList<>(), "", null), h2, null));
    }
    protected void bracketLine(char c1, char c2){
        Help help = getCursorHelp();
        if(!lines.isEmpty()) cursor++;
        lines.add(cursor, new Line(new Tip(String.valueOf(c1), "", new ArrayList<>(), "", null), help, null));
        lines.add(cursor + 1, new Line(new Tip(String.valueOf(c2), "", new ArrayList<>(), "", null), help, null));
    }
    protected void bracketLine(char c1, char c2, Help h2, boolean negate){
        Help help = getCursorHelp();
        if(!lines.isEmpty()) cursor++;
        lines.add(cursor, new Line(new Tip(String.valueOf(c1), "", new ArrayList<>(), "", null), help, null, negate));
        lines.add(cursor + 1, new Line(new Tip(String.valueOf(c2), "", new ArrayList<>(), "", null), h2 == null? help : h2, null));
    }
    protected boolean isBracket(){
        return isBracket(cursor);
    }
    protected boolean isBracket(int in){
        return !lines.isEmpty() && isBracket(lines.get(in));
    }
    protected boolean isBracket(Line in){
        return isBracket(in.tip.name[0]);
    }
    protected boolean isBracket(String in){
        return isBracket(in.charAt(0));
    }
    protected boolean isBracket(char in){
        return isOpenBracket(in) || isCloseBracket(in);
    }
    protected boolean isOpenBracket(int in){
        return !lines.isEmpty() && isOpenBracket(lines.get(in));
    }
    protected boolean isOpenBracket(Line in){
        return isOpenBracket(in.tip.name[0]);
    }
    protected boolean isOpenBracket(String in){
        return isOpenBracket(in.charAt(0));
    }
    protected boolean isOpenBracket(char in){
        return in == '[' || in == '(' || in == '{';
    }
    protected boolean isCloseBracket(int in){
        return !lines.isEmpty() && isCloseBracket(lines.get(in));
    }
    protected boolean isCloseBracket(Line in){
        return isCloseBracket(in.tip.name[0]);
    }
    protected boolean isCloseBracket(String in){
        return isCloseBracket(in.charAt(0));
    }
    protected boolean isCloseBracket(char in){
        return in == ']' || in == ')' || in == '}';
    }

    public static class Script {
        public String name;
        public Help help;
        public Consumer<String> save;
        public Consumer<String> apply;
        public Supplier<String> load;
        public Map<String, Help> embedable;
        public Parameters parameters;

        public Script(String name, Help help, Consumer<String> save, Consumer<String> apply, Supplier<String> load, Map<String, Help> embeddable, Parameters parameters){
            this.name = name;
            this.help = help;
            this.save = save;
            this.apply = apply;
            this.load = load;
            this.embedable = embeddable;
            this.parameters = parameters;
        }

        public Script(String name, Help help, Consumer<String> save, Consumer<String> apply, Supplier<String> load, Map<String, Help> embeddable){
            this(name, help, save, apply, load, embeddable, Default.PARAMETERS);
        }

        public Script(String name, Help help){
            this(name, help, null, null, null, getDefaultEmbed());
        }
    }

    protected static class Tip {
        public String[] name;
        public String desc;
        public List<Supplier<Set<String>>> par;
        public String parName;
        public Help embed;

        Tip(String[] name, String desc, List<Supplier<Set<String>>> par, String parName, Help embed){
            this.name = name;
            this.desc = desc;
            this.par = par;
            this.parName = parName;
            this.embed = embed;
        }

        Tip(String name, String desc, List<Supplier<Set<String>>> par, String parName, Help embed){
            this(new String[]{name}, desc, par, parName, embed);
        }

        @Override
        public String toString() {
            return  (embed == null ? "" : "~")+
                    name[0] +
                    (par.size()>0 ? embed == null ? ":" : "~" : embed == null ? "" : ":");
        }
    }
    protected static class Line {
        final Tip tip;
        final Help help;
        String val;
        boolean negate = false;
        Line (Tip tip, Help help, String val){
            this.val = val;
            this.help = help;
            this.tip = tip;
        }
        Line (Tip tip, Help help, boolean negate){
            this(tip, help, null);
            this.negate = negate;
        }
        Line (Tip tip, Help help, String val, boolean negate){
            this(tip, help, val);
            this.negate = negate;
        }
        Line(Tip tip, Help help){
            this(tip, help, null);
        }
        void negate(){
            negate = !negate;
        }

        @Override
        public String toString() {
            return  (negate ? "!" : "") +
                    tip +
                    (val !=null? tip.embed == null ? val : val + ':' : "")
                    ;
        }
    }

    public static Map<String, Help> getDefaultEmbed(){
        return default_embed;
    }
    static {
        for (Map.Entry<String, PredicateProvider<?>> entry : Default.getDefaultMap().entrySet()) {
            PredicateProvider<?> hlp = entry.getValue();
            if (hlp instanceof Help) {
                default_embed.put(entry.getKey(), (Help) hlp);
            }
        }
    }
}
