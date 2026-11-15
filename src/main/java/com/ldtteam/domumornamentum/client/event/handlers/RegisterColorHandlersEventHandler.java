package com.ldtteam.domumornamentum.client.event.handlers;

import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Legacy entry point for callers registering material colors manually.
 * Automatic registration belongs to MateriallyTexturedBlockTintProvider.
 */
public class RegisterColorHandlersEventHandler
{
    public static void onRegisterColorHandlersBlock(final RegisterClientExtensionsEvent event)
    {
        MateriallyTexturedBlockTintProvider.onRegisterClientExtensions(event);
    }
}
