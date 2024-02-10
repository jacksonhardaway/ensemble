package dev.hardaway.ensemble.common.registry;

import dev.hardaway.ensemble.Ensemble;
import dev.hardaway.ensemble.common.attachment.InstrumentHolderAttachment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;

public class EnsembleAttachments {

    public static final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Ensemble.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<InstrumentHolderAttachment>> INSTRUMENT_HOLDER = REGISTRY.register("instrument_holder", () -> AttachmentType.builder(() -> new InstrumentHolderAttachment(null)).serialize(InstrumentHolderAttachment.CODEC).build());
}
