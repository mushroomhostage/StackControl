/*
Copyright (c) 2012, Mushroom Hostage
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package me.exphc.StackControl;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Formatter;
import java.lang.Byte;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.*;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.*;
import org.bukkit.Material.*;
import org.bukkit.material.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import org.bukkit.inventory.*;
import org.bukkit.configuration.*;
import org.bukkit.configuration.file.*;
import org.bukkit.scheduler.*;
import org.bukkit.enchantments.*;
import org.bukkit.*;

import net.minecraft.server.CraftingManager;

import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

public class StackControl extends JavaPlugin implements Listener {
    Logger log = Logger.getLogger("Minecraft");


    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadConfig();

        List<Map<?,?>> maps = getConfig().getMapList("items");
        for (Map<?,?> map: maps) {
            int id;

            // Get id
            Object objId = map.get("id");
            if (objId == null) {
                log.warning("Ignoring entry without id: " + map);
                continue;
            }
            if (objId instanceof Integer) {
                id = ((Integer)objId).intValue();
            } else if (objId instanceof String) {
                Material material = Material.matchMaterial((String)objId);
                if (material == null) {
                    log.warning("Ignoring entry with invalid material name: " + objId + " in " + map);
                    continue;
                }
                id = material.getId();
            } else {
                log.warning("Ignoring entry with unrecognized material type: " + objId + " in " + map);
                continue;
            }

            // Read items map into hash
            for (Map.Entry<?,?> entry: map.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();

                if (!(key instanceof String)) {
                    log.warning("Ignoring non-string key: " + key + " in " + map);
                    continue;
                }
                
                String name = (String)key;
                if (name.equals("id")) {
                    continue;
                }

                String translatedName = getConfig().getString("fields." + name, name);

                setField(translatedName, id, value);
            }
        }

        // TODO: craftingResult = containerItem (for water buckets, etc., keeps on grid)
        //setField("fieldContainerItem", "craftingResult", 1, ...);

        // Other item fields, not set here
        // should max damage work? for tools..or is it client-side, too?
        //setField("fieldMaxDamage", "durability", Material.DIAMOND_SWORD.getId(), 1);
        // bS = bFull3D (client-side)
        //setField("fieldBFull3D", "bS", 1, true);
        // bU = potionEffect (doesn't seem to useful?)
        //setField("fieldPotionEffect", "bU", 1, "foo");
        // textureId
        // name
    }

    public void setField(String fieldName, int id, Object value) {
        try {
            Field field = net.minecraft.server.Item.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            if (value instanceof Integer) {
                field.setInt(net.minecraft.server.Item.byId[id], ((Integer)value).intValue());
            } else if (value instanceof Boolean) {
                field.setBoolean(net.minecraft.server.Item.byId[id], ((Boolean)value).booleanValue());
            } else { 
                field.set(net.minecraft.server.Item.byId[id], value);
            } 
        } catch (Exception e) {
            log.warning("Failed to set field '"+fieldName+" for id "+id+ ": " + e);
        }
        log.info("Set field " + id + "." + fieldName + " = " + value);
    }

    public void onDisable() {
    }

}
