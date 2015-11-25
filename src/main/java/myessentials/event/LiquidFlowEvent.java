package myessentials.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

@Cancelable
public class LiquidFlowEvent extends BlockEvent {
    public final int toX, toY, toZ;

    public LiquidFlowEvent(int x, int y, int z, World world, Block block, int blockMetadata, int toX, int toY, int toZ) {
        super(x, y, z, world, block, blockMetadata);
        this.toX = toX;
        this.toY = toY;
        this.toZ = toZ;
    }

    public static boolean fireEvent(Block block, World world, int toX, int toY, int toZ, int unknown, int x, int y, int z) {
        return MinecraftForge.EVENT_BUS.post(new LiquidFlowEvent(x,y,z, world, block, world.getBlockMetadata(x,y,z), toX,toY,toZ));
    }
}
