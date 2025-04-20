package com.example.cubearia.game.inventory;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.example.cubearia.game.world.BlockType;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    private static final Map<String, Item> itemsById = new HashMap<>();

    public static void init(TextureAtlas blockAtlas) {
        for (BlockType block : BlockType.values()) {
            if (block == BlockType.AIR || block == BlockType.WATER)
                continue; // nicht als Items

            String id = block.name().toLowerCase(); // z.B. "stone"
            TextureRegion icon = blockAtlas.findRegion("tiles"); // dein TileSheet
            if (icon != null) {
                TextureRegion blockIcon = new TextureRegion(
                    icon,
                    block.getTileX() * 16, block.getTileY() * 16,
                    16, 16
                );

                Item item = new Item(id, block.name(), blockIcon, 64);
                register(item);
            }
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
