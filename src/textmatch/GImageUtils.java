package textmatch;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

public class GImageUtils {
    public static BufferedImage imageFromBase64Encoded(String base64txt) throws Exception {
        byte[] imageBytes = DatatypeConverter.parseBase64Binary(base64txt);
        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }
    
    public static String base64EncodeImage(BufferedImage img) throws Exception {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        ImageIO.write(img, "png", s);
        byte[] data = s.toByteArray();
        return DatatypeConverter.printBase64Binary(data);
    }
    
    public static BufferedImage transparentRectangle(Dimension imagedim, int sx, int sy, int w, int h) {
        BufferedImage img = new BufferedImage(imagedim.width, imagedim.height, BufferedImage.TYPE_INT_ARGB);
        for (int x = sx; x < sx + w; ++x) {
            for (int y = sy; y < sy + h; ++y) {
                img.setRGB(x, y, 0x5500C8C8);
            }
        }
        return img;
    }
    
    public static void main(String[] args) throws Exception {
        BufferedImage img = transparentRectangle(new Dimension(500, 700), 40, 80, 60, 100);
        ImageIO.write(img, "png", new File("output.png"));
    }
}
