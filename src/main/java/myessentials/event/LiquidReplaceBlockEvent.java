package myessentials.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

@Cancelable
public class LiquidReplaceBlockEvent extends BlockEvent {
    public final Block newBlock;
    public final int newBlockMetadata;

    public LiquidReplaceBlockEvent(int x, int y, int z, World world, Block block, int blockMetadata, Block newBlock, int newBlockMetadata) {
        super(x, y, z, world, block, blockMetadata);
        this.newBlock = newBlock;
        this.newBlockMetadata = newBlockMetadata;
    }

    public static boolean fireEvent(Block block, int x, int y, int z, World world) {
        return MinecraftForge.EVENT_BUS.post(new LiquidReplaceBlockEvent(x,y,z, world, block, world.getBlockMetadata(x,y,z), Blocks.stone, 0));
    }
}
