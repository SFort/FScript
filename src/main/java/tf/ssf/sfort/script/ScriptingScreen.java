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
import tf.ssf.sfort.script.instance.ServerPlayerEntityScript;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//A good chunk of this class was copied from github.com/unascribed/fabrication

//SpaghetGettingOutOfHand TM

public class ScriptingScreen extends Screen {
    private static final Map<String, Help> default_embed = new HashMap<>();
    private final Screen parent;
    private final Script script;

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
    private int ticks;

    private boolean didClick;
    private boolean didRightClick;
    private boolean renderHelp = false;

    private int tooltipBlinkTicks = 0;

    private List<Tip> tip = new ArrayList<>();

    private boolean bufferTooltips = false;
    private final List<Runnable> bufferedTooltips = Lists.newArrayList();

    private TextFieldWidget searchField;

    //TODO remove test
    public ScriptingScreen(Screen parent) {
        this(new LiteralText("FScript"), parent, new Script("§2Test", new ServerPlayerEntityScript<>(), null, System.out::println, () -> "[on_fire;on_file]", default_embed));
    }

    public ScriptingScreen(Text title, Screen parent, Script script) {
        super(title);
        this.parent = parent;
        this.script = script;
        setTip();
    }
    @Override
    public void init(){
        super.init();
        searchField = new TextFieldWidget(textRenderer, 1, 1, 128, 14, searchField, new LiteralText("Search"));
        searchField.setChangedListener((s) -> s = s.trim());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        matrices.push();
        if (client.world == null) this.renderBackground(matrices);
        if (renderHelp) drawHelp(matrices, mouseX, mouseY, delta);
        else drawForeground(matrices, mouseX, mouseY, delta);
        matrices.pop();
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
    private void setTip(Map<String, Object> help){
        clearTip();
        for (Map.Entry<String, Object> as : help.entrySet()) {
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
        if (lines.size() > 0) {
            lines.get(cursor).negate();
        }
    }
    private void pushValMake(Tip os){
        if (valMake == null) {
            if (os.par.size() > 0) {
                setTip(os.par);
                valMake = new Line(os, getCursorHelp(os));
            } else {
                lines.add(cursor+(lines.size()>0?1:0), new Line(os, getCursorHelp(os)));
                setTip();
                valMake = null;
                searchField.setText("");
            }
        }else {
           pushValMake(os.name[0]);
        }
    }
    private void pushValMake(String os){
        if (valMake == null) {
            lines.add(cursor+(lines.size()>0?1:0), new Line(new Tip(os, "", new ArrayList<>(), null), getCursorHelp()));
            searchField.setText("");
        }else {
            valMake.val = os;
            lines.add(cursor+(lines.size()>0?1:0), valMake);
            setTip(valMake.help);
            searchField.setText("");
            valMake = null;
        }
    }
    private Help getCursorHelp(Tip os) {
        return getCursorHelp(os.embed);
    }
    private Help getCursorHelp(Help embed) {
        return embed == null ? getCursorHelp() : embed;
    }
    private Help getCursorHelp() {
        if (lines.size() > 0){
            return lines.get(cursor).help;
        }
        return script.help;
    }
    private void drawForeground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        textRenderer.drawWithShadow(matrices, script.name, 136, 4, -1);
        fill(matrices, 0, 16, 130, height, 0x44000000);
        float scroll = sidebarHeight < height ? 0 : sidebarScroll;
        scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
        float y = 22-scroll;
        int newHeight = 8;
        for (Tip os : tip) {
            {
                final String match = searchField.getText().toLowerCase();
                if (!os.desc.toString().toLowerCase().contains(match) && Arrays.stream(os.name).noneMatch(s -> s.toLowerCase().contains(match)))
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
                    x = textRenderer.draw(matrices, word+" ", x, y, -1);
                }
                if(line == 0 && Arrays.stream(os.name).anyMatch(st -> textRenderer.getWidth(st)>115)){
                    y += 12;
                    newHeight += 12;
                }
                y += 12;
                thisHeight += 12;
            }
            if (didClick) {
                if (mouseX >= 0 && mouseX <= 130 && mouseY > startY-4 && mouseY < y && mouseY > 22) {
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
                    pushValMake(os);
                }
            }
            thisHeight += 8;

            y += 8;
            newHeight += thisHeight;
            if(y>height){
                textRenderer.draw(matrices, "v", 0, height-7, -1);
                textRenderer.draw(matrices, "v", 124, height-7, -1);
                break;
            }
        }
        sidebarHeight = newHeight;
        if(sidebarScroll>0){
            textRenderer.draw(matrices, "^", 0, 17, -1);
            textRenderer.draw(matrices, "^", 124, 17, -1);
        }

        scroll = sidebar2Height < height ? 0 : sidebar2Scroll;
        scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
        y = 22-scroll;
        newHeight = 8;
        int x = 140;
        if(valMake != null && lines.size() == 0){
            textRenderer.draw(matrices, valMake + " §7"+last_par, x, y, -2000);
            if(didClick && mouseX < width && mouseX > 132 && mouseY < y+12 && mouseY > 20){
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
                valMake = null;
                setTip();
            }
        }
        for (int i = 0; i< lines.size(); i++) {
            String s = lines.get(i).toString();
            int thisHeight = 0;
            float startY = y;
            thisHeight += 12;
            if (isCloseBracket(s.charAt(s.length() - 1))) x -= 8;
            int lx = 0;
            if(cursor == i){
                if(valMake != null) lx = textRenderer.draw(matrices, valMake + " §7"+last_par, x-8, y, -2000) - x +16;
                textRenderer.draw(matrices, ">", x+lx-8, y, -1);
            }
            if(y>20 && y<height-30) textRenderer.draw(matrices, s, x+lx, y, -1);
            if (isOpenBracket(s)) x += 8;
            y += 12;
            thisHeight += 12;
            if (didClick) {
                if (mouseX < width && mouseX > 132 && mouseY > startY-4 && mouseY < y && mouseY > 20) {
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
                    if (didRightClick){
                        if(!isBracket(i)) {
                            lines.remove(i);
                            if (i <= cursor && cursor != 0) cursor--;
                            if (i < lines.size() && i > 0 && isBracket(i) && isBracket(i-1)){
                                lines.remove(i);
                                lines.remove(i-1);
                                if (i <= cursor) cursor--;
                                if (i-1 <= cursor && cursor != 0) cursor--;
                            }
                        }else if (isOpenBracket(i)){
                            if (i < lines.size()-1 && isCloseBracket(i+1)){
                                lines.remove(i);
                                lines.remove(i);
                                if (i <= cursor && cursor != 0) cursor--;
                                if (i-1 <= cursor && cursor != 0) cursor--;
                            }
                        }else{
                            if (i > 0 && isOpenBracket(i-1)){
                                lines.remove(i);
                                lines.remove(i-1);
                                if (i <= cursor) cursor--;
                                if (i-1 <= cursor && cursor != 0) cursor--;
                            }
                        }
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
                textRenderer.draw(matrices, "v", 130, height-27, -1);
                textRenderer.draw(matrices, "v", width-8, height-27, -1);
                break;
            }
        }
        sidebar2Height = newHeight;
        if(sidebar2Scroll>0){
            textRenderer.draw(matrices, "^", 130, 17, -1);
            textRenderer.draw(matrices, "^", width-8, 17, -1);
        }

        bufferTooltips = true;

        RenderSystem.setShaderColor(1, 1, 1, 0.2f);
        searchField.render(matrices, mouseX, mouseY, delta);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        if (drawButton(matrices, width-50, height-20, 50, 20, "Done", mouseX, mouseY)) {
            onClose();
        }
        {
            x = width-100;
            if (script.save != null) {
                if (drawButton(matrices, x, height - 20, 50, 20, "Save", mouseX, mouseY))
                    script.save.accept(unloadScript());
                x -= 50;
            }
            if (script.apply != null){
                if (drawButton(matrices, x, height - 20, 50, 20, "Apply", mouseX, mouseY))
                    script.apply.accept(unloadScript());
                x -= 50;
                }
            if (script.load != null){
                if (drawButton(matrices, x, height - 20, 50, 20, "Load", mouseX, mouseY))
                    loadScript(script.load.get());
                x -= 50;
            }
            x = 130;
            if (drawButton(matrices, x, height - 20, 20, 20, "!", mouseX, mouseY))
                negateVal();
            x += 20;
            if (drawButton(matrices, x, height - 20, 20, 20, "[]", mouseX, mouseY))
                bracketLine('[', ']');
            x += 20;
            if (drawButton(matrices, x, height - 20, 20, 20, "()", mouseX, mouseY))
                bracketLine('(', ')');
            x += 20;
            if (drawButton(matrices, x, height - 20, 20, 20, "{}", mouseX, mouseY))
                bracketLine('{', '}');
        }
        if (didClick) didClick = false;
        if (didRightClick) didRightClick = false;

        super.render(matrices, mouseX, mouseY, delta);

        bufferTooltips = false;
        for (Runnable r : bufferedTooltips) {
            r.run();
        }
        bufferedTooltips.clear();

        matrices.pop();
    }

    private void drawHelp(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        String hlp = """
        F1 - Toggles Help
        F5 - Load Script
        F6 - Apply Script
        F7 - Save Script
        Ctrl-F - Selects Search field
        RMB - Clears Search / Removes elements
        ESC - Closes the UI
        """;
        int y = 16;
        for (String h : hlp.lines().collect(Collectors.toList())) {
            textRenderer.drawWithShadow(matrices, h, 16, y, -1);
            y+=16;
        }
    }
    private String unloadScript(){
        //TODO
        StringBuilder out = new StringBuilder();
        for (int i = 0; i< lines.size(); i++) {
            Line s = lines.get(i);
            Tip os = s.tip;
            String key = os.name[0];
            if (s.negate) out.append('!');
            out.append(key);
            if (isBracket(key)) continue;
            boolean e = os.embed != null;
            boolean p = os.par.size() > 0;
            if (e || p) {
                if (e) out.insert(out.length()-1,'~');
                if (e && p) out.append('~');
                else out.append(':');
                out.append(s.val);
            }
            //TODO
            if (lines.size() != i+1)
                out.append(';');
        }
        return out.toString();
    }
    private void loadScript(String in){
        //TODO maybe accurately escape embed keys?
        lines.clear();
        cursor =0;
        for (int i = 0; i<in.length(); i++) {
            switch (in.charAt(i)){
                case '[','{','(' -> {
                    if(i+1 == in.length()) break;
                    scriptLoadingBracket(in.substring(0, i+1));
                    in = in.substring(i+1);
                    i = -1;
                }
                case ';' -> {
                    scriptLoading(in.substring(0, i));
                    in = in.substring(i+1);
                    i = -1;
                }
                case ']','}',')' -> {
                    scriptLoading(in.substring(0, i));
                    if(i+1 == in.length()){
                        in = in.substring(i);
                        break;
                    }
                    scriptLoadingBracket(String.valueOf(in.charAt(i)));
                    in = in.substring(i+1);
                    i = -1;
                }
            }
        }
        scriptLoading(in);
    }
    private void scriptLoadingBracket(String str){
        boolean negate = false;
        if (str.charAt(0) == '!'){
            negate = true;
            str = str.substring(1);
        }
        //TODO insert help context
        //lines.add(new Line(getTip(str), negate));
    }
    private void scriptLoading(String str){
        boolean negate = false;
        if (str.charAt(0) == '!'){
            negate = true;
            str = str.substring(1);
        }
        int i = str.indexOf(':');
        //TODO script help context
        if (i != -1){
            //lines.add(new Line(getTip(str.substring(0, i)), str.substring(i+1), negate));
        }else{
            //lines.add(new Line(getTip(str), negate));
        }
    }
    //TODO
    private Tip getTip(String str){
        Map<String, Object> h = Help.recurseImportedSeperate(script.help, new HashSet<>());
        return null;
    }

    private boolean drawButton(MatrixStack matrices, int x, int y, int w, int h, String text, float mouseX, float mouseY) {
        int textWidth = textRenderer.getWidth(text);
        textRenderer.draw(matrices, text, x+((w-textWidth)/2), y+((h-8)/2), -1);
        if (mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+h) {
            fill(matrices, x, y, x+w, y+1, -1);
            fill(matrices, x, y, x+1, y+h, -1);
            fill(matrices, x, y+h-1, x+w, y+h, -1);
            fill(matrices, x+w-1, y, x+w, y+h, -1);
            if (didClick) {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
                return true;
            }
        }
        return false;
    }

    public static String formatTitleCase(String in) {
        String[] pieces = in.toLowerCase().split("[_ ;:]");
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
        ticks++;
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
        if (mouseX<=128 && mouseY<=14) searchField.setText("");
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
            case 257 -> { //Enter
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

    private void bracketLine(char c1, char c2){
        if (lines.size()<1 || isBracket()) return;
        Help help = getCursorHelp();
        lines.add(cursor + 1, new Line(new Tip(String.valueOf(c2), "", new ArrayList<>(), null), help, null));
        lines.add(cursor + (lines.get(cursor).tip.embed == null ? 0 : 1), new Line(new Tip(String.valueOf(c1), "", new ArrayList<>(), null), help, null));
    }
    private boolean isBracket(){
        return isBracket(cursor);
    }
    private boolean isBracket(int in){
        return isBracket(lines.get(in));
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
        return isOpenBracket(lines.get(in));
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
        return isCloseBracket(lines.get(in));
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
            Object desc,
            List<Help.Parameter> par,
            Help embed
    ) {
        Tip(String name, Object desc, List<Help.Parameter> par, Help embed){
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
                    (val !=null? val : "")
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
