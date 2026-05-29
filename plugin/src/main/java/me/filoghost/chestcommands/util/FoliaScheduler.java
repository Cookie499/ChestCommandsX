/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.util;

import me.filoghost.chestcommands.ChestCommands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class FoliaScheduler {

    private static final boolean FOLIA = isClassPresent("io.papermc.paper.threadedregions.RegionizedServer");
    private static final Runnable RETIRED_NOOP = () -> {};

    private FoliaScheduler() {}

    public static boolean isFolia() {
        return FOLIA;
    }

    public static void runGlobal(Runnable runnable) {
        if (FOLIA) {
            invokeGlobalScheduler("run", new Class<?>[] {Plugin.class, Consumer.class}, schedulerTask(runnable));
        } else {
            Bukkit.getScheduler().runTask(ChestCommands.getInstance(), runnable);
        }
    }

    public static void runGlobalLater(Runnable runnable, long delayTicks) {
        if (FOLIA) {
            invokeGlobalScheduler("runDelayed", new Class<?>[] {Plugin.class, Consumer.class, long.class}, schedulerTask(runnable), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(ChestCommands.getInstance(), runnable, delayTicks);
        }
    }

    public static void runGlobalTimer(Runnable runnable, long delayTicks, long periodTicks) {
        if (FOLIA) {
            invokeGlobalScheduler("runAtFixedRate", new Class<?>[] {Plugin.class, Consumer.class, long.class, long.class},
                    schedulerTask(runnable), delayTicks, periodTicks);
        } else {
            Bukkit.getScheduler().runTaskTimer(ChestCommands.getInstance(), runnable, delayTicks, periodTicks);
        }
    }

    public static void runAtPlayer(Player player, Runnable runnable) {
        if (FOLIA) {
            invokeEntityScheduler(player, "run", new Class<?>[] {Plugin.class, Consumer.class, Runnable.class}, schedulerTask(runnable), RETIRED_NOOP);
        } else {
            Bukkit.getScheduler().runTask(ChestCommands.getInstance(), runnable);
        }
    }

    public static void runAtPlayerLater(Player player, Runnable runnable, long delayTicks) {
        if (FOLIA) {
            invokeEntityScheduler(player, "runDelayed", new Class<?>[] {Plugin.class, Consumer.class, Runnable.class, long.class},
                    schedulerTask(runnable), RETIRED_NOOP, delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(ChestCommands.getInstance(), runnable, delayTicks);
        }
    }

    public static void cancelTasks() {
        if (FOLIA) {
            invokeGlobalScheduler("cancelTasks", new Class<?>[] {Plugin.class});
        } else {
            Bukkit.getScheduler().cancelTasks(ChestCommands.getInstance());
        }
    }

    @SuppressWarnings("unchecked")
    private static Consumer<Object> schedulerTask(Runnable runnable) {
        return scheduledTask -> runnable.run();
    }

    private static void invokeGlobalScheduler(String methodName, Class<?>[] parameterTypes, Object... arguments) {
        try {
            Object scheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            invokeSchedulerMethod("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler", scheduler, methodName, parameterTypes, arguments);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not use Folia global scheduler.", unwrap(e));
        }
    }

    private static void invokeEntityScheduler(Player player, String methodName, Class<?>[] parameterTypes, Object... arguments) {
        try {
            Object scheduler = player.getClass().getMethod("getScheduler").invoke(player);
            invokeSchedulerMethod("io.papermc.paper.threadedregions.scheduler.EntityScheduler", scheduler, methodName, parameterTypes, arguments);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not use Folia entity scheduler.", unwrap(e));
        }
    }

    private static void invokeSchedulerMethod(String schedulerClassName, Object scheduler, String methodName,
                                              Class<?>[] parameterTypes, Object... arguments) throws ReflectiveOperationException {
        Object[] fullArguments = new Object[arguments.length + 1];
        fullArguments[0] = ChestCommands.getInstance();
        System.arraycopy(arguments, 0, fullArguments, 1, arguments.length);

        Method method = Class.forName(schedulerClassName).getMethod(methodName, parameterTypes);
        method.invoke(scheduler, fullArguments);
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Throwable unwrap(ReflectiveOperationException e) {
        if (e instanceof InvocationTargetException && e.getCause() != null) {
            return e.getCause();
        }
        return e;
    }
}
