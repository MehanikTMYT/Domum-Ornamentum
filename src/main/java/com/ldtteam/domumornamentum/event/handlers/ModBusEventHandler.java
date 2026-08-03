package com.ldtteam.domumornamentum.event.handlers;

import com.ldtteam.domumornamentum.network.messages.CreativeSetArchitectCutterSlotMessage;
import com.ldtteam.domumornamentum.util.Constants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class ModBusEventHandler
{
    /**
     * Called when mod is being initialized.
     *
     * @param event event
     */
    @SubscribeEvent
    public static void onNetworkRegistry(final RegisterPayloadHandlersEvent event)
    {
        final String modVersion = ModList.get().getModContainerById(Constants.MOD_ID).get().getModInfo().getVersion().toString();
        final PayloadRegistrar registry = event.registrar(Constants.MOD_ID).versioned(modVersion);

        registry.playToServer(CreativeSetArchitectCutterSlotMessage.ID, CreativeSetArchitectCutterSlotMessage.CODEC, CreativeSetArchitectCutterSlotMessage::onExecute);
    }

    }
