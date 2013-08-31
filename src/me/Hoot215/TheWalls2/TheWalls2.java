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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.Hoot215.TheWalls2.api.AddonLoader;
import me.Hoot215.TheWalls2.metrics.Metrics;
import me.Hoot215.TheWalls2.util.AutoUpdater;
import me.Hoot215.TheWalls2.util.Teleport;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class TheWalls2 extends JavaPlugin
  {
    private TheWalls2 plugin = this;
    public static String worldName;
    public static String fallbackWorldName;
    public static Economy economy = null;
    private AddonLoader addonLoader;
    private AutoUpdater autoUpdater;
    private TheWalls2PlayerQueue queue;
    private TheWalls2GameTeams teams;
    private TheWalls2GameList gameList;
    private TheWalls2RespawnQueue respawnQueue;
    private TheWalls2LocationData locData;
    private TheWalls2World wallsWorld;
    private TheWalls2Inventory inventories;
    private Listener playerListener;
    private Listener entityListener;
    
    public boolean onCommand (CommandSender sender, Command cmd, String label,
      String[] args)
      {
        if (cmd.getName().equalsIgnoreCase("월즈"))
          {
            if (args.length == 0)
              {
                if (sender.hasPermission("thewalls2.command.thewalls"))
                  {
                    sender.sendMessage(ChatColor.GREEN + "****"
                        + ChatColor.BOLD + ChatColor.AQUA + " The Walls 2 - 한글화 Neder "
                        + ChatColor.RESET + ChatColor.GREEN + " ****");
                    sender.sendMessage(ChatColor.YELLOW + "/월즈"
                        + ChatColor.WHITE + " - TheWalls2 의 도움말 표시");
                    sender.sendMessage(ChatColor.YELLOW + "/월즈 들어가기"
                        + ChatColor.WHITE + " - 게임 대기실에 들어가기");
                    sender.sendMessage(ChatColor.YELLOW + "/월즈 나가기"
                        + ChatColor.WHITE + " - 게임 대기실에서 나가기");
                    sender.sendMessage(ChatColor.YELLOW
                        + "/월즈 팀 <1-4>" + ChatColor.WHITE
                        + " - 선택한 팀에 들어가기");
                    sender.sendMessage(ChatColor.RED + "/월즈 시작"
                        + ChatColor.WHITE + " - 게임 시작하기");
                    sender.sendMessage(ChatColor.RED + "/월즈 종료"
                        + ChatColor.WHITE + " - 게임 종료하기");
                    sender.sendMessage(ChatColor.RED + "/월즈 월드복구"
                        + ChatColor.WHITE + " - 월드 복구하기");
                    return true;
                  }
                sender.sendMessage(ChatColor.RED
                    + "당신은 해당 명령어를 사용하기 위한 권한이 없습니다!");
                return true;
              }
            else if (args.length == 1)
              {
                if (args[0].equalsIgnoreCase("들어가기"))
                  {
                    Player player;
                    
                    if (sender instanceof Player)
                      {
                        player = (Player) sender;
                      }
                    else
                      {
                        sender.sendMessage(ChatColor.RED
                            + "이 명령어는 게임 안에서만 사용 가능합니다");
                        return true;
                      }
                    
                    if (player.hasPermission("thewalls2.command.thewalls.join"))
                      {
                        if (gameList != null)
                          {
                            player.sendMessage(ChatColor.RED
                                + "현재 게임 중입니다!");
                            return true;
                          }
                        
                        if (TheWalls2World.isRestoring)
                          {
                            player.sendMessage(ChatColor.RED
                                + "현재 월드 복구 중입니다");
                            return true;
                          }
                        
                        if ( !queue.isInQueue(player.getName()))
                          {
                            
                            if (queue.getSize() < 12)
                              {
                                inventories.addInventory(player.getName(),
                                    player.getInventory());
                                inventories.clearInventory(player);
                                queue.addPlayer(player.getName(),
                                    player.getLocation());
                                Teleport.teleportPlayerToLocation(player,
                                    locData.getLobby());
                                player.sendMessage(ChatColor.GREEN
                                    + "게임 대기실에 입장 했습니다!");
                              }
                            else
                              {
                                player.sendMessage(ChatColor.RED
                                    + "게임 대기실이 꽉 찼습니다!");
                              }
                            return true;
                          }
                        player.sendMessage(ChatColor.RED
                            + "이미 게임 대기실에 있습니다!");
                        return true;
                      }
                    player.sendMessage(ChatColor.RED
                        + "당신은 해당 명령어를 사용하기 위한 권한이 없습니다!");
                    return true;
                  }
                else if (args[0].equalsIgnoreCase("나가기"))
                  {
                    Player player;
                    
                    if (sender instanceof Player)
                      {
                        player = (Player) sender;
                      }
                    else
                      {
                        sender.sendMessage(ChatColor.RED
                            + "이 명령어는 게임 안에서만 사용 가능합니다");
                        return true;
                      }
                    
                    if (player
                        .hasPermission("thewalls2.command.thewalls.leave"))
                      {
                        if (gameList != null)
                          {
                            player.sendMessage(ChatColor.RED
                                + "현재 게임 중입니다!");
                            return true;
                          }
                        String playerName = player.getName();
                        
                        if (queue.isInQueue(playerName))
                          {
                            queue.removePlayer(playerName, true);
                            if (teams.isInTeam(playerName))
                              {
                                teams.removePlayer(playerName);
                              }
                            player.getInventory().setContents(
                                inventories.getInventoryContents(playerName));
                            player.getInventory().setArmorContents(
                                inventories.getArmourContents(playerName));
                            player.sendMessage(ChatColor.GREEN
                                + "게임 대기실에서 나왔습니다!");
                            return true;
                          }
                        player.sendMessage(ChatColor.RED
                            + "게임 대기실에 입장하지 않았습니다!");
                        return true;
                      }
                    player.sendMessage(ChatColor.RED
                        + "당신은 해당 명령어를 사용하기 위한 권한이 없습니다!");
                    return true;
                  }
                else if (args[0].equalsIgnoreCase("팀"))
                  {
                    Player player;
                    
                    if (sender instanceof Player)
                      {
                        player = (Player) sender;
                      }
                    else
                      {
                        sender.sendMessage(ChatColor.RED
                            + "이 명령어는 게임 안에서만 사용 가능합니다");
                        return true;
                      }
                    
                    if (player.hasPermission("thewalls2.command.thewalls.team"))
                      {
                        sender.sendMessage(ChatColor.YELLOW
                            + "/월즈 팀 <1-4>" + ChatColor.WHITE
                            + " - 선택한 팀에 들어가기");
                        return true;
                      }
                    player.sendMessage(ChatColor.RED
                        + "당신은 해당 명령어를 사용하기 위한 권한이 없습니다!");
                    return true;
                  }
                else if (args[0].equalsIgnoreCase("시작"))
                  {
                    if (sender
                        .hasPermission("thewalls2.command.thewalls.start"))
                      {
                        if (gameList != null)
                          {
                            sender.sendMessage(ChatColor.RED
                                + "현재 게임 중입니다!");
                            return true;
                          }
                        
                        if (TheWalls2World.isRestoring)
                          {
                            sender.sendMessage(ChatColor.RED
                                + "현재 월드 복구 중입니다");
                            return true;
                          }
                        
                        if (startGame())
                          {
                            sender.sendMessage(ChatColor.GREEN
                                + "게임이 시작되었습니다!");
                          }
                        else
                          sender.sendMessage(ChatColor.RED
                              + "최소 2개의 팀이 존재해야 합니다!");
                        return true;
                      }
                    sender.sendMessage(ChatColor.RED
                        + "당신은 해당 명령어를 사용하기 위한 권한이 없습니다!");
                    return true;
                  }
                else if (args[0].equalsIgnoreCase("종료"))
                  {
                    if (sender.hasPermission("thewalls2.command.thewalls.stop"))
                      {
                        if (gameList == null)
                          {
                            sender.sendMessage(ChatColor.RED
                                + "진행 중인 게임이 없습니다!");
                            return true;
                          }
                        
                        if (TheWalls2World.isRestoring)
                          {
                            sender.sendMessage(ChatColor.RED
                                + "월드 복구 중입니다");
                            return true;
                          }
                        
                        teams.reset();
                        queue.reset(true);
                        gameList = null;
                        restoreBackup();
                        return true;
                      }
                    sender.sendMessage(ChatColor.RED
                        + "당신은 해당 명령어를 사용하기 위한 권한이 없습니다!");
                    return true;
                  }
                else if (args[0].equalsIgnoreCase("월드복구"))
                  {
                    if (sender
                        .hasPermission("thewalls2.command.thewalls.restoreWorld"))
                      {
                        sender.sendMessage(ChatColor.GREEN
                            + "월드 복구 중...");
                        restoreBackup();
                        return true;
                      }
                    sender.sendMessage(ChatColor.RED
                        + "당신은 해당 명령어를 사용하기 위한 권한이 없습니다!");
                    return true;
                  }
                else if (args[0].equalsIgnoreCase("디버그"))
                  {
                    System.out.println("대기실:");
                    for (String s : queue.getList())
                      {
                        System.out.println(s);
                      }
                    System.out.println("팀 1:");
                    for (String s : teams.getTeam(1))
                      {
                        System.out.println(s);
                      }
                    System.out.println("팀 2:");
                    for (String s : teams.getTeam(2))
                      {
                        System.out.println(s);
                      }
                    System.out.println("팀 3:");
                    for (String s : teams.getTeam(3))
                      {
                        System.out.println(s);
                      }
                    System.out.println("팀 4:");
                    for (String s : teams.getTeam(4))
                      {
                        System.out.println(s);
                      }
                    System.out.println("게임 목록:");
                    for (String s : gameList.getList())
                      {
                        System.out.println(s);
                      }
                    return true;
                  }
                return false;
              }
            else if (args.length == 2)
              {
                if (args[0].equalsIgnoreCase("팀"))
                  {
                    Player player;
                    
                    if (sender instanceof Player)
                      {
                        player = (Player) sender;
                      }
                    else
                      {
                        sender.sendMessage(ChatColor.RED
                            + "이 명령어는 게임 안에서만 사용 가능합니다");
                        return true;
                      }
                    
                    if (player.hasPermission("thewalls2.command.thewalls.team"))
                      {
                        if (queue.isInQueue(player.getName()))
                          {
                            int i;
                            
                            try
                              {
                                i = Integer.parseInt(args[1]);
                              }
                            catch (NumberFormatException e)
                              {
                                player.sendMessage(ChatColor.RED
                                    + "ERROR: 올바른 숫자를 입력하세요!");
                                return false;
                              }
                            if ( !teams.isInTeam(player.getName()))
                              {
                                if ( !teams.isTeamFull(i))
                                  {
                                    if (teams.addPlayerToTeam(i,
                                        player.getName()))
                                      {
                                        player.sendMessage(ChatColor.GREEN
                                            + "성공적으로 들어왔습니다 - 팀 "
                                            + String.valueOf(i) + "!");
                                        return true;
                                      }
                                    player.sendMessage(ChatColor.RED
                                        + "그런 이름의 팀은 없습니다!");
                                    return true;
                                  }
                                player.sendMessage(ChatColor.RED
                                    + "그런 이름의 팀이 존재하지 않거나 꽉 찼습니다!");
                                return true;
                              }
                            if ( !teams.isTeamFull(i))
                              {
                                teams.removePlayer(player.getName());
                                if (teams.addPlayerToTeam(i, player.getName()))
                                  {
                                    player.sendMessage(ChatColor.GREEN
                                        + "성공적으로 팀을 변경하였습니다 - 팀 "
                                        + String.valueOf(i) + "!");
                                    return true;
                                  }
                                player.sendMessage(ChatColor.RED
                                    + "그런 이름의 팀은 없습니다!");
                                return true;
                              }
                            player.sendMessage(ChatColor.RED
                                + "그런 이름의 팀이 존재하지 않거나 꽉 찼습니다!");
                            return true;
                          }
                        player.sendMessage(ChatColor.RED
                            + "당신은 게임 대기실에 있지 않습니다!");
                        return true;
                      }
                    player.sendMessage(ChatColor.RED
                        + "당신은 해당 명령어를 사용하기 위한 권한이 없습니다!");
                    return true;
                  }
                return false;
              }
            return false;
          }
        return false;
      }
    
    public boolean startGame ()
      {
        World world = getServer().getWorld(worldName);
        
        teams.cleanup();
        
        if (teams.getEmptyTeamCount() > 2)
          return false;
        
        gameList = new TheWalls2GameList(queue.getList());
        
        for (int t = 1; t < 5; t++)
          {
            teleportTeamToGame(t, world);
          }
        
        gameList.notifyAll("게임이 시작되었습니다!");
        
        if (getConfig().getBoolean("game.virtual"))
          {
            boolean notify = getConfig().getBoolean("game.notify");
            int notifyInterval = getConfig().getInt("game.notify-interval");
            int time = getConfig().getInt("game.time");
            gameList.notifyAll(String.valueOf(time) + " 분 남았습니다!");
            getServer().getScheduler().scheduleSyncDelayedTask(this,
                new Runnable()
                  {
                    public void run ()
                      {
                        for (int t = 1; t < 5; t++)
                          {
                            for (int s = 0; s < 3; s++)
                              {
                                Location loc = locData.getSlot(t, s);
                                int x = loc.getBlockX();
                                int y = loc.getBlockY() - 3;
                                int z = loc.getBlockZ();
                                Bukkit.getWorld(TheWalls2.worldName)
                                    .getBlockAt(x, y, z)
                                    .setType(Material.REDSTONE_TORCH_ON);
                              }
                          }
                      }
                  }, 20L);
            new Thread(new TheWalls2GameTimer(this, notify, notifyInterval,
                time)).start();
          }
        else
          {
            final Location loc = new Location(world, -781, 98, -58);
            
            loc.getBlock().setType(Material.REDSTONE_TORCH_ON);
            
            getServer().getScheduler().scheduleSyncDelayedTask(this,
                new Runnable()
                  {
                    
                    public void run ()
                      {
                        loc.getBlock().setType(Material.AIR);
                      }
                  }, 20L);
          }
        
        return true;
      }
    
    public void teleportTeamToGame (int teamNumber, World world)
      {
        int i = 0;
        Set<String> team = teams.getTeam(teamNumber);
        
        if (team.size() == 0)
          return;
        
        for (String s : team)
          {
            Player player = getServer().getPlayer(s);
            if (player == null)
              continue;
            Teleport.teleportPlayerToLocation(player,
                locData.getSlot(teamNumber, i));
            i++;
          }
      }
    
    public boolean checkIfGameIsOver ()
      {
        if (gameList.getPlayerCount() < 4)
          {
            final Set<String> playerNames = gameList.getList();
            int firstTeam = 0;
            List<Player> playerList = new ArrayList<Player>();
            List<String> playerNameList = new ArrayList<String>();
            for (String s : playerNames)
              {
                if (firstTeam == 0)
                  {
                    firstTeam = teams.getPlayerTeam(s);
                    playerList.add(getServer().getPlayer(s));
                    playerNameList.add(s);
                    continue;
                  }
                if (teams.getPlayerTeam(s) != firstTeam)
                  return false;
                playerList.add(getServer().getPlayer(s));
                playerNameList.add(s);
              }
            final List<String> finalPlayerList = playerNameList;
            
            getServer().broadcastMessage(
                ChatColor.YELLOW + "팀 " + String.valueOf(firstTeam)
                    + ChatColor.GREEN + " 이(가) The Walls 2 에서 승리하였습니다!");
            for (Player player : playerList)
              {
                player.sendMessage(ChatColor.GOLD + "추카합니다! "
                    + "당신은 The Walls 2 에서 승리하였습니다!");
              }
            
            getServer().getScheduler().scheduleSyncDelayedTask(this,
                new Runnable()
                  {
                    
                    public void run ()
                      {
                        for (String s : finalPlayerList)
                          {
                            Player futurePlayer = getServer().getPlayer(s);
                            if (futurePlayer != null)
                              {
                                gameList.removeFromGame(s);
                                queue.removePlayer(s, true);
                                futurePlayer.getInventory().setContents(
                                    inventories.getInventoryContents(s));
                                futurePlayer.getInventory().setArmorContents(
                                    inventories.getArmourContents(s));
                                TheWalls2Prize.givePrize(plugin, futurePlayer);
                              }
                          }
                        
                        teams.reset();
                        queue.reset(false);
                        gameList = null;
                        restoreBackup();
                      }
                  }, 100L);
            return true;
          }
        return false;
      }
    
    public void restoreBackup ()
      {
        TheWalls2World.reloadWorld(this);
      }
    
    public TheWalls2PlayerQueue getQueue ()
      {
        return queue;
      }
    
    public TheWalls2GameList getGameList ()
      {
        return gameList;
      }
    
    public TheWalls2GameTeams getGameTeams ()
      {
        return teams;
      }
    
    public TheWalls2RespawnQueue getRespawnQueue ()
      {
        return respawnQueue;
      }
    
    public TheWalls2LocationData getLocationData ()
      {
        return locData;
      }
    
    public TheWalls2Inventory getInventory ()
      {
        return inventories;
      }
    
    public TheWalls2World getWorld ()
      {
        return wallsWorld;
      }
    
    public AutoUpdater getAutoUpdater ()
      {
        return autoUpdater;
      }
    
    private boolean setupEconomy ()
      {
        RegisteredServiceProvider<Economy> economyProvider =
            getServer().getServicesManager().getRegistration(
                net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null)
          {
            economy = economyProvider.getProvider();
          }
        
        return (economy != null);
      }
    
    @Override
    public void onDisable ()
      {
        // Add-ons
        System.out.println("[TheWalls2] 애드온 로드 해제 중...");
        addonLoader.unloadAddons();
        
        System.out.println("[TheWalls2] 월드 로드 해제 중...");
        getServer().unloadWorld(worldName, false);
        System.out.println(this + " 비활성화됨!");
      }
    
    @Override
    public void onEnable ()
      {
        getConfig().options().copyDefaults(true);
        saveConfig();
        worldName = getConfig().getString("general.world");
        fallbackWorldName = getConfig().getString("general.fallback-world");
        queue = new TheWalls2PlayerQueue(this);
        teams = new TheWalls2GameTeams(queue);
        System.out.println("[TheWalls2] Loading world...");
        WorldCreator wc = new WorldCreator(worldName);
        World world = getServer().createWorld(wc);
        world.setAutoSave(false);
        locData = new TheWalls2LocationData(world);
        inventories = new TheWalls2Inventory();
        respawnQueue = new TheWalls2RespawnQueue(this);
        String lobby = getConfig().getString("locations.lobby");
        if (TheWalls2ConfigSetter.isLobbyDifferent(this, lobby))
          {
            TheWalls2ConfigSetter.updateLobbyLocation(this, lobby);
          }
        String prize = getConfig().getString("general.prize");
        if ( !prize.equals("item") && !prize.equals("money")
            && !prize.equals("none"))
          {
            System.out.println("[TheWalls2] ERROR: "
                + "general.prize 가 이상하게 설정되어 있습니다!");
            System.out.println("[TheWalls2] Falling back to item prize!");
            getConfig().set("general.prize", "item");
            saveConfig();
          }
        else if (getConfig().getString("general.prize").equals("money"))
          {
            if ( !setupEconomy())
              {
                System.out.println("[TheWalls2] ERROR: "
                    + "Vault가 어느 요인에 의해 비활성화되었습니다!");
                System.out.println("[TheWalls2] Falling back to item prize!");
                getConfig().set("general.prize", "item");
              }
          }
        System.out.println("[TheWalls2] Prize mode: "
            + getConfig().getString("general.prize"));
        playerListener = new TheWalls2PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);
        if ( !getConfig().getBoolean("general.monsters"))
          {
            entityListener = new TheWalls2EntityListener();
            getServer().getPluginManager().registerEvents(entityListener, this);
          }
        
        // Add-ons
        System.out.println("[TheWalls2] 애드온 로드 중...");
        addonLoader = new AddonLoader();
        addonLoader.loadAddons(new File("plugins/TheWalls2/addons"));
        
        if (getConfig().getBoolean("timer.enabled"))
          {
            long initialTime = getConfig().getLong("timer.initial-time");
            long normalTime = getConfig().getLong("timer.normal-time");
            new Thread(new TheWalls2AutoGameStartTimer(this, initialTime,
                normalTime)).start();
          }
        
        try
          {
            Metrics metrics = new Metrics(this);
            metrics.start();
          }
        catch (IOException e)
          {
          }
        
        String version = this.getDescription().getVersion();
        autoUpdater = new AutoUpdater(this, new Object(), version);
        new Thread(autoUpdater).start();
        
        System.out.println(this + " 활성화됨!");
      }
  }