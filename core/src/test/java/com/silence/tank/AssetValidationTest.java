package com.silence.tank;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetValidationTest {
    @Test
    void bundledLevelsParseSuccessfully() {
        for (int i = 1; i <= 3; i++) {
            LevelDefinition level = LevelLoader.load(new FileHandle(new File(assetsDir(), "levels/level" + i + ".json")));
            assertEquals(GameConfig.MAP_WIDTH, level.width());
            assertEquals(GameConfig.MAP_HEIGHT, level.height());
            assertTrue(!level.waves().isEmpty());
            assertTrue(!level.powerUps().isEmpty());
        }
    }

    @Test
    void atlasContainsAllReferencedRegions() {
        TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(
                new FileHandle(new File(assetsDir(), "tank.atlas")),
                new FileHandle(assetsDir()),
                false
        );
        Set<String> regions = new HashSet<>();
        data.getRegions().forEach(region -> regions.add(region.name));

        assertTrue(regions.containsAll(Set.of(
                AssetKeys.PLAYER,
                AssetKeys.ENEMY_BASIC,
                AssetKeys.ENEMY_FAST,
                AssetKeys.ENEMY_ARMORED,
                AssetKeys.ENEMY_POWER,
                AssetKeys.TILE_BRICK,
                AssetKeys.TILE_STEEL,
                AssetKeys.TILE_WATER,
                AssetKeys.TILE_GRASS,
                AssetKeys.TILE_ICE,
                AssetKeys.BASE,
                AssetKeys.POWER_SHIELD,
                AssetKeys.POWER_SPEED,
                AssetKeys.POWER_SHOT,
                AssetKeys.POWER_BOMB,
                AssetKeys.POWER_LIFE,
                AssetKeys.BULLET_PLAYER,
                AssetKeys.BULLET_ENEMY,
                AssetKeys.EXPLOSION_1,
                AssetKeys.EXPLOSION_2,
                AssetKeys.EXPLOSION_3
        )));
    }

    private static File assetsDir() {
        File rootAssets = new File("assets");
        if (rootAssets.exists()) {
            return rootAssets;
        }
        return new File("../assets");
    }
}
