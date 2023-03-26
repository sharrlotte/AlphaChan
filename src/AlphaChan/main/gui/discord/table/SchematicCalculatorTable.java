package AlphaChan.main.gui.discord.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import AlphaChan.main.gui.discord.Table;
import AlphaChan.main.handler.ContentHandler;

import mindustry.Vars;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.type.UnitType;
import mindustry.world.blocks.heat.HeatProducer;
import mindustry.world.blocks.production.BeamDrill;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.production.Fracker;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.HeatCrafter;
import mindustry.world.blocks.production.Pump;
import mindustry.world.blocks.production.Separator;
import mindustry.world.blocks.production.WallCrafter;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.blocks.units.UnitFactory.UnitPlan;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquid;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;

public class SchematicCalculatorTable extends Table {

    public static enum Stat {

        HEAT("heat"), DRILL("drill"), PUMP("pump");

        private final String name;

        Stat(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Only block from those folder that container this
     */

    private static final String[] allowedBlockTypeSource = { "units", "production", "heat" };

    private static ConcurrentHashMap<String, BlockRatio> blockMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, LinkedList<BlockRatio>> sourceMap = new ConcurrentHashMap<>();

    private LinkedHashMap<BlockRatio, Integer> blocks = new LinkedHashMap<>();

    static {

        ContentHandler.getInstance();

        Vars.content.blocks().forEach((block) -> {
            for (String blockType : allowedBlockTypeSource) {
                if (!block.getClass().getGenericSuperclass().getTypeName().contains(blockType))
                    continue;

                BlockRatio blockRatio = new BlockRatio(block.name);

                for (Consume cons : block.consumers) {

                    if (cons instanceof ConsumeItems) {
                        ConsumeItems c = (ConsumeItems) cons;

                        for (ItemStack stack : c.items) {
                            blockRatio.addInput(stack.item.name, stack.amount);
                        }

                    } else if (cons instanceof ConsumeLiquid) {
                        ConsumeLiquid c = (ConsumeLiquid) cons;
                        blockRatio.addInput(c.liquid.name, c.amount * 60);
                    }
                }

                if (block instanceof GenericCrafter) {
                    GenericCrafter crafter = (GenericCrafter) block;

                    for (Consume cons : crafter.consumers) {

                        if (cons instanceof ConsumeItems) {
                            ConsumeItems c = (ConsumeItems) cons;

                            for (ItemStack stack : c.items) {
                                blockRatio.addInput(stack.item.name, stack.amount / crafter.craftTime * 60);
                            }

                        } else if (cons instanceof ConsumeLiquid) {
                            ConsumeLiquid c = (ConsumeLiquid) cons;
                            blockRatio.addInput(c.liquid.name, c.amount * 60);
                        }

                        if (crafter.outputItem != null) {
                            blockRatio.addOutput(crafter.outputItem.item.name, crafter.outputItem.amount / crafter.craftTime * 60);

                        } else {
                            if (crafter.outputItems != null) {
                                for (ItemStack stack : crafter.outputItems) {
                                    blockRatio.addOutput(stack.item.name, stack.amount / crafter.craftTime * 60);
                                }
                            }
                        }

                        if (crafter.outputLiquid != null) {
                            blockRatio.addOutput(crafter.outputLiquid.liquid.name, crafter.outputLiquid.amount * 60);

                        } else {
                            if (crafter.outputLiquids != null) {
                                for (LiquidStack stack : crafter.outputLiquids) {
                                    blockRatio.addOutput(stack.liquid.name, stack.amount * 60);
                                }
                            }
                        }
                    }

                } else if (block instanceof Reconstructor) {
                    Reconstructor constructor = (Reconstructor) block;

                    for (Consume cons : constructor.consumers) {
                        if (cons instanceof ConsumeItems) {
                            ConsumeItems c = (ConsumeItems) cons;

                            for (ItemStack stack : c.items) {
                                blockRatio.addInput(stack.item.name, stack.amount / constructor.constructTime * 60);
                            }

                        } else if (cons instanceof ConsumeLiquid) {
                            ConsumeLiquid c = (ConsumeLiquid) cons;
                            blockRatio.addInput(c.liquid.name, c.amount * 60);
                        }
                    }

                    for (UnitType[] type : constructor.upgrades) {
                        blockRatio.addOutput(type[1].name, 1 / constructor.constructTime / 60);
                    }

                } else if (block instanceof UnitFactory) {
                    UnitFactory factory = (UnitFactory) block;
                    for (UnitPlan plan : factory.plans) {

                        for (ItemStack stack : plan.requirements) {
                            blockRatio.addInput(stack.item.name, stack.amount * plan.time / 60);
                        }

                        blockRatio.addInput(plan.unit.name, plan.time / 60);
                    }
                }

                if (block instanceof HeatProducer) {
                    HeatProducer heatProducer = (HeatProducer) block;
                    blockRatio.addOutput(Stat.HEAT, heatProducer.heatOutput);

                } else if (block instanceof HeatCrafter) {
                    HeatCrafter heatCrafter = (HeatCrafter) block;
                    blockRatio.addInput(Stat.HEAT, heatCrafter.heatRequirement);

                } else if (block instanceof WallCrafter) {
                    WallCrafter wallCrafter = (WallCrafter) block;
                    blockRatio.addOutput(Stat.DRILL, 60f / wallCrafter.drillTime * wallCrafter.size);

                } else if (block instanceof Drill) {
                    Drill drill = (Drill) block;
                    blockRatio.addOutput(Stat.DRILL, 60f / drill.drillTime * drill.size * drill.size);

                } else if (block instanceof BeamDrill) {
                    BeamDrill beamDrill = (BeamDrill) block;
                    blockRatio.addOutput(Stat.DRILL, 60f / beamDrill.drillTime * beamDrill.size);

                } else if (block instanceof Fracker) {
                    Fracker fracker = (Fracker) block;
                    blockRatio.addOutput(fracker.result.name, fracker.pumpAmount * 60);

                } else if (block instanceof Pump) {
                    Pump pump = (Pump) block;
                    blockRatio.addOutput(Stat.PUMP, pump.pumpAmount * 60 * pump.size * pump.size);

                } else if (block instanceof Separator) {
                    Separator separator = (Separator) block;
                    for (ItemStack stack : separator.results) {
                        blockRatio.addOutput(stack.item.name, stack.amount * separator.craftTime / 60);
                    }
                }

                blockMap.put(blockRatio.name, blockRatio);
            }
        });

        for (BlockRatio block : blockMap.values()) {
            for (String key : block.getOutput().keySet()) {
                LinkedList<BlockRatio> list = sourceMap.get(key);
                if (list == null) {
                    list = new LinkedList<>();
                    sourceMap.put(key, list);
                }
                list.add(block);
            }
        }

        // for (String key : sourceMap.keySet()) {
        // LinkedList<BlockRatio> list = sourceMap.get(key);
        // String s = key + ": ";
        // for (BlockRatio r : list)
        // s += r.name + ", ";

        // System.out.format("%s \n", s);
        // }
    }

    public SchematicCalculatorTable(SlashCommandInteractionEvent event) {
        super(event, 30); // 30 updates

        addButton(deny("X", () -> deleteTable()));

        onPrepareTable.connect(this::onPrepareTable);
    }

    public boolean setBlock(String blockName, int number) {

        if (number < 0) {
            removeBlock(blockName);
            return true;
        }

        if (!blockMap.containsKey(blockName))
            return false;

        BlockRatio block = blockMap.get(blockName);
        if (block == null)
            throw new IllegalArgumentException("Block is not exists <" + blockName + ">");

        blocks.put(block, number);
        updateTable();

        return true;
    }

    public void removeBlock(String blockName) {
        if (!blockMap.containsKey(blockName))
            return;

        BlockRatio block = blockMap.get(blockName);
        blocks.remove(block);
        updateTable();
    }

    public static ConcurrentHashMap<String, BlockRatio> getBlockMap() {
        return blockMap;
    }

    public static ConcurrentHashMap<String, LinkedList<BlockRatio>> getSourceMap() {
        return sourceMap;
    }

    @Override
    public String getId() {
        return getEvent().getMember().getId();
    }

    public void onPrepareTable(MessageEditAction action) {

        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder field = new StringBuilder();

        HashMap<String, Float> input = new HashMap<>();
        HashMap<String, Float> output = new HashMap<>();

        for (Entry<BlockRatio, Integer> entry : blocks.entrySet()) {
            BlockRatio block = entry.getKey();
            int number = entry.getValue();

            HashMap<String, Float> blockInput = block.getInput();
            HashMap<String, Float> blockOutput = block.getOutput();

            for (String in : blockInput.keySet()) {
                float total = 0;
                ArrayList<BlockRatio> blockNeedCal = new ArrayList<>();
                for (Entry<BlockRatio, Integer> entry2 : blocks.entrySet()) {
                    if (entry2.getKey().getOutput().containsKey(in)) {
                        if (entry2.getValue() == 0) {
                            blockNeedCal.add(entry2.getKey());

                        } else if (entry2.getValue() > 0) {
                            total += entry2.getKey().getOutput().get(in) * entry2.getValue();
                        }
                    }
                }

                float need = block.getInput().get(in) * number - total;
                if (need <= 0 || blockNeedCal.size() == 0)
                    continue;

                int numbedNeed = (int) (need / blockNeedCal.get(0).getOutput().get(in)) + 1;
                blocks.put(blockNeedCal.get(0), numbedNeed);
            }

            for (String key : blockInput.keySet()) {
                ContentHandler.add(input, key, blockInput.get(key) * number);
            }

            for (String key : blockOutput.keySet()) {
                ContentHandler.add(output, key, blockOutput.get(key) * number);
            }
        }

        HashMap<String, Float> resource = new HashMap<>();
        HashMap<String, Float> finalInput = new HashMap<>(input);
        HashMap<String, Float> finalOutput = new HashMap<>(output);

        Iterator<String> itr = finalInput.keySet().iterator();

        while (itr.hasNext()) {
            String key = itr.next();
            if (finalOutput.containsKey(key)) {
                resource.put(key, finalInput.get(key) - finalOutput.get(key));
                itr.remove();
                finalOutput.remove(key);
            }
        }

        for (BlockRatio block : blocks.keySet()) {
            field.append(block.name + ": " + blocks.get(block) + "\n");
        }

        builder.addField("Current blocks", field.toString(), false);
        field = new StringBuilder();

        for (String key : resource.keySet()) {
            field.append(key + ": " + resource.get(key) + "\n");
        }

        builder.addField("Resource", field.toString(), false);
        field = new StringBuilder();

        for (String key : finalInput.keySet()) {
            field.append(key + ": " + finalInput.get(key) + "\n");
        }

        builder.addField("Final input", field.toString(), false);
        field = new StringBuilder();

        for (String key : finalOutput.keySet()) {
            field.append(key + ": " + finalOutput.get(key) + "\n");
        }

        builder.addField("Final output", field.toString(), false);
        field = new StringBuilder();
    }

    private static class BlockRatio {

        public final String name;

        private HashMap<String, Float> input = new HashMap<>();
        private HashMap<String, Float> output = new HashMap<>();

        public BlockRatio(String name) {
            this.name = name;
        }

        public HashMap<String, Float> getInput() {
            return input;
        }

        public HashMap<String, Float> getOutput() {
            return output;
        }

        public void addInput(Object name, float value) {
            input.put(name.toString(), value);
        }

        public void addOutput(Object name, float value) {
            output.put(name.toString(), value);
        }
    }

}
