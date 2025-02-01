package render.texture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class AssetManager {
    private static final HashMap<ResourceLocation, BufferedImage> textures = new HashMap<>();

    public static BufferedImage getImage(ResourceLocation resource, boolean cache) {
        if (textures.containsKey(resource))
            return textures.get(resource);
        try {
            InputStream inputStream = AssetManager.class.getResourceAsStream(resource.getPath("../assets/"));
            if (inputStream == null) {
                //When packaged as a jar file, the assets path cannot be accessed the same way
                inputStream = AssetManager.class.getResourceAsStream(resource.getPath("/"));
            }
            if (inputStream == null)
                throw new RuntimeException("Unable to load image with path " + resource.relativePath);
            BufferedImage image = ImageIO.read(inputStream);
            inputStream.close();
            if (image == null)
                throw new RuntimeException("Image was null with path: " + resource.relativePath);
            image = preLoad(image);
            if (cache)
                textures.put(resource, image);
            return image;
        } catch (IOException e) {
            throw new RuntimeException("Error opening image file with path " + resource.relativePath + " : " + e.getMessage());
        }
    }

    public static BufferedImage preLoad(Image inputImage) {
        BufferedImage decodedImage = new BufferedImage(
                inputImage.getWidth(null),
                inputImage.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = decodedImage.createGraphics();
        g.drawImage(inputImage, 0, 0, null);
        g.dispose();
        return decodedImage;
    }
}
