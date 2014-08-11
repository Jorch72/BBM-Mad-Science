package madscience.mobs.abomination;

import net.minecraft.util.MathHelper;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class AbominationMobLivingHandler
{
    @ForgeSubscribe
    public void onEntityLivingDeath(LivingDeathEvent event) // NO_UCD (unused code)
    {
        if (event.entityLiving.worldObj.isRemote)
        {
            return;
        }

        if (event.source.getSourceOfDamage() instanceof AbominationMobEntity)
        {
            // MadScience.logger.info("ABOMINATION KILLED: " + event.entityLiving);

            int someBlockID = MadBlocks.ABOMINATIONEGGBLOCK.blockID;
            int x = MathHelper.floor_double(event.entityLiving.posX);
            int y = MathHelper.floor_double(event.entityLiving.posY);
            int z = MathHelper.floor_double(event.entityLiving.posZ);

            event.entityLiving.worldObj.setBlock(x, y, z, someBlockID);
        }
    }
}
