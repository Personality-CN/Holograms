package com.sainttx.holograms.nms.v1_9_R2;

import com.sainttx.holograms.api.entity.Nameable;
import com.sainttx.holograms.api.line.HologramLine;
import net.minecraft.server.v1_9_R2.AxisAlignedBB;
import net.minecraft.server.v1_9_R2.DamageSource;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.EnumHand;
import net.minecraft.server.v1_9_R2.EnumInteractionResult;
import net.minecraft.server.v1_9_R2.ItemStack;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_9_R2.SoundEffect;
import net.minecraft.server.v1_9_R2.Vec3D;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;

public class EntityNameable extends EntityArmorStand implements Nameable {

    private boolean lockTick;
    private HologramLine parentPiece;

    public EntityNameable(World world, HologramLine parentPiece) {
        super(world);
        super.a(new NullBoundingBox()); // Forces the bounding box
        setInvisible(true);
        setSmall(true);
        setArms(false);
        setGravity(true);
        setBasePlate(true);
        setMarker(true);
        this.parentPiece = parentPiece;
        setLockTick(true);
    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
    }

    @Override
    public boolean c(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
        return false;
    }

    @Override
    public boolean d(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
        return false;
    }

    @Override
    public NBTTagCompound e(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
        return new NBTTagCompound();
    }


    @Override
    public boolean isInvulnerable(DamageSource source) {
        /*
         * The field Entity.invulnerable is private.
		 * It's only used while saving NBTTags, but since the entity would be killed
		 * on chunk unload, we prefer to override isInvulnerable().
		 */
        return true;
    }

    @Override
    public void setName(String text) {
        super.setCustomName(text);
        super.setCustomNameVisible(!text.isEmpty());
    }

    @Override
    public String getName() {
        return super.getCustomName();
    }

    @Override
    public void setCustomNameVisible(boolean visible) {
        // Locks the custom name.
    }

    @Override
    public EnumInteractionResult a(EntityHuman entityhuman, Vec3D vec3d, ItemStack itemstack, EnumHand enumhand) {
        // Prevent stand being equipped
        return EnumInteractionResult.FAIL;
    }

    @Override
    public boolean c(int i, ItemStack item) {
        // Prevent stand being equipped
        return false;
    }

    @Override
    public void a(AxisAlignedBB boundingBox) {
        // Do not change it!
    }

    @Override
    public int getId() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length > 2 && elements[2] != null && elements[2].getFileName().equals("EntityTrackerEntry.java") && elements[2].getLineNumber() > 137 && elements[2].getLineNumber() < 147) {
            return -1;
        }

        return super.getId();
    }

    @Override
    public void m() {
        if (!lockTick) {
            super.playStepSound();
        }
    }

    @Override
    public void a(SoundEffect soundeffect, float f, float f1) {
        // Remove sounds.
    }

    public void setLockTick(boolean lock) {
        lockTick = lock;
    }

    @Override
    public void remove() {
        die();
    }

    @Override
    public void die() {
        setLockTick(false);
        super.die();
    }

    @Override
    public CraftEntity getBukkitEntity() {
        if (super.bukkitEntity == null) {
            this.bukkitEntity = new CraftNameable(this.world.getServer(), this);
        }
        return this.bukkitEntity;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);

        // Send a packet near to update the position.
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(this);
        this.world.players.stream()
                .filter(p -> p instanceof EntityPlayer)
                .map(p -> (EntityPlayer) p)
                .forEach(p -> {
                    double distanceSquared = Math.pow(p.locX - this.locX, 2) + Math.pow(p.locZ - this.locZ, 2);
                    if (distanceSquared < 8192 && p.playerConnection != null) {
                        p.playerConnection.sendPacket(teleportPacket);
                    }
                });
    }

    @Override
    public HologramLine getHologramLine() {
        return parentPiece;
    }

}
