package madscience.mobs.shoggoth;

import madscience.MadFluids;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class ShoggothMobEntity extends EntityLiving implements IMob
{
    public float prevSquishFactor;
    /** the time between each jump of the slime */
    private int slimeJumpDelay;
    public float squishAmount;

    public float squishFactor;

    public ShoggothMobEntity(World par1World)
    {
        super(par1World);
        
        this.yOffset = 0.0F;
        this.slimeJumpDelay = this.rand.nextInt(5) + 10;
        
        int i = 1 << this.rand.nextInt(3);
        this.setSlimeSize(i);
       
        // The below means if possible, it wont walk into water
        this.getNavigator().setAvoidsWater(false);
    }

    protected void alterSquishAmount()
    {
        this.squishAmount *= 0.9F;
    }

    /** Indicates weather the slime is able to damage the player (based upon the slime's size) */
    protected boolean canDamagePlayer()
    {
        return this.getSlimeSize() > 1;
    }

    protected ShoggothMobEntity createInstance()
    {
        return new ShoggothMobEntity(this.worldObj);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, new Byte((byte) 1));
    }

    /** Gets the amount of damage dealt to the player when "attacked" by the slime. */
    protected int getAttackStrength()
    {
        return this.getSlimeSize();
    }

    /** Checks if the entity's current position is a valid location to spawn this entity. */
    @Override
    public boolean getCanSpawnHere()
    {
        Chunk chunk = this.worldObj.getChunkFromBlockCoords(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posZ));

        if (this.worldObj.getWorldInfo().getTerrainType().handleSlimeSpawnReduction(rand, worldObj))
        {
            return false;
        }
        else
        {
            if (this.getSlimeSize() == 1 || this.worldObj.difficultySetting > 0)
            {
                BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posZ));

                if (biomegenbase == BiomeGenBase.swampland && this.posY > 50.0D && this.posY < 70.0D && this.rand.nextFloat() < 0.5F && this.rand.nextFloat() < this.worldObj.getCurrentMoonPhaseFactor()
                        && this.worldObj.getBlockLightValue(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)) <= this.rand.nextInt(8))
                {
                    return super.getCanSpawnHere();
                }

                if (this.rand.nextInt(10) == 0 && chunk.getRandomWithSeed(987234911L).nextInt(10) == 0 && this.posY < 40.0D)
                {
                    return super.getCanSpawnHere();
                }
            }

            return false;
        }
    }

    /** Returns the sound this mob makes on death. */
    @Override
    protected String getDeathSound()
    {
        return "mob.slime." + (this.getSlimeSize() > 1 ? "big" : "small");
    }

    /** Returns the item ID for the item the mob drops on death. */
    @Override
    protected int getDropItemId()
    {
        return this.getSlimeSize() == 1 ? Item.slimeBall.itemID : 0;
    }

    /** Returns the sound this mob makes when it is hurt. */
    @Override
    protected String getHurtSound()
    {
        return "mob.slime." + (this.getSlimeSize() > 1 ? "big" : "small");
    }

    /** Gets the amount of time the slime needs to wait between jumps. */
    protected int getJumpDelay()
    {
        return this.rand.nextInt(2) + 10;
    }

    /** Returns the name of the sound played when the slime jumps. */
    protected String getJumpSound()
    {
        return "mob.slime." + (this.getSlimeSize() > 1 ? "big" : "small");
    }

    /** Returns the name of a particle effect that may be randomly created by EntitySlime.onUpdate() */
    protected String getSlimeParticle()
    {
        return "slime";
    }

    /** Returns the size of the slime. */
    public int getSlimeSize()
    {
        return this.dataWatcher.getWatchableObjectByte(16);
    }
    
    /** Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig. */
    @Override
    public boolean interact(EntityPlayer par1EntityPlayer)
    {
        ItemStack itemstack = par1EntityPlayer.inventory.getCurrentItem();

        if (itemstack != null && itemstack.itemID == Item.slimeBall.itemID && !par1EntityPlayer.capabilities.isCreativeMode)
        {
            if (itemstack.stackSize-- >= 1)
            {
                int i = this.getSlimeSize();
                if (i < 3)
                {
                    this.setSlimeSize(++i);
                }
            }

            return true;
        }
        else
        {
            return super.interact(par1EntityPlayer);
        }
    }

    /** Returns the volume for the sounds this mob makes. */
    @Override
    protected float getSoundVolume()
    {
        return 0.4F * this.getSlimeSize();
    }

    /** The speed it takes to move the entityliving's rotationPitch through the faceEntity method. This is only currently use in wolves. */
    @Override
    public int getVerticalFaceSpeed()
    {
        return 0;
    }

    /** Returns true if the slime makes a sound when it jumps (based upon the slime's size) */
    protected boolean makesSoundOnJump()
    {
        return this.getSlimeSize() > 0;
    }

    /** Returns true if the slime makes a sound when it lands after a jump (based upon the slime's size) */
    protected boolean makesSoundOnLand()
    {
        return this.getSlimeSize() > 2;
    }

    /** Called by a player entity when they collide with an entity */
    @Override
    public void onCollideWithPlayer(EntityPlayer par1EntityPlayer)
    {
        if (this.canDamagePlayer())
        {
            int i = this.getSlimeSize();

            if (this.canEntityBeSeen(par1EntityPlayer) && this.getDistanceSqToEntity(par1EntityPlayer) < 0.6D * i * 0.6D * i && par1EntityPlayer.attackEntityFrom(DamageSource.causeMobDamage(this), this.getAttackStrength()))
            {
                this.playSound("mob.attack", 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            }
        }
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void onUpdate()
    {
        if (!this.worldObj.isRemote && this.worldObj.difficultySetting == 0 && this.getSlimeSize() > 0)
        {
            this.isDead = true;
        }

        this.squishFactor += (this.squishAmount - this.squishFactor) * 0.9F;
        this.prevSquishFactor = this.squishFactor;
        boolean flag = this.onGround;
        super.onUpdate();
        int i;

        if (this.onGround && !flag)
        {
            i = this.getSlimeSize();

            for (int j = 0; j < i * 8; ++j)
            {
                float f = this.rand.nextFloat() * (float) Math.PI * 2.0F;
                float f1 = this.rand.nextFloat() * 0.5F + 0.5F;
                float f2 = MathHelper.sin(f) * i * 0.5F * f1;
                float f3 = MathHelper.cos(f) * i * 0.5F * f1;
                this.worldObj.spawnParticle(this.getSlimeParticle(), this.posX + f2, this.boundingBox.minY, this.posZ + f3, 0.0D, 0.0D, 0.0D);
            }

            if (this.makesSoundOnLand())
            {
                this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) / 0.8F);
            }

            this.squishAmount = -0.2F;
        }
        else if (!this.onGround && flag)
        {
            this.squishAmount = 1.0F;
        }

        this.alterSquishAmount();

        if (this.worldObj.isRemote)
        {
            i = this.getSlimeSize();
            this.setSize(0.6F * i, 0.6F * i);
        }
    }

    /** (abstract) Protected helper method to read subclass entity data from NBT. */
    @Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        this.setSlimeSize(par1NBTTagCompound.getInteger("Size") + 1);
    }

    /** Will get destroyed next tick. */
    @Override
    public void setDead()
    {
        int i = this.getSlimeSize();

        if (!this.worldObj.isRemote && i > 1 && this.getHealth() <= 0.0F)
        {
            int j = 2 + this.rand.nextInt(3);

            for (int k = 0; k < j; ++k)
            {
                float f = (k % 2 - 0.5F) * i / 4.0F;
                float f1 = (k / 2 - 0.5F) * i / 4.0F;
                ShoggothMobEntity entityslime = this.createInstance();
                this.entityDropItem(new ItemStack(Item.dyePowder, 1, 0), 0.0F);
                entityslime.setSlimeSize(i / 2);
                entityslime.setLocationAndAngles(this.posX + f, this.posY + 0.5D, this.posZ + f1, this.rand.nextFloat() * 360.0F, 0.0F);
                this.worldObj.spawnEntityInWorld(entityslime);
            }
        }

        super.setDead();
    }

    protected void setSlimeSize(int par1)
    {
        this.dataWatcher.updateObject(16, new Byte((byte) par1));
        this.setSize(0.6F * par1, 0.6F * par1);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(par1 * par1);
        this.setHealth(this.getMaxHealth());
        this.experienceValue = par1;
    }
    
    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(15.0D);
    }

    @Override
    protected void updateEntityActionState()
    {
        this.despawnEntity();
        EntityPlayer entityplayer = this.worldObj.getClosestVulnerablePlayerToEntity(this, 24.0D);

        if (entityplayer != null)
        {
            this.faceEntity(entityplayer, 10.0F, 20.0F);
        }

        if (this.onGround && this.slimeJumpDelay-- <= 0)
        {
            this.slimeJumpDelay = this.getJumpDelay();

            if (entityplayer != null)
            {
                this.slimeJumpDelay /= 3;
            }

            this.isJumping = true;

            if (this.makesSoundOnJump())
            {
                this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F) * 0.8F);
            }

            this.moveStrafing = 1.0F - this.rand.nextFloat() * 2.0F;
            this.moveForward = 1 * this.getSlimeSize();
        }
        else
        {
            this.isJumping = false;

            if (this.onGround)
            {
                this.moveStrafing = this.moveForward = 0.0F;
            }
        }
    }

    /** (abstract) Protected helper method to write subclass entity data to NBT. */
    @Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("Size", this.getSlimeSize() - 1);
    }
}
