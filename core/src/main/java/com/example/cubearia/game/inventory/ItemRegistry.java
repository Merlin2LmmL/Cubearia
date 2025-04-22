package com.example.cubearia.game.inventory;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.cubearia.game.world.BlockType;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    private static final Map<String, Item> itemsById = new HashMap<>();

    public static void init() {
        for (BlockType block : BlockType.values()) {
            // Blöcke überspringen, die nicht im Inventar erscheinen sollen
            if (block == BlockType.AIR || block == BlockType.WATER || block.getRegion() == null) {
                continue;
            }

            String id = block.name().toLowerCase(); // z.B. "stone"
            TextureRegion icon = block.getRegion();

            Item item = new Item(id, block.name(), icon, 64); // maxStackSize = 64
            register(item);
        }
    }

    public static void register(Item item) {
        itemsById.put(item.getId(), item);
    }

    public static Item get(String id) {
        return itemsById.get(id);
    }

    public static boolean isRegistered(String id) {
        return itemsById.containsKey(id);
    }

    public static Iterable<Item> getAllItems() {
        return itemsById.values();
    }
}
