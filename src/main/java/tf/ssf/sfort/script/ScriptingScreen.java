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
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;
import tf.ssf.sfort.script.instance.ServerPlayerEntityScript;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//A good chunk of this class was copied from github.com/unascribed/fabrication

public class ScriptingScreen extends Screen {
    private static final Map<String, Help> default_embed = new HashMap<>();
    private final Screen parent;
    private final Script script;
    private final Map<String, Pair<String, String>> embed_help = new HashMap<>();

    private Line valMake;
    private String last_par = "";
    private List<Line> lines = new ArrayList<>();
    private int cursor = 0;
    private float sidebarScrollTarget;
    private float sidebarScroll;
    private float sidebarHeight;
    private float sidebar2ScrollTarget;
    private float sidebar2Scroll;
    private float sidebar2Height;
    private boolean tick = false;
    private int ticks;

    private boolean didClick;
    private boolean didRightClick;
    private boolean renderHelp = false;
    private boolean renderTips = false;

    private int tooltipBlinkTicks = 0;

    private List<Tip> tip = new ArrayList<>();

    private boolean bufferTooltips = false;
    private final List<Runnable> bufferedTooltips = Lists.newArrayList();

    private TextFieldWidget searchField;

    public ScriptingScreen(Text title, Screen parent, Script script) {
        super(title);
        this.parent = parent;
        this.script = script;
        Set<String> existing = new HashSet<>();
        Help.recurseAcceptor(script.help, new HashSet<>(),
                s -> {
                    final Triplet<String, List<String>, String> triple = Help.dismantle(s.getKey());
                    if (!triple.getA().startsWith("~")) return;
                    List<String> names = triple.getB();
                    names.removeIf(n -> !existing.add(n));
                    String sc = triple.getC();
                    int si = sc.indexOf('~');
                    for (String name : names)
                        embed_help.put(si ==-1 ? name : name+"~", new Pair<>(si == -1? sc : sc.substring(si), s.getValue()));
                }
        );
        setTip();
    }
    @Override
    public void init(){
        super.init();
        searchField = new TextFieldWidget(textRenderer, 1, 1, 128, 14, searchField, new LiteralText("Search"));
        searchField.setChangedListener((s) -> s = s.trim());
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
        matrix.push();
        if (client.world == null) this.renderBackground(matrix);
        else fill(matrix, 0, 0, width, height, 0x44000000);
        if (renderHelp) drawHelp(matrix);
        else drawForeground(matrix, mouseX, mouseY, delta);
        matrix.pop();
    }

    private void clearTip(){
        sidebarScrollTarget = -20;
        tip = new ArrayList<>();
        last_par = "";
    }
    private void setTip(List<Help.Parameter> par){
        clearTip();
        for (Help.Parameter pa : par) {
            last_par += pa.name;
            tip.addAll(pa.getParameters().stream().map(p -> new Tip(p, "", new ArrayList<>(), null)).collect(Collectors.toSet()));
        }
    }

