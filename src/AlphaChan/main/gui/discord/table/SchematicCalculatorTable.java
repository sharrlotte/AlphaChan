package AlphaChan.main.gui.discord.table;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import AlphaChan.main.command.Table;
import AlphaChan.main.handler.ContentHandler;
import mindustry.Vars;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquid;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;

public class SchematicCalculatorTable extends Table {

    public static String[] allowedBlockType = { "units", "production", "heat" };

    public static ConcurrentHashMap<String, BlockRatio> blockMap = new ConcurrentHashMap<>();

    static {
        ContentHandler.getInstance();
        Vars.content.blocks().forEach((block) -> {
            for (String blockType : allowedBlockType) {
                if (!block.getClass().getGenericSuperclass().getTypeName().contains(blockType))
                    continue;

                BlockRatio blockRatio = new BlockRatio(block.name);

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
                }

                blockMap.put(blockRatio.name, blockRatio);
            }
        });

        for (BlockRatio r : blockMap.values()) {
            System.out.format("name: %-25s  input: %-50s   output: %-50s\n", r.name, r.getInput().toString(), r.getOutput().toString());
        }
    }

    public static void main(String[] args) {

    }

    public SchematicCalculatorTable(SlashCommandInteractionEvent event) {
        super(event, 30); // 30 updates
    }

    @Override
    public String getId() {
        return getEvent().getMember().getId();
    }

    @Override
    public void updateTable() {

        resetTimer();

        MessageEditAction action = getMessage().editMessage("Test");

        setButtons(action);

        action.queue();
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

        public void addInput(String name, float value) {
            input.put(name, value);
        }

        public void addOutput(String name, float value) {
            output.put(name, value);
        }
    }

}
