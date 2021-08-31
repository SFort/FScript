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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tf.ssf.sfort.script.instance.ServerPlayerEntityScript;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

//A good chunk of this class was copied from github.com/unascribed/fabrication

public class ScriptingScreen extends Screen {
    private static List<Script> scripts = new ArrayList<>();
    private static Map<String, Help> default_embed = new HashMap<>();
    //TODO remove test
    static {
        addScript("§2Test", Help.recurseImported(new ServerPlayerEntityScript<>(), new HashSet<>()), null, null, () -> "[on_fire;on_file]", default_embed);
    }
    private final Screen parent;

    private Script selected_script;
    private List<String> script = new ArrayList<>();
    private int script_cursor = 0;
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

    private int noteIndex = 0;

    private TextFieldWidget searchField;

    public ScriptingScreen(Screen parent) {
        super(new LiteralText("FScript"));
        this.parent = parent;
    }
    private void setTip(List<String> in, Map<String, Help> embedable){
        tip = new ArrayList<>();
        for (String os : in) {
            int colon = os.indexOf(':');
            List<Help.Parameter> val = new ArrayList<>();
            Help embed = null;
            if (colon != -1) {
                String arg = os.substring(colon+1);
                os = os.substring(0, colon);
                if (os.startsWith("~")){
                    if (embedable != null) embed = embedable.getOrDefault(arg, null);
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
            tip.add(new Tip(os.split(" "), 0, val, embed));
        }
    }
    @Override
    protected void init() {
        super.init();
        searchField = new TextFieldWidget(textRenderer, 1, 1, 128, 14, searchField, new LiteralText("Search"));
        searchField.setChangedListener((s) -> s = s.trim());
        if(!scripts.isEmpty()){
            selected_script = scripts.get(0);
            setTip(selected_script.help.keySet().stream().toList(), selected_script.embedable);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        matrices.push();
        if (client.world == null) this.renderBackground(matrices);
        if (renderHelp) drawHelp(matrices, mouseX, mouseY, delta);
        else drawForeground(matrices, mouseX, mouseY, delta);
        matrices.pop();
    }

    private void drawForeground(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        textRenderer.drawWithShadow(matrices, selected_script.name, 136, 4, -1);
        fill(matrices, 0, 16, 130, height, 0x44000000);
        float scroll = sidebarHeight < height ? 0 : sidebarScroll;
        scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
        float y = 22-scroll;
        int newHeight = 8;
        for (Tip os : tip) {
            if(Arrays.stream(os.name).noneMatch(s -> s.toLowerCase().contains(searchField.getText().toLowerCase()))) continue;
            String s = os.name[os.name.length == 1? 0 : (ticks>>5) %os.name.length];
            s = formatTitleCase(s);
            if (os.embed != null && os.par.size() == 0) s = "~"+s;
            else if (os.embed != null) s = "~"+s+"~";
            int thisHeight = 0;
            float startY = y;
            thisHeight += 12;
            {
                int x = 8;
                int line = 0;
                for (String word : Splitter.on(CharMatcher.whitespace()).split(s)) {
                    if (textRenderer.getWidth(word)+x > 100 && line == 0) {
                        x = 8;
                        y += 12;
                        newHeight += 12;
                        line = 1;
                    }
                    if(y<22) continue;
                    x = textRenderer.draw(matrices, word+" ", x, y, -1);
                }
                if(line == 0 && Arrays.stream(os.name).anyMatch(st -> textRenderer.getWidth(st)>90)){
                    y += 12;
                    newHeight += 12;
                }
                y += 12;
                thisHeight += 12;
            }
            if (didClick) {
                if (mouseX >= 0 && mouseX <= 130 && mouseY > startY-4 && mouseY < y && mouseY > 22) {
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
                    //TODO
                    //PICK first option write it in script if has no parameters
                    //if it has parameters add : to to search bar indicating to switch it's mode to parameter mode
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
        for (int i = 0; i<script.size(); i++) {
            String s = script.get(i);
            int thisHeight = 0;
            float startY = y;
            thisHeight += 12;
            char chr = s.charAt(s.length() - 1);
            if (chr == ']' || chr == ')' || chr == '}') x -= 8;
            if(y>20 && y<height-30) {
                 textRenderer.draw(matrices, s, x, y, -1);
            }
            if(script_cursor == i) textRenderer.draw(matrices, ">", 132, y, -1);
            chr = s.charAt(0);
            if (chr == '[' || chr == '(' || chr == '{') x += 8;
            y += 12;
            thisHeight += 12;
            if (didClick) {
                if (mouseX < width && mouseX > 132 && mouseY > startY-4 && mouseY < y && mouseY > 20) {
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, 1.2f, 1f));
                    //TODO verify removal
                    if (didRightClick){
                        script.remove(i);
                        if (i <= script_cursor && script_cursor != 0) script_cursor--;
                    } else{
                        script_cursor = i;
                        if (s.contains(":") || s.contains("~"))
                            searchField.setText(s);
                    }
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
        if (selected_script != null) {
            x = 100;
            if (selected_script.save != null) {
                if (drawButton(matrices, width - x, height - 20, 50, 20, "Save", mouseX, mouseY))
                    selected_script.save.accept(String.join("", script));
                x += 50;
            }
            if (selected_script.apply != null){
                if (drawButton(matrices, width - x, height - 20, 50, 20, "Apply", mouseX, mouseY))
                    selected_script.apply.accept(String.join("", script));
                x += 50;
                }
            if (selected_script.load != null){
                if (drawButton(matrices, width - x, height - 20, 50, 20, "Load", mouseX, mouseY))
                    loadScript(selected_script.load.get());
                x += 50;
            }
        }
        //TODO save as / open
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
        Crl-F - Selects Search field
        RMB - Clears Search / Removes elements
        """;
        int y = 16;
        for (String h : hlp.lines().collect(Collectors.toSet())) {
            textRenderer.drawWithShadow(matrices, h, 16, y, -1);
            y+=16;
        }
    }

    private void loadScript(String in){
        //TODO maybe accurately escape embed keys?
        script.clear();
        script_cursor=0;
        for (int i = 0; i<in.length(); i++) {
            switch (in.charAt(i)){
                case '[','{','(',';' -> {
                    if(i+1 == in.length()) break;
                    script.add(in.substring(0, i+1));
                    in = in.substring(i+1);
                    i = -1;
                }
                case ']','}',')' -> {
                    script.add(in.substring(0, i));
                    if(i+1 == in.length()){
                        in = in.substring(i);
                        break;
                    }
                    script.add(String.valueOf(in.charAt(i)));
                    in = in.substring(i+1);
                    i = -1;
                }
            }
        }
        script.add(in);
    }

    private boolean drawButton(MatrixStack matrices, int x, int y, int w, int h, String text, float mouseX, float mouseY) {
        boolean click = false;
        boolean hover = mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+h;
        fill(matrices, x, y, x+w, y+h, 0x44FFFFFF);
        if (hover) {
            fill(matrices, x, y, x+w, y+1, -1);
            fill(matrices, x, y, x+1, y+h, -1);
            fill(matrices, x, y+h-1, x+w, y+h, -1);
            fill(matrices, x+w-1, y, x+w, y+h, -1);
            if (didClick) {
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
                click = true;
            }
        }
        int textWidth = textRenderer.getWidth(text);
        textRenderer.draw(matrices, text, x+((w-textWidth)/2), y+((h-8)/2), -1);
        return click;
    }

    public static String formatTitleCase(String in) {
        String[] pieces = new String[] { in };
        if (in.contains("_")) {
            pieces = in.toLowerCase().split("_");
        }

        StringBuilder result = new StringBuilder();
        for (String s : pieces) {
            if (s == null)
                continue;
            String t = s.trim().toLowerCase();
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
        //TODO if search selected & has parameter write to index
        if(keyCode == 290/*F1*/){
            renderHelp = !renderHelp;
        }
        if (renderHelp) return super.keyPressed(keyCode, scanCode, modifiers);
        if (hasControlDown() && keyCode == 89/*F*/){
            searchField.setTextFieldFocused(true);
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

    public final static record Script(
            String name,
            Map<String, Object> help,
            Consumer<String> save,
            Consumer<String> apply,
            Supplier<String> load,
            Map<String, Help> embedable
    ) { }

    private final static record Tip(
            String[] name,
            int max_lines,
            List<Help.Parameter> par,
            Help embed
    ) { }
    public static void addScript(Script script){
        scripts.add(script);
    }
    public static void addScript(@NotNull String name, @NotNull Map<String, Object> help, @Nullable Consumer<String> save, @Nullable Consumer<String> apply, @Nullable Supplier<String> load, @Nullable Map<String, Help> embedable){
        addScript(new Script(name, help, save, apply, load, embedable));
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
