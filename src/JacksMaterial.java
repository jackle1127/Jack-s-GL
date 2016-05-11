
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class JacksMaterial {
    float r = 1;
    float g = 1;
    float b = 1;
    float rS = 1;
    float gS = 1;
    float bS = 1;
    float rA = 0;
    float gA = 0;
    float bA = 0;
    float specular = 1;
    int specularExponent = 3;
    byte[] texture;
    String name = "unnamed";

    public JacksMaterial() {
    }

    public JacksMaterial(int r, int g, int b, int rS, int gS, int bS,
            float specular, int specularExponent) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.rS = rS;
        this.gS = gS;
        this.bS = bS;
        this.specular = specular;
        this.specularExponent = specularExponent;
    }
    
    public JacksMaterial(String name) {
        this.name = name;
    }
    
    public JacksMaterial(BufferedImage texture,
            float specular, int specularExponent) {
        BufferedImage temp = new BufferedImage(texture.getWidth(),
                texture.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        temp.getGraphics().drawImage(texture, 0, 0, null);
        this.texture = ((DataBufferByte) temp.getData().getDataBuffer()).getData();
        this.specular = specular;
        this.specularExponent = specularExponent;
    }
    
    void setTexture(BufferedImage texture) {
        BufferedImage temp = new BufferedImage(texture.getWidth(),
                texture.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        temp.getGraphics().drawImage(texture, 0, 0, null);
        this.texture = ((DataBufferByte) temp.getData().getDataBuffer()).getData();
    }
}
