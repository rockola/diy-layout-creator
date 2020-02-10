package org.diylc.images;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class FontLoader {
  private FontLoader() {
    //
  }

  private static Map<String, Font> fonts = new HashMap<String, Font>();

  public static Font getFont(String name, int style, int size) {
    Font font = fonts.get(name);
    if (font == null) {
      try {
        InputStream is =
            FontLoader.class.getResourceAsStream(String.format("/fonts/%s.ttf", name));
        font = Font.createFont(Font.TRUETYPE_FONT, is);
        fonts.put(name, font);
      } catch (IOException | FontFormatException ex) {
        font = new Font("serif", style, size);
      }
    }
    return font.deriveFont(style, size);
  }
}
