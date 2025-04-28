package com.example.cubearia.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.example.cubearia.game.inventory.Item;
import com.example.cubearia.game.inventory.ItemID;
import com.example.cubearia.game.inventory.ItemRegistry;
import com.example.cubearia.game.world.BlockType;

public class Assets {
    public static TilesetLoader  tileset;
    public static TilesetLoader  itemsTileset;
    public static Texture        playerTexture;
    public static Texture        slotBackground;
    public static Texture        inventoryBackground;
    public static Texture        craftingTableBackground;
    public static Texture        blackPixel;
    public static Texture        whitePixel;

    public static TextureRegion[] breakingFrames;
    public static Animation<TextureRegion> breakingAnimation;

    public static void load() {
        // 1) Lade das Tileset
        tileset = new TilesetLoader("Blocks.tsx");
        itemsTileset = new TilesetLoader("Items.tsx");
        for (BlockType block : BlockType.values()) {
            block.resolve(tileset);
        }
        for (ItemID itemID : ItemID.values()) {
            TextureRegion icon = Assets.itemsTileset.getRegionById(itemID.getId() - 1);
            Item item = new Item(
                itemID.getName(),
                itemID.getName(),
                icon,
                itemID.getMaxStackSize(),
                itemID.getType(),
                itemID.getToolLevel()
            );
            ItemRegistry.register(item);
        }

        // 2) Generiere den Black Pixel
        Pixmap pix1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix1.setColor(Color.BLACK);
        pix1.fill();
        blackPixel = new Texture(pix1);
        pix1.dispose();

        Pixmap pix2 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix2.setColor(Color.WHITE);
        pix2.fill();
        whitePixel = new Texture(pix2);
        pix2.dispose();

        // 3) Lade andere Assets
        playerTexture = new Texture(Gdx.files.internal("Player.png"));
        slotBackground = new Texture(Gdx.files.internal("ui/SlotBackground.png"));
        inventoryBackground = new Texture(Gdx.files.internal("ui/InventoryBackground.png"));
        craftingTableBackground = new Texture(Gdx.files.internal("ui/CraftingTableBackground.png"));

        // 4) Lade die Animation für den Breaking-Effekt
        Texture breakingSheet = new Texture(Gdx.files.internal("BreakingAnimation.png"));
        breakingFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            breakingFrames[i] = new TextureRegion(breakingSheet, i * 16, 0, 16, 16);
        }
        breakingAnimation = new Animation<>(0.1f, breakingFrames);
    }

    public static void dispose() {
        // Entsorge alle Assets
        playerTexture.dispose();
        slotBackground.dispose();
        inventoryBackground.dispose();
        craftingTableBackground.dispose();
        // Entsorge das Breaking-Animation-Sheet
        breakingAnimation.getKeyFrames()[0].getTexture().dispose();  // Entsorge die Textur der Animation
    }
}