    private void setTip(){
        setTip(getCursorHelp());
    }
    private void setTip(Help help){
        setTip(Help.recurseImported(help, new HashSet<>()));
    }
    private void setTip(Map<String, String> help){
        clearTip();
        for (Map.Entry<String, String> as : help.entrySet()) {
            String os = as.getKey();
            int colon = os.indexOf(':');
            List<Help.Parameter> val = new ArrayList<>();
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
                            Help.Parameter p = Help.Parameter.byName(s);
                            if (p != null) val.add(p);
                        }
                        os = os.substring(0, i);
                    }
                }else for (String s :arg.split(" ")){
                    Help.Parameter p = Help.Parameter.byName(s);
                    if (p != null) val.add(p);
                }
            }
            tip.add(new Tip(os.split(" "), as.getValue(), val, embed));
        }
    }

    private void negateVal(){
        if (!lines.isEmpty() && !isCloseBracket(lines.get(cursor))) {
            lines.get(cursor).negate();
        }
    }
    private void pushValMake(Tip os){
        if (valMake == null) {
            if (os.par.size() > 0) {
                setTip(os.par);
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
    private void pushValMake(String os){
        if (valMake == null) {
            if (lines.size()>0 && lines.get(cursor).tip.embed != null) cursor++;
            lines.add(cursor+(lines.isEmpty()?0:1), new Line(new Tip(os, "", new ArrayList<>(), null), getCursorHelp()));
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
    private Help getCursorHelp(Tip os) {
        return getCursorHelp(os.embed);
    }
    private Help getCursorHelp(Help embed) {
        return embed == null ? getCursorHelp() : embed;
    }
    private Help getCursorHelp() {
        if (!lines.isEmpty()){
            return lines.get(cursor).help;
        }
        return script.help;
    }

    private void drawForeground(MatrixStack matrix, int mouseX, int mouseY, float delta) {
        fill(matrix, width-12, 10, width-10, (ticks>>3)%8+2, -1);
        fill(matrix, 0, 16, 130, height, 0x44000000);
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
        textRenderer.drawWithShadow(matrix, script.name, 136, 4, -1);
        float scroll = sidebarHeight < height ? 0 : sidebarScroll;
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
                    if(y<22) continue;
                    x = textRenderer.drawWithShadow(matrix, word+" ", x, y, -1);
                }
                if(line == 0 && Arrays.stream(os.name).anyMatch(st -> textRenderer.getWidth(st)>115)){
                    y += 12;
                    newHeight += 12;
                }
                y += 12;
                thisHeight += 12;
            }
            if (mouseX >= 0 && mouseX <= 130 && mouseY > startY-4 && mouseY < y && mouseY > 22) {
                if (didClick) {
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
                    pushValMake(os);
                }
                if (renderTips && !os.desc.isEmpty()) {
                    renderTooltip(matrix, new LiteralText(os.desc), mouseX, mouseY);
                }
            }
            thisHeight += 8;

            y += 8;
            newHeight += thisHeight;
            if(y>height){
                textRenderer.draw(matrix, "v", 0, height-7, -1);
                textRenderer.draw(matrix, "v", 124, height-7, -1);
                break;
            }
        }
        sidebarHeight = newHeight;
        if(sidebarScroll>0){
            textRenderer.draw(matrix, "^", 0, 17, -1);
            textRenderer.draw(matrix, "^", 124, 17, -1);
        }

        scroll = sidebar2Height < height ? 0 : sidebar2Scroll;
        scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
        y = 22-scroll;
        newHeight = 8;
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

        bufferTooltips = true;

        RenderSystem.setShaderColor(1, 1, 1, 0.2f);
        searchField.render(matrix, mouseX, mouseY, delta);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        if (drawButton(matrix, width-50, height-20, 50, 20, "Done", null, mouseX, mouseY)) {
            onClose();
        }
        {
            x = width-100;
            if (script.save != null) {
                if (drawButton(matrix, x, height - 20, 50, 20, "Save", null, mouseX, mouseY))
                    script.save.accept(unloadScript());
                x -= 50;
            }
            if (script.apply != null){
                if (drawButton(matrix, x, height - 20, 50, 20, "Apply", null, mouseX, mouseY))
                    script.apply.accept(unloadScript());
                x -= 50;
                }
            if (script.load != null){
                if (drawButton(matrix, x, height - 20, 50, 20, "Load", null, mouseX, mouseY))
                    loadScript(script.load.get());
            }
            x = 130;
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

    //TODO might to be visible in all resolutions
    private void drawHelp(MatrixStack matrices) {
        String hlp = """
        Keybinds:
            F1 - Toggles Help
            F5 - Load Script
            F6 - Apply Script
            F7 - Save Script
            Ctrl-F - Selects Search field
            RMB - Clears Search / Removes elements
            ESC - Closes the UI
        
        Buttons:
            ! - Negates selected condition
            [] - AND on conditions inside
            () - OR on conditions inside
            {} - XOR on conditions inside
        
        Note that some suggestions like BiomeID can only be obtained while in a world
        """;
        int y = 16;
        for (String h : hlp.lines().collect(Collectors.toList())) {
            textRenderer.drawWithShadow(matrices, h, 16, y, -1);
            y+=16;
        }
    }
    private String unloadScript(){
        StringBuilder out = new StringBuilder();
        for (int i = 0; i< lines.size(); i++) {
            Line s = lines.get(i);
            out.append(s);
            if (!isOpenBracket(s) && s.tip.embed == null && i+1<lines.size() && !isCloseBracket(lines.get(i+1)))
                out.append(';');
        }
        return out.toString();
    }
    public static void main(String[] args){
        new ScriptingScreen(new LiteralText("wop"), null, new Script("  ", new ServerPlayerEntityScript<>(), null, null, null, default_embed)).loadScript("true");
    }
    public void loadScript(String in){
        lines.clear();
        cursor = 0;
        boolean negate = false;
        Help prev_help = null;
        for (int i = 0; i<in.length(); i++) {
            char chr = in.charAt(i);
            switch (chr){
                case '!' -> negate = !negate;
                case '~' -> {
                    int colon = in.indexOf(':', i);
                    int tilde = findChr(in, '~', i+1, colon);
                    Pair<String, String> shlp = embed_help.get(in.substring(i+1, tilde == -1 ? colon : (tilde+1))+":");
                    Help hlp = script.embedable.get(shlp.getA());
                    prev_help = getCursorHelp();
                    if(!lines.isEmpty())cursor++;
                    lines.add(cursor,
                            new Line(
                                    new Tip(in.substring(i+1, colon), "", new ArrayList<>(), hlp),
                                    hlp,
                                    tilde == -1 ? null : in.substring(tilde+1, colon),
                                    negate
                            )
                    );
                    i = colon;
                }
                case '[' -> bracketLine('[', ']', prev_help, negate);
                case '{' -> bracketLine('{', '}', prev_help, negate);
                case '(' -> bracketLine('(', ')', prev_help, negate);
                case ']','}',')' -> cursor++;
                default -> {
                    int scolon = findEndChr(in, i, in.length());
                    int colon = findChr(in, ':', i, scolon);
                    if (lines.size()>0 && lines.get(cursor).tip.embed != null) cursor++;
                    if (prev_help != null) bracketLine('[', ']', prev_help, negate);
                    lines.add(cursor+(lines.isEmpty()?0:1), new Line(new Tip(in.substring(i, colon == -1 ? scolon : colon), "", new ArrayList<>(), null), getCursorHelp(), colon == -1 ? null : in.substring(colon, scolon), negate));
                    negate = false;
                    i = scolon;
                    if (lines.size()>1) cursor++;
                }
            }
            if(chr != '!') negate = false;
            if(chr != '!' && chr != '~') prev_help = null;
        }
    }
    private int findChr(String str, int chr, int from, int to){
        for(int i = from; i < to; ++i)
            if (str.charAt(i) == chr) return i;
        return -1;
    }
    private int findEndChr(String str, int from, int to){
        for(int i = from; i < to; ++i) {
            char chr = str.charAt(i);
            if (chr == ';' || isCloseBracket(chr)) return i;
        }
        return to;
    }
    private boolean drawButton(MatrixStack matrix, int x, int y, int w, int h, String text, String desc, int mouseX, int mouseY) {
        boolean hovering = mouseIn(x, y, w, h, mouseX, mouseY);
        if (hovering) {
            if (didClick) {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
            }
            drawOutlineBox(matrix, x, y, w, h, -1);
            if (desc != null && renderTips)
                renderTooltip(matrix, new LiteralText(desc), mouseX, mouseY);
        }
        if (text != null)
            textRenderer.drawWithShadow(matrix, text, x + ((w - textRenderer.getWidth(text)) / 2), y + ((h - 8) / 2), -1);
        return hovering && didClick;
    }
    private boolean drawToggleButton(MatrixStack matrix, int x, int y, int w, int h, String text, String desc, int mouseX, int mouseY, boolean toggled) {
        boolean hovering = mouseIn(x, y, w, h, mouseX, mouseY);
        if (hovering) {
            if (didClick) {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
            }
            if (desc != null && renderTips)
                renderTooltip(matrix, new LiteralText(desc), mouseX, mouseY);
        }
        if (text != null)
            textRenderer.drawWithShadow(matrix, text, x + ((w - textRenderer.getWidth(text)) / 2), y + ((h - 8) / 2), -1);
        if (hovering^toggled) drawOutlineBox(matrix, x, y, w, h, -1);

        return hovering && didClick;
    }
    private boolean mouseIn(int x, int y, int w, int h, float mouseX, float mouseY){
        return mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+h;
    }
    private void drawOutlineBox(MatrixStack matrix, int x, int y, int w, int h, int color){
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
    public void onClose() {
        client.setScreen(parent);
    }

    @Override
    public void tick() {
        if(tick)ticks++;
        super.tick();
        if (sidebarHeight > height) {
            sidebarScroll += (sidebarScrollTarget-sidebarScroll)/2;
            if (sidebarScrollTarget < 0) sidebarScrollTarget /= 2;
            float h = sidebarHeight-height;
            if (sidebarScrollTarget > h) sidebarScrollTarget = h+((sidebarScrollTarget-h)/2);
        }
        if (sidebar2Height > height) {
            sidebar2Scroll += (sidebar2ScrollTarget-sidebar2Scroll)/2;
            if (sidebar2ScrollTarget < 0) sidebar2ScrollTarget /= 2;
            float h = sidebarHeight-height;
            if (sidebar2ScrollTarget > h) sidebar2ScrollTarget = h+((sidebar2ScrollTarget-h)/2);
        }

        if (tooltipBlinkTicks > 0) {
            tooltipBlinkTicks--;
        }
        searchField.tick();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if(renderHelp) return super.mouseScrolled(mouseX, mouseY, amount);
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
            case 257, 335 -> { //Enter
                if (searchField.isActive()) pushValMake(searchField.getText());
            }
            case 89 -> { //F
                if (hasControlDown()) searchField.setTextFieldFocused(true);
            }
            case 294 -> { //F5
                if (script.load != null) loadScript(script.load.get());
            }
            case 295 -> { //F6
                if (script.apply != null) script.apply.accept(unloadScript());
            }
            case 296 -> { //F7
                if (script.save != null) script.save.accept(unloadScript());
            }
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
                    textRenderer.draw(line, innerX, innerY, -1, false, matrices.peek().getModel(), vcp, false, 0, 0xF000F0);
                }
                innerY += 10;
            }

            vcp.draw();
            matrices.pop();
        }
    }
    private void bracketLine(char c1, char c2, Help h2){
        if (isBracket()) return;
        Help help = getCursorHelp();
        if(!lines.isEmpty()) cursor++;
        lines.add(cursor, new Line(new Tip(String.valueOf(c1), "", new ArrayList<>(), null), help, null));
        lines.add(cursor + 1, new Line(new Tip(String.valueOf(c2), "", new ArrayList<>(), null), h2, null));
    }
    private void bracketLine(char c1, char c2){
        if (isBracket()) return;
        Help help = getCursorHelp();
        if(!lines.isEmpty()) cursor++;
        lines.add(cursor, new Line(new Tip(String.valueOf(c1), "", new ArrayList<>(), null), help, null));
        lines.add(cursor + 1, new Line(new Tip(String.valueOf(c2), "", new ArrayList<>(), null), help, null));
    }
    private void bracketLine(char c1, char c2, Help h2, boolean negate){
        if (isBracket()) return;
        Help help = getCursorHelp();
        if(!lines.isEmpty()) cursor++;
        lines.add(cursor, new Line(new Tip(String.valueOf(c1), "", new ArrayList<>(), null), help, null, negate));
        lines.add(cursor + 1, new Line(new Tip(String.valueOf(c2), "", new ArrayList<>(), null), h2 == null? help : h2, null));
    }
    private boolean isBracket(){
        return isBracket(cursor);
    }
    private boolean isBracket(int in){
        return !lines.isEmpty() && isBracket(lines.get(in));
    }
    private boolean isBracket(Line in){
        return isBracket(in.tip.name[0]);
    }
    private boolean isBracket(String in){
        return isBracket(in.charAt(0));
    }
    private boolean isBracket(char in){
        return isOpenBracket(in) || isCloseBracket(in);
    }
    private boolean isOpenBracket(int in){
        return !lines.isEmpty() && isOpenBracket(lines.get(in));
    }
    private boolean isOpenBracket(Line in){
        return isOpenBracket(in.tip.name[0]);
    }
    private boolean isOpenBracket(String in){
        return isOpenBracket(in.charAt(0));
    }
    private boolean isOpenBracket(char in){
        return in == '[' || in == '(' || in == '{';
    }
    private boolean isCloseBracket(int in){
        return !lines.isEmpty() && isCloseBracket(lines.get(in));
    }
    private boolean isCloseBracket(Line in){
        return isCloseBracket(in.tip.name[0]);
    }
    private boolean isCloseBracket(String in){
        return isCloseBracket(in.charAt(0));
    }
    private boolean isCloseBracket(char in){
        return in == ']' || in == ')' || in == '}';
    }
    public static record Script(
            String name,
            Help help,
            Consumer<String> save,
            Consumer<String> apply,
            Supplier<String> load,
            Map<String, Help> embedable
    ) { }

    private static record Tip(
            String[] name,
            String desc,
            List<Help.Parameter> par,
            Help embed
    ) {
        Tip(String name, String desc, List<Help.Parameter> par, Help embed){
            this(new String[]{name}, desc, par, embed);
        }

        @Override
        public String toString() {
            return  (embed == null ? "" : "~")+
                    name[0] +
                    (par.size()>0 ? embed == null ? ":" : "~" : embed == null ? "" : ":");
        }
    }
    private static class Line {
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
        default_embed.put("ENTITY", Default.ENTITY);
        default_embed.put("LIVING_ENTITY", Default.LIVING_ENTITY);
        default_embed.put("PLAYER_ENTITY", Default.PLAYER_ENTITY);
        default_embed.put("SERVER_PLAYER_ENTITY", Default.SERVER_PLAYER_ENTITY);
        default_embed.put("DIMENSION_TYPE", Default.DIMENSION_TYPE);
        default_embed.put("CHUNK", Default.CHUNK);
        default_embed.put("WORLD", Default.WORLD);
        default_embed.put("BIOME", Default.BIOME);
        default_embed.put("ITEM", Default.ITEM);
        default_embed.put("ITEM_STACK", Default.ITEM_STACK);
        default_embed.put("ENCHANTMENT", Default.ENCHANTMENT);
        default_embed.put("ENCHANTMENT_LEVEL_ENTRY", Default.ENCHANTMENT_LEVEL_ENTRY);
        default_embed.put("GAME_MODE", Default.GAME_MODE);
        default_embed.put("FISHING_BOBBER_ENTITY", Default.FISHING_BOBBER_ENTITY);
    }
}
