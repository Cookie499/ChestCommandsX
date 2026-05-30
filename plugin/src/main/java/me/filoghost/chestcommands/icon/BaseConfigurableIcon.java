/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands.icon;

import me.filoghost.chestcommands.api.Icon;
import me.filoghost.chestcommands.parsing.NumberParser;
import me.filoghost.chestcommands.parsing.ParseException;
import me.filoghost.chestcommands.placeholder.PlaceholderManager;
import me.filoghost.chestcommands.placeholder.PlaceholderString;
import me.filoghost.chestcommands.placeholder.PlaceholderStringList;
import me.filoghost.chestcommands.util.Text;
import me.filoghost.fcommons.Preconditions;
import me.filoghost.fcommons.collection.CollectionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseConfigurableIcon implements Icon {

    private Material material;
    private int amount;
    private PlaceholderString amountExpression;
    private Integer damage;

    private PlaceholderString name;
    private PlaceholderStringList lore;
    private Map<Enchantment, Integer> enchantments;
    private Color leatherColor;
    private PlaceholderString skullOwner;
    private DyeColor bannerColor;
    private List<Pattern> bannerPatterns;
    private Integer customModelData;
    private Boolean unbreakable;
    private Set<ItemFlag> itemFlags;
    private boolean placeholdersEnabled;

    private ItemStack cachedRendering; // Cache the rendered item when possible and if state hasn't changed

    public BaseConfigurableIcon(Material material) {
        this.material = material;
        this.amount = 1;
    }

    protected boolean shouldCacheRendering() {
        if (amountExpression != null && amountExpression.hasDynamicPlaceholders()) {
            return false;
        }
        if (placeholdersEnabled && hasDynamicPlaceholders()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean hasDynamicPlaceholders() {
        return (name != null && name.hasDynamicPlaceholders())
                || (lore != null && lore.hasDynamicPlaceholders())
                || (skullOwner != null && skullOwner.hasDynamicPlaceholders());
    }

    public void setMaterial(@NotNull Material material) {
        this.material = material;
        cachedRendering = null;
    }

    public @NotNull Material getMaterial() {
        return material;
    }

    public void setAmount(int amount) {
        Preconditions.checkArgument(amount > 0, "amount must be greater than 0");
        this.amount = Math.min(amount, 127);
        this.amountExpression = null;
        cachedRendering = null;
    }

    public void setAmountExpression(@Nullable PlaceholderString amountExpression) {
        this.amountExpression = amountExpression;
        cachedRendering = null;
    }

    public int getAmount() {
        return amount;
    }

    @Deprecated
    public void setDurability(short durability) {
        setDamage(durability);
    }

    @Deprecated
    public short getDurability() {
        return damage != null ? damage.shortValue() : 0;
    }

    public void setDamage(int damage) {
        Preconditions.checkArgument(damage >= 0, "damage must be 0 or greater");
        this.damage = damage;
        cachedRendering = null;
    }

    public int getDamage() {
        return damage != null ? damage : 0;
    }

    @Deprecated
    public void setNBTData(@Nullable String nbtData) {
        throw new UnsupportedOperationException("NBT-DATA is no longer supported on Minecraft 1.21");
    }

    @Deprecated
    public @Nullable String getNBTData() {
        return null;
    }

    public void setName(@Nullable String name) {
        this.name = PlaceholderString.of(name);
        cachedRendering = null;
    }

    public @Nullable String getName() {
        if (name != null) {
            return name.getOriginalValue();
        } else {
            return null;
        }
    }

    public void setLore(@Nullable String... lore) {
        setLore(lore != null ? Arrays.asList(lore) : null);
    }

    public void setLore(@Nullable List<String> lore) {
        if (lore != null) {
            this.lore = new PlaceholderStringList(CollectionUtils.toArrayList(lore, element -> {
                return element != null ? element : "";
            }));
        } else {
            this.lore = null;
        }
        cachedRendering = null;
    }

    public @Nullable List<String> getLore() {
        if (lore != null) {
            return new ArrayList<>(lore.getOriginalValue());
        } else {
            return null;
        }
    }

    public void setEnchantments(@Nullable Map<Enchantment, Integer> enchantments) {
        this.enchantments = CollectionUtils.newHashMap(enchantments);
        cachedRendering = null;
    }

    public @Nullable Map<Enchantment, Integer> getEnchantments() {
        return CollectionUtils.newHashMap(enchantments);
    }

    public void addEnchantment(@NotNull Enchantment enchantment) {
        addEnchantment(enchantment, 1);
    }

    public void addEnchantment(@NotNull Enchantment enchantment, int level) {
        if (enchantments == null) {
            enchantments = new HashMap<>();
        }
        enchantments.put(enchantment, level);
        cachedRendering = null;
    }

    public void removeEnchantment(@NotNull Enchantment enchantment) {
        if (enchantments == null) {
            return;
        }
        enchantments.remove(enchantment);
        cachedRendering = null;
    }

    public @Nullable Color getLeatherColor() {
        return leatherColor;
    }

    public void setLeatherColor(@Nullable Color leatherColor) {
        this.leatherColor = leatherColor;
        cachedRendering = null;
    }

    public @Nullable String getSkullOwner() {
        if (skullOwner != null) {
            return skullOwner.getOriginalValue();
        } else {
            return null;
        }
    }

    public void setSkullOwner(@Nullable String skullOwner) {
        this.skullOwner = PlaceholderString.of(skullOwner);
        cachedRendering = null;
    }

    public @Nullable DyeColor getBannerColor() {
        return bannerColor;
    }

    public void setBannerColor(@Nullable DyeColor bannerColor) {
        this.bannerColor = bannerColor;
        cachedRendering = null;
    }

    public @Nullable List<Pattern> getBannerPatterns() {
        return CollectionUtils.newArrayList(bannerPatterns);
    }

    public void setBannerPatterns(@Nullable Pattern... bannerPatterns) {
        setBannerPatterns(bannerPatterns != null ? Arrays.asList(bannerPatterns) : null);
    }

    public void setBannerPatterns(@Nullable List<Pattern> bannerPatterns) {
        this.bannerPatterns = CollectionUtils.newArrayList(bannerPatterns);
        cachedRendering = null;
    }

    public void setCustomModelData(@Nullable Integer customModelData) {
        this.customModelData = customModelData;
        cachedRendering = null;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        cachedRendering = null;
    }

    public void setItemFlags(@Nullable List<ItemFlag> itemFlags) {
        this.itemFlags = itemFlags != null ? new LinkedHashSet<>(itemFlags) : null;
        cachedRendering = null;
    }

    public boolean isPlaceholdersEnabled() {
        return placeholdersEnabled;
    }

    public void setPlaceholdersEnabled(boolean placeholdersEnabled) {
        this.placeholdersEnabled = placeholdersEnabled;
        cachedRendering = null;
    }

    public @Nullable String renderName(Player viewer) {
        if (name == null) {
            return null;
        }
        if (!placeholdersEnabled) {
            return name.getOriginalValue();
        }

        String name = this.name.getValue(viewer);

        if (name.isEmpty()) {
            // Add a color to display the name empty
            return ChatColor.WHITE.toString();
        } else {
            return name;
        }
    }

    public @Nullable List<String> renderLore(Player viewer) {
        if (lore == null) {
            return null;
        }
        if (!placeholdersEnabled) {
            return lore.getOriginalValue();
        }

        return lore.getValue(viewer);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemStack render(@NotNull Player viewer) {
        if (shouldCacheRendering() && cachedRendering != null) {
            // Performance: return a cached item
            return cachedRendering;
        }

        ItemStack itemStack = new ItemStack(material, renderAmount(viewer));

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            if (renderName(viewer) != null) {
                itemMeta.displayName(Text.component(renderName(viewer)));
            }
            if (renderLore(viewer) != null) {
                itemMeta.lore(Text.components(renderLore(viewer)));
            }

            if (leatherColor != null && itemMeta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) itemMeta).setColor(leatherColor);
            }

            if (damage != null && itemMeta instanceof Damageable) {
                ((Damageable) itemMeta).setDamage(damage);
            }

            if (customModelData != null) {
                itemMeta.setCustomModelData(customModelData);
            }

            if (unbreakable != null) {
                itemMeta.setUnbreakable(unbreakable);
            }

            if (skullOwner != null && itemMeta instanceof SkullMeta) {
                String skullOwner = this.skullOwner.getValue(viewer);
                ((SkullMeta) itemMeta).setOwner(skullOwner);
            }

            if (itemMeta instanceof BannerMeta) {
                if (bannerPatterns != null) {
                    ((BannerMeta) itemMeta).setPatterns(bannerPatterns);
                }
            }

            // Hide all text details (damage, enchantments, potions, etc,)
            if (itemFlags != null && !itemFlags.isEmpty()) {
                itemMeta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
            }

            itemStack.setItemMeta(itemMeta);
        }

        if (enchantments != null) {
            enchantments.forEach(itemStack::addUnsafeEnchantment);
        }


        if (shouldCacheRendering()) {
            cachedRendering = itemStack;
        }

        return itemStack;
    }

    private int renderAmount(Player viewer) {
        if (amountExpression == null) {
            return amount;
        }

        try {
            String renderedExpression = PlaceholderManager.replaceDynamicPlaceholders(amountExpression.getOriginalValue(), viewer, 127);
            int renderedAmount = NumberParser.getStrictlyPositiveInteger(renderedExpression);
            return Math.min(renderedAmount, 127);
        } catch (ParseException e) {
            return amount;
        }
    }

}
