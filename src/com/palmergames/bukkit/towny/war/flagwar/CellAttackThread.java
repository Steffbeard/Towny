// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.war.flagwar;

import java.util.TimerTask;

public class CellAttackThread extends TimerTask
{
    CellUnderAttack cell;
    
    public CellAttackThread(final CellUnderAttack cellUnderAttack) {
        this.cell = cellUnderAttack;
    }
    
    @Override
    public void run() {
        this.cell.changeFlag();
        if (this.cell.hasEnded()) {
            TownyWar.attackWon(this.cell);
        }
    }
}
