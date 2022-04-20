package com.abstractphil.croppulser.holos;

import ninja.coelho.dimm.libutil.asyncentity.AsyncEntitiesContext;
import ninja.coelho.dimm.libutil.asyncholo.AsyncHoloAlign;
import ninja.coelho.dimm.libutil.asyncholo.AsyncHoloLineType;
import ninja.coelho.dimm.libutil.asyncholo.VirtualHolo;
import ninja.coelho.dimm.libutil.asyncholo.VirtualHolosContext;
import org.bukkit.Location;

public class PulserHolo extends VirtualHolo{

    public PulserHolo(Location location, AsyncHoloAlign align, AsyncEntitiesContext entityContext) {
        super(location, align, entityContext);
    }
}
