// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.chat;

import com.palmergames.bukkit.towny.chat.checks.MayorCheck;
import net.tnemc.tnc.core.common.chat.ChatCheck;
import com.palmergames.bukkit.towny.chat.checks.KingCheck;
import com.palmergames.bukkit.towny.chat.variables.TitleVariable;
import com.palmergames.bukkit.towny.chat.variables.TownVariable;
import net.tnemc.tnc.core.common.chat.ChatVariable;
import com.palmergames.bukkit.towny.chat.variables.NationVariable;
import com.palmergames.bukkit.towny.chat.types.TownType;
import com.palmergames.bukkit.towny.chat.types.NationType;
import net.tnemc.tnc.core.common.chat.ChatType;
import com.palmergames.bukkit.towny.chat.types.AllyType;
import net.tnemc.tnc.core.common.chat.ChatHandler;

public class TheNewChatHandler extends ChatHandler
{
    public TheNewChatHandler() {
        this.addType((ChatType)new AllyType());
        this.addType((ChatType)new NationType());
        this.addType((ChatType)new TownType());
        this.addVariable((ChatVariable)new NationVariable());
        this.addVariable((ChatVariable)new TownVariable());
        this.addVariable((ChatVariable)new TitleVariable());
        this.addCheck((ChatCheck)new KingCheck());
        this.addCheck((ChatCheck)new MayorCheck());
    }
    
    public String getName() {
        return "towny";
    }
}
