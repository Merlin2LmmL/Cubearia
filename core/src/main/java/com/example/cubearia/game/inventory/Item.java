package com.example.cubearia.game.inventory;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Item {
    private final String id;
    private final String name;
    private final TextureRegion icon;
    private final int stackable;
    private final int type;
    private final int toolLevel;

    public Item(String id, String name, TextureRegion icon, int stackable, int type, int toolLevel) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.stackable = stackable;
        this.type = type;           // Types: 0=Block, 1=Crafting Item, 2=Tool/Weapon, 3=Armor
        this.toolLevel = toolLevel;
    }

    public static Item valueOf(String value) {
        if (value == null) return null;
        return ItemRegistry.get(value.toLowerCase());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TextureRegion getIcon() {
        return icon;
    }

    public int getMaxStackSize() {
        return stackable;
    }

    public boolean isStackable() {
        return stackable > 1;
    }

    public int getType() {
        return type;
    }

    public int getToolLevel() {
        return toolLevel;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Item)) return false;
        Item other = (Item) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
