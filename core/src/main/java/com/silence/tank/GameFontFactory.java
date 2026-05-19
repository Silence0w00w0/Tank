package com.silence.tank;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class GameFontFactory {
    private static final String[] FONT_CANDIDATES = {
            "NotoSansSC-VF.ttf",
            "SourceHanSansCN-Regular.otf",
            "msyh.ttc",
            "Deng.ttf",
            "simhei.ttf",
            "simsun.ttc"
    };

    private GameFontFactory() {
    }

    public static BitmapFont createFont() {
        for (File fontFile : fontFiles()) {
            try {
                return createFont(fontFile);
            } catch (RuntimeException ignored) {
                // Try the next installed font before falling back to LibGDX's default bitmap font.
            }
        }
        return new BitmapFont();
    }

    private static BitmapFont createFont(File fontFile) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.absolute(fontFile.getAbsolutePath()));
        try {
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 20;
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + Texts.FONT_CHARS;
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            return generator.generateFont(parameter);
        } finally {
            generator.dispose();
        }
    }

    private static List<File> fontFiles() {
        String windowsDir = System.getenv("WINDIR");
        if (windowsDir == null || windowsDir.isBlank()) {
            windowsDir = "C:\\Windows";
        }
        File fontDir = new File(windowsDir, "Fonts");
        List<File> files = new ArrayList<>();
        for (String candidate : FONT_CANDIDATES) {
            File file = new File(fontDir, candidate);
            if (file.isFile()) {
                files.add(file);
            }
        }
        return files;
    }
}
