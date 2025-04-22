package com.example.cubearia.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.graphics.Texture;

public class TilesetLoader {
    private final TextureRegion[] tileRegions;
    private final Texture tilesetTexture;

    public TilesetLoader(String tsxPath) {
        TextureRegion[] regionsTemp = null;
        Texture tex = null;

        try {
            XmlReader reader = new XmlReader();
            XmlReader.Element root = reader.parse(Gdx.files.internal(tsxPath));

            int tileWidth = root.getIntAttribute("tilewidth");
            int tileHeight = root.getIntAttribute("tileheight");
            int columns = root.getIntAttribute("columns");
            int tileCount = root.getIntAttribute("tilecount");

            String imagePath = root.getChildByName("image").getAttribute("source");
            tex = new Texture(Gdx.files.internal(imagePath));

            regionsTemp = new TextureRegion[tileCount];

            for (int id = 0; id < tileCount; id++) {
                int x = (id % columns) * tileWidth;
                int y = (id / columns) * tileHeight;

                regionsTemp[id] = new TextureRegion(tex, x, y, tileWidth, tileHeight);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.tileRegions = regionsTemp;
        this.tilesetTexture = tex;
    }

    public TextureRegion getRegionById(int id) {
        if (id < 0 || id >= tileRegions.length) return null;
        return tileRegions[id];
    }

    public void dispose() {
        if (tilesetTexture != null) tilesetTexture.dispose();
    }
}
