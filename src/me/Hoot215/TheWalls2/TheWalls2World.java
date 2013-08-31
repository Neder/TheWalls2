/*
 * TheWalls2: The Walls 2 plugin. Copyright (C) 2012 Andrew Stevanus (Hoot215)
 * <hoot893@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.Hoot215.TheWalls2;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

public class TheWalls2World
  {
    public static boolean isRestoring = false;
    
    public static void reloadWorld (final TheWalls2 plugin)
      {
        plugin.getServer().broadcastMessage(
            ChatColor.AQUA + "[TheWalls2] " + ChatColor.YELLOW
                + "월드가 로드 해제됩니다...");
        isRestoring = true;
        World world = plugin.getServer().getWorld(TheWalls2.worldName);
        for (Player player : world.getPlayers())
          {
            if (player == null)
              break;
            
            player.kickPlayer("[TheWalls2] 월드가 로드 해제 상태일때는 "
                + "들어갈 수 없습니다! 몇 초 뒤 들어가세요.");
          }
        for (Player player : plugin.getRespawnQueue().getPlayerList())
          {
            if (player == null)
              break;
            
            player.kickPlayer("[TheWalls2] 월드가 로드 해제 상태일때는 "
                + "들어갈 수 없습니다! 몇 초 뒤 들어가세요.");
          }
        
        world.getPlayers().clear();
        
        if (plugin.getServer().unloadWorld(TheWalls2.worldName, false))
          {
            plugin.getServer().broadcastMessage(
                ChatColor.AQUA + "[TheWalls2] " + ChatColor.YELLOW
                    + "월드가 로드 해제됩니다...");
            WorldCreator wc = new WorldCreator(TheWalls2.worldName);
            World newWorld = plugin.getServer().createWorld(wc);
            newWorld.setAutoSave(false);
            plugin.getLocationData().setWorld(newWorld);
            isRestoring = false;
          }
        else
          {
            plugin.getServer().getScheduler()
                .scheduleSyncDelayedTask(plugin, new Runnable()
                  {
                    public void run ()
                      {
                        if (plugin.getServer().unloadWorld(TheWalls2.worldName,
                            false))
                          {
                            WorldCreator wc =
                                new WorldCreator(TheWalls2.worldName);
                            plugin.getServer().createWorld(wc)
                                .setAutoSave(false);
                          }
                        else
                          {
                            System.out.println("[TheWalls2] "
                                + "월드 로드 해제 실패!");
                          }
                        isRestoring = false;
                      }
                  }, 60L);
          }
      }
  }