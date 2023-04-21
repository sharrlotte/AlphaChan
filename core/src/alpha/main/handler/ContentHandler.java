package alpha.main.handler;

import arc.*;
import arc.files.*;
import arc.graphics.Color;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.io.*;
import arc.util.serialization.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.game.Schematic.*;
import mindustry.io.*;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquid;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

import javax.imageio.*;

import alpha.main.util.Log;

import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.zip.*;

import static mindustry.Vars.*;

public class ContentHandler {

    public static final String schemHeader = schematicBaseStart;

    private static Color co = new Color();
    private static Graphics2D currentGraphics;
    private static BufferedImage currentImage;
    private static ObjectMap<String, Fi> imageFiles = new ObjectMap<>();
    private static ObjectMap<String, BufferedImage> regions = new ObjectMap<>();

    private static ContentHandler instance = new ContentHandler();
    private String assets = "Mindustry/core/";

    public synchronized static ContentHandler getInstance() {
        return instance;
    }

    private ContentHandler() {

        try {

            File file = new File(assets);
            if (!file.exists())
                assets = "build/libs/Mindustry/core/";

            new Fi("cache").deleteDirectory();
            new File("cache/").mkdir();
            new File("cache/temp/").mkdir();

            Version.enabled = false;
            Vars.content = new ContentLoader();
            Vars.content.createBaseContent();
            for (ContentType type : ContentType.all) {
                for (Content content : Vars.content.getBy(type)) {
                    try {
                        content.init();
                    } catch (Throwable ignored) {
                    }
                }
            }

            Vars.state = new GameState();

            TextureAtlasData data = new TextureAtlasData(new Fi(assets + "assets/sprites/sprites.aatls"),
                    new Fi(assets + "assets/sprites"),
                    false);
            Core.atlas = new TextureAtlas();

            new Fi(assets + "assets-raw/sprites_out").walk(f -> {
                if (f.extEquals("png")) {
                    imageFiles.put(f.nameWithoutExtension(), f);
                }
            });

            data.getPages().each(page -> {
                page.texture = Texture.createEmpty(null);
                page.texture.width = page.width;
                page.texture.height = page.height;
            });

            data.getRegions().each(
                    reg -> Core.atlas.addRegion(reg.name,
                            new AtlasRegion(reg.page.texture, reg.left, reg.top, reg.width, reg.height) {
                                {
                                    name = reg.name;
                                    texture = reg.page.texture;
                                }
                            }));

            Lines.useLegacyLine = true;
            Core.atlas.setErrorRegion("error");
            Draw.scl = 1f / 4f;
            Core.batch = new SpriteBatch(0) {
                @Override
                protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width,
                        float height,
                        float rotation) {
                    x += 4;
                    y += 4;

                    x *= 4;
                    y *= 4;
                    width *= 4;
                    height *= 4;

                    y = currentImage.getHeight() - (y + height / 2f) - height / 2f;

                    AffineTransform at = new AffineTransform();
                    at.translate(x, y);
                    at.rotate(-rotation * Mathf.degRad, originX * 4, originY * 4);

                    currentGraphics.setTransform(at);
                    BufferedImage image = getImage(((AtlasRegion) region).name);
                    if (!color.equals(Color.white)) {
                        image = tint(image, color);
                    }

                    currentGraphics.drawImage(image, 0, 0, (int) width, (int) height, null);
                }

                @Override
                protected void draw(Texture texture, float[] spriteVertices, int offset, int count) {
                    // do nothing
                }
            };

            for (ContentType type : ContentType.values()) {
                for (Content content : Vars.content.getBy(type)) {
                    try {
                        content.load();
                        content.loadIcon();
                    } catch (Throwable ignored) {
                    }
                }
            }

            try {
                BufferedImage image = ImageIO.read(new File(assets + "assets/sprites/block_colors.png"));

                for (Block block : Vars.content.blocks()) {
                    block.mapColor.argb8888(image.getRGB(block.id, 0));
                    if (block instanceof OreBlock) {
                        block.mapColor.set(block.itemDrop.color);
                    }
                }
            } catch (Exception e) {
                Log.error(e);
            }

            world = new World() {
                public Tile tile(int x, int y) {
                    return new Tile(x, y);
                }
            };
        } catch (Exception exception) {
            Log.error(exception);
        }
    }

    private static BufferedImage getImage(String name) {
        return regions.get(name, () -> {
            try {
                return ImageIO.read(imageFiles.get(name, imageFiles.get("error")).file());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private BufferedImage tint(BufferedImage image, Color color) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Color tmp = new Color();
        for (int x = 0; x < copy.getWidth(); x++) {
            for (int y = 0; y < copy.getHeight(); y++) {
                int argb = image.getRGB(x, y);
                tmp.argb8888(argb);
                tmp.mul(color);
                copy.setRGB(x, y, tmp.argb8888());
            }
        }
        return copy;
    }

    public static Schematic parseSchematic(String text) throws IOException {
        return read(new ByteArrayInputStream(Base64Coder.decode(text)));
    }

    public static Schematic parseSchematicURL(String text) throws Exception {
        return read(NetworkHandler.download(text));
    }

    public static Schematic read(InputStream input) throws IOException {
        byte[] header = { 'm', 's', 'c', 'h' };
        for (byte b : header) {
            if (input.read() != b) {
                throw new IOException("Not a schematic file (missing header).");
            }
        }

        // discard version
        input.read();

        try (DataInputStream stream = new DataInputStream(new InflaterInputStream(input))) {
            short width = stream.readShort(), height = stream.readShort();

            StringMap tags = new StringMap();
            byte tagLength = stream.readByte();
            for (int i = 0; i < tagLength; i++) {
                tags.put(stream.readUTF(), stream.readUTF());
            }

            IntMap<Block> blocks = new IntMap<>();
            byte length = stream.readByte();
            for (int i = 0; i < length; i++) {
                String name = stream.readUTF();
                Block block = Vars.content.getByName(ContentType.block, SaveFileReader.fallback.get(name, name));
                blocks.put(i, block == null || block instanceof LegacyBlock ? Blocks.air : block);
            }

            int total = stream.readInt();
            Seq<Stile> tiles = new Seq<>(total);
            for (int i = 0; i < total; i++) {
                Block block = blocks.get(stream.readByte());
                int position = stream.readInt();
                Object config = TypeIO.readObject(Reads.get(stream));
                byte rotation = stream.readByte();
                if (block != Blocks.air) {
                    tiles.add(new Stile(block, Point2.x(position), Point2.y(position), config, rotation));
                }
            }

            return new Schematic(tiles, tags, width, height);
        }
    }

    public static BufferedImage previewSchematic(Schematic schem) throws Exception {

        BufferedImage image = new BufferedImage(schem.width * 32, schem.height * 32, BufferedImage.TYPE_INT_ARGB);

        Draw.reset();
        Seq<BuildPlan> requests = schem.tiles.map(t -> new BuildPlan(t.x, t.y, t.rotation, t.block, t.config));
        currentGraphics = image.createGraphics();
        currentImage = image;
        requests.each(req -> {
            req.animScale = 1f;
            req.worldContext = false;
            req.block.drawPlanRegion(req, requests);
            Draw.reset();
        });

        requests.each(req -> req.block.drawPlanConfigTop(req, requests));

        return image;
    }

    public static void add(HashMap<String, Float> map, String key, float amount) {
        if (map.containsKey(key)) {
            map.put(key, map.get(key) + amount);
        } else {
            map.put(key, amount);
        }
    }

    public static HashMap<String, Float> getSchematicInput(Schematic schem) {

        HashMap<String, Float> input = new HashMap<>();

        schem.tiles.forEach((tile) -> {

            Block block = Vars.content.block(tile.block.name);
            block.init();

            if (block instanceof GenericCrafter) {

                GenericCrafter crafter = (GenericCrafter) block;

                for (Consume cons : crafter.consumers) {
                    if (cons instanceof ConsumeItems) {
                        ConsumeItems c = (ConsumeItems) cons;

                        for (ItemStack stack : c.items) {
                            add(input, stack.item.name, stack.amount / crafter.craftTime * 60);
                        }

                    } else if (cons instanceof ConsumeLiquid) {
                        ConsumeLiquid c = (ConsumeLiquid) cons;
                        add(input, c.liquid.name, c.amount * 60);
                    }
                }
            } else if (block instanceof Reconstructor) {
                Reconstructor constructor = (Reconstructor) block;

                for (Consume cons : constructor.consumers) {
                    if (cons instanceof ConsumeItems) {
                        ConsumeItems c = (ConsumeItems) cons;

                        for (ItemStack stack : c.items) {
                            add(input, stack.item.name, stack.amount / constructor.constructTime * 60);
                        }

                    } else if (cons instanceof ConsumeLiquid) {
                        ConsumeLiquid c = (ConsumeLiquid) cons;
                        add(input, c.liquid.name, c.amount * 60);
                    }
                }
            }
        });
        return input;
    }

    public static HashMap<String, Float> getSchematicOutput(Schematic schem) {

        HashMap<String, Float> output = new HashMap<>();

        schem.tiles.forEach((tile) -> {

            Block block = Vars.content.block(tile.block.name);
            block.init();

            if (block instanceof GenericCrafter) {

                GenericCrafter crafter = (GenericCrafter) block;

                if (crafter.outputItem != null) {
                    add(output, crafter.outputItem.item.name, crafter.outputItem.amount / crafter.craftTime * 60);

                } else {
                    if (crafter.outputItems != null) {
                        for (ItemStack stack : crafter.outputItems) {
                            add(output, stack.item.name, stack.amount / crafter.craftTime * 60);
                        }
                    }
                }

                if (crafter.outputLiquid != null) {
                    add(output, crafter.outputLiquid.liquid.name, crafter.outputLiquid.amount * 60);

                } else {
                    if (crafter.outputLiquids != null) {
                        for (LiquidStack stack : crafter.outputLiquids) {
                            add(output, stack.liquid.name, stack.amount * 60);
                        }
                    }
                }
            }
        });
        return output;
    }

    public static boolean isSchematicText(Message message) {
        return message.getContentRaw().startsWith(ContentHandler.schemHeader) && message.getAttachments().isEmpty();
    }

    public static boolean isSchematicFile(Attachment attachment) {
        String fileExtension = attachment.getFileExtension();
        if (fileExtension == null)
            return true;

        return fileExtension.equals(Vars.schematicExtension);
    }

    public static boolean isSchematicFile(List<Attachment> attachments) {
        for (Attachment a : attachments) {
            if (isSchematicFile(a))
                return true;
        }
        return false;
    }

    public static boolean isMapFile(Attachment attachment) {
        return attachment.getFileName().endsWith(".msav") || attachment.getFileExtension() == null;
    }

    public static boolean isMapFile(Message message) {
        for (Attachment a : message.getAttachments()) {
            if (isMapFile(a))
                return true;
        }
        return false;
    }

    public static boolean isMapFile(List<Attachment> attachments) {
        for (Attachment a : attachments) {
            if (isMapFile(a))
                return true;
        }
        return false;
    }

    public static EmbedBuilder getSchematicInfoEmbedBuilder(Schematic schem, Member member) {
        EmbedBuilder builder = new EmbedBuilder();

        if (!schem.description().isEmpty())
            builder.setFooter(schem.description());

        // Schem heigh, width
        StringBuilder sizeString = new StringBuilder();
        sizeString.append("<schematic.width>[Width]: " + String.valueOf(schem.width) + "\n");
        sizeString.append("<schematic.height>[Height]: " + String.valueOf(schem.height) + "\n");
        builder.addField("<schematic.size>[Size]", sizeString.toString(), true);

        StringBuilder requirement = new StringBuilder();
        // Power input/output

        int powerProduction = (int) Math.round(schem.powerProduction()) * 60;
        int powerConsumption = (int) Math.round(schem.powerConsumption()) * 60;

        if (powerConsumption != 0)
            requirement.append("\n<schematic.power_out>[Power need]: " + String.valueOf(powerConsumption) + "/s");

        if (powerProduction != 0)
            requirement.append("\n<schematic.power_out>[Power generate]: " + String.valueOf(powerProduction) + "/s");

        if (powerProduction != 0 && powerConsumption != 0) {
            float powerLeft = powerProduction - powerConsumption;
            requirement.append("\n"
                    + (powerLeft >= 0 ? "<schematic.power_out>[Power need]" : "<schematic.power_out>[Power generate]"));
            requirement.append(": " + String.valueOf(Math.abs(powerLeft)) + "/s");
        }

        if (requirement.length() != 0)
            builder.addField("<schematic.power>[Power]", requirement.toString(), true);

        // Item requirements
        requirement = new StringBuilder();
        for (ItemStack stack : schem.requirements()) {
            List<RichCustomEmoji> emotes = member.getGuild().getEmojisByName(stack.item.name.replace("-", ""), true);
            requirement.append((emotes.isEmpty() ? stack.item.name : emotes.get(0).getAsMention()) + ": "
                    + stack.amount + "\n");
        }

        builder.addField("<schematic.build_requirement>[Build requirement]", requirement.toString(), true);

        return builder;
    }

    public static File getSchematicFile(Schematic schem) throws IOException {
        String sname = schem.name().replace("/", "_").replace(" ", "_").replace(":", "_");
        new File("cache").mkdir();
        if (sname.isEmpty())
            sname = "empty";
        File schemFile = new File("cache/temp/" + sname + "." + Vars.schematicExtension);
        Schematics.write(schem, new Fi(schemFile));
        return schemFile;
    }

    public static File getSchematicPreviewFile(Schematic schem) throws Exception {

        BufferedImage preview = ContentHandler.previewSchematic(schem);
        new File("cache").mkdir();
        File previewFile = new File("cache/temp/img_" + UUID.randomUUID() + ".png");
        ImageIO.write(preview, "png", previewFile);

        return previewFile;

    }

    public static Map readMap(InputStream is) throws IOException {
        try (InputStream ifs = new InflaterInputStream(is);
                CounterInputStream counter = new CounterInputStream(ifs);
                DataInputStream stream = new DataInputStream(counter)) {
            Map out = new Map();

            SaveIO.readHeader(stream);
            int version = stream.readInt();
            SaveVersion ver = SaveIO.getSaveWriter(version);
            StringMap[] metaOut = { null };
            ver.region("meta", stream, counter, in -> metaOut[0] = ver.readStringMap(in));

            StringMap meta = metaOut[0];

            out.name = meta.get("name", "Unknown");
            out.author = meta.get("author");
            out.description = meta.get("description");
            out.tags = meta;

            int width = meta.getInt("width"), height = meta.getInt("height");

            var floors = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            var walls = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            var fgraphics = floors.createGraphics();
            var jcolor = new java.awt.Color(0, 0, 0, 64);
            int black = 255;
            CachedTile tile = new CachedTile() {
                @Override
                public void setBlock(Block type) {
                    super.setBlock(type);

                    int c = MapIO.colorFor(block(), Blocks.air, Blocks.air, team());
                    if (c != black && c != 0) {
                        walls.setRGB(x, floors.getHeight() - 1 - y, conv(c));
                        fgraphics.setColor(jcolor);
                        fgraphics.drawRect(x, floors.getHeight() - 1 - y + 1, 1, 1);
                    }
                }
            };

            ver.region("content", stream, counter, ver::readContentHeader);
            ver.region("preview_map", stream, counter, in -> ver.readMap(in, new WorldContext() {
                @Override
                public void resize(int width, int height) {
                }

                @Override
                public boolean isGenerating() {
                    return false;
                }

                @Override
                public void begin() {
                    world.setGenerating(true);
                }

                @Override
                public void end() {
                    world.setGenerating(false);
                }

                @Override
                public void onReadBuilding() {
                    // read team colors
                    if (tile.build != null) {
                        int c = tile.build.team.color.argb8888();
                        int size = tile.block().size;
                        int offsetx = -(size - 1) / 2;
                        int offsety = -(size - 1) / 2;
                        for (int dx = 0; dx < size; dx++) {
                            for (int dy = 0; dy < size; dy++) {
                                int drawx = tile.x + dx + offsetx, drawy = tile.y + dy + offsety;
                                walls.setRGB(drawx, floors.getHeight() - 1 - drawy, c);
                            }
                        }
                    }
                }

                @Override
                public Tile tile(int index) {
                    tile.x = (short) (index % width);
                    tile.y = (short) (index / width);
                    return tile;
                }

                @Override
                public Tile create(int x, int y, int floorID, int overlayID, int wallID) {
                    if (overlayID != 0) {
                        floors.setRGB(x, floors.getHeight() - 1 - y,
                                conv(MapIO.colorFor(Blocks.air, Blocks.air, content.block(overlayID), Team.derelict)));
                    } else {
                        floors.setRGB(x, floors.getHeight() - 1 - y,
                                conv(MapIO.colorFor(Blocks.air, content.block(floorID), Blocks.air, Team.derelict)));
                    }
                    return tile;
                }
            }));

            fgraphics.drawImage(walls, 0, 0, null);
            fgraphics.dispose();

            out.image = floors;

            return out;

        } finally {
            content.setTemporaryMapper(null);
        }
    }

    static int conv(int rgba) {
        return co.set(rgba).argb8888();
    }

    public static File getMapFile(Attachment attachment) throws FileNotFoundException, IOException {
        new File("cache/").mkdir();
        new File("cache/temp/").mkdir();
        File mapFile = new File("cache/temp/" + attachment.getFileName());
        Streams.copy(NetworkHandler.download(attachment.getUrl()), new FileOutputStream(mapFile));

        return mapFile;
    }

    public static File getMapImageFile(Map map) throws IOException {
        new File("cache/").mkdir();
        new File("cache/temp/").mkdir();
        Fi imageFile = Fi.get("cache/temp/image_" + UUID.randomUUID() + ".png");
        ImageIO.write(map.image, "png", imageFile.file());

        return imageFile.file();
    }

    public static EmbedBuilder getMapEmbedBuilder(Map map, File mapFile, File imageFile, Member member) {

        EmbedBuilder builder = new EmbedBuilder().setImage("attachment://" + imageFile.getName())
                .setAuthor(member.getEffectiveName(), member.getEffectiveAvatarUrl(), member.getEffectiveAvatarUrl())
                .setTitle(map.name == null ? "OH NO" : map.name);
        builder.addField("Size: ", "- " + map.image.getWidth() + "x" + map.image.getHeight(), false);
        if (map.description != null)
            builder.setFooter(map.description);

        return builder;

    }

    public static class Map {
        public String name, author, description;
        public ObjectMap<String, String> tags = new ObjectMap<>();
        public BufferedImage image;
    }
}
