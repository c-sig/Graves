package com.ranull.graves;

import com.ranull.graves.commands.GravesCommand;
import com.ranull.graves.hooks.VaultHook;
import com.ranull.graves.listeners.Events;
import com.ranull.graves.manager.*;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Graves extends JavaPlugin {
    GraveManager graveManager;
    GUIManager guiManager;
    RecipeManager recipeManager;
    BooleanFlag graveSpawns;

    @Override
    public void onLoad() {
        // TODO softdepend on WorldGuard
        registerWorldGuardFlags();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        VaultHook vaultHook = new VaultHook();
        if (!vaultHook.setupEconomy()) {
            vaultHook = null;
        }

        MessageManager messageManager = new MessageManager(this);
        DataManager data = new DataManager(this);
        graveManager = new GraveManager(this, data, messageManager);

        recipeManager = new RecipeManager(this, graveManager);
        recipeManager.loadRecipes();

        guiManager = new GUIManager(this, graveManager, vaultHook);

        Objects.requireNonNull(getCommand("graves")).
                setExecutor(new GravesCommand(this, data, graveManager, guiManager, recipeManager, messageManager));

        getServer().getPluginManager().registerEvents(new Events(this, graveManager, guiManager, messageManager), this);
    }

    @Override
    public void onDisable() {
        graveManager.removeHolograms();
        graveManager.closeGraves();
        graveManager.saveGraves();

        recipeManager.unloadRecipes();
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    private void registerWorldGuardFlags() {
        var registry = WorldGuard.getInstance().getFlagRegistry();
        var flag = registry.get("grave-spawns");
        if (flag instanceof BooleanFlag booleanFlag)
            graveSpawns = booleanFlag;
        else try {
            var booleanFlag = new BooleanFlag("grave-spawns");
            registry.register(booleanFlag);
            graveSpawns = booleanFlag;
        } catch (FlagConflictException e) {
            e.printStackTrace();
        }
    }

    public BooleanFlag getGraveSpawns() {
        return graveSpawns;
    }
}
