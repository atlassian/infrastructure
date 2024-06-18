import java.awt.*;

public class FontTest {
    public static void main(String[] args) {
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        fonts[0].canDisplay('a');
    }
}
