// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.chat;

import net.tnemc.tnc.core.common.chat.ChatHandler;
import net.tnemc.tnc.core.common.api.TNCAPI;

public class TNCRegister
{
    public static void initialize() {
        TNCAPI.addHandler((ChatHandler)new TheNewChatHandler());
    }
}
