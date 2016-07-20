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
    public final int replacedX, replacedY, replacedZ;

    public LiquidReplaceBlockEvent(int x, int y, int z, World world, Block block, int blockMetadata,
                                   Block newBlock, int newBlockMetadata, int replacedX, int replacedY, int replacedZ) {
        super(x, y, z, world, block, blockMetadata);
        this.newBlock = newBlock;
        this.newBlockMetadata = newBlockMetadata;
        this.replacedX = replacedX;
        this.replacedY = replacedY;
        this.replacedZ = replacedZ;
    }

    public static boolean fireEventReplaceBelow(Block block, int x, int y, int z, World world) {
        return MinecraftForge.EVENT_BUS.post(new LiquidReplaceBlockEvent(x,y,z, world, block, world.getBlockMetadata(x,y,z), Blocks.stone, 0, x, y-1, z));
    }

    public static boolean fireEvent(Block block, World world, int x, int y, int z, int modifiedX, int modifiedY, int modifiedZ) {
        int blockMetadata = world.getBlockMetadata(x, y, z);

        Block replacement;
        int replacementMeta;
        if(blockMetadata == 0) {
            replacement = Blocks.obsidian;
            replacementMeta = 0;
        }
        else if(blockMetadata <= 4) {
            replacement = Blocks.cobblestone;
            replacementMeta = 0;
        }
        else {
            replacement = world.getBlock(modifiedX, modifiedY, modifiedZ);
            replacementMeta = world.getBlockMetadata(modifiedX,modifiedY,modifiedZ);
        }

        return MinecraftForge.EVENT_BUS.post(
                new LiquidReplaceBlockEvent(
                        x,y,z,world,block,blockMetadata,
                        replacement,replacementMeta,modifiedX,modifiedY,modifiedZ
                )
        );
    }
}
