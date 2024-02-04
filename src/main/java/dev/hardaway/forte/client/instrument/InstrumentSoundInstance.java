package dev.hardaway.forte.client.instrument;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InstrumentSoundInstance extends AbstractTickableSoundInstance {

    private final Entity entity;
    private boolean noteOn = true;
    private long endLife = 200L;

    public InstrumentSoundInstance(SoundEvent soundEvent, SoundSource source, float volume) {
        super(soundEvent, source, SoundInstance.createUnseededRandom());
        this.volume = volume;
        this.entity = null;
    }

    public InstrumentSoundInstance(SoundEvent soundEvent, SoundSource source, float volume, @NotNull Entity entity) {
        super(soundEvent, source, SoundInstance.createUnseededRandom());
        this.volume = volume;
        this.entity = entity;
        this.x = this.entity.getX();
        this.y = this.entity.getY();
        this.z = this.entity.getZ();
    }

    public InstrumentSoundInstance(SoundEvent soundEvent, SoundSource source, float volume, BlockPos pos) {
        this(soundEvent, source, volume);
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public void noteOff() {
        this.noteOn = false;
    }

    @Override
    public boolean canPlaySound() {
        return this.entity == null || !this.entity.isSilent();
    }

    @Override
    public void tick() {
        if (this.entity != null) {
            if (this.entity.isRemoved()) {
                this.stop();
            } else {
                this.x = this.entity.getX();
                this.y = this.entity.getY();
                this.z = this.entity.getZ();
            }
        }

        if (!this.noteOn) {
            this.volume *= 0.75F;
            if (this.endLife-- <= 0) {
                this.stop();
            }
        }
    }
}
