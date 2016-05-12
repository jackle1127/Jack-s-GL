
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
    private int width = 0;
    private int height = 0;
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
        width = texture.getWidth();
        height = texture.getHeight();
        this.texture = ((DataBufferByte) temp.getData().getDataBuffer()).getData();
        this.specular = specular;
        this.specularExponent = specularExponent;
    }
    
    void setTexture(BufferedImage texture) {
        BufferedImage temp = new BufferedImage(texture.getWidth(),
                texture.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        temp.getGraphics().drawImage(texture, 0, 0, null);
        width = texture.getWidth();
        height = texture.getHeight();
        this.texture = ((DataBufferByte) temp.getData().getDataBuffer()).getData();
    }
    
    float getB(float x, float y) {
//        System.out.println(x + ", " + y);
        return byteToFloat(texture[(rotateNumber((int)((1 - y) * (height)), height) * width
                + rotateNumber((int)(x * (width)), width)) * 3]) / 255.0f;
    }
    
    float getG(float x, float y) {
        return byteToFloat(texture[(rotateNumber((int)((1 - y) * (height)), height) * width
                + rotateNumber((int)(x * (width)), width)) * 3 + 1]) / 255.0f;
    }
    
    float getR(float x, float y) {
        return byteToFloat(texture[(rotateNumber((int)((1 - y) * (height)), height) * width
                + rotateNumber((int)(x * (width)), width)) * 3 + 2]) / 255.0f;
    }
    
    float byteToFloat(byte number) {
        return number >= 0
                ? number
                : number + 256;
    }
    private int rotateNumber(int x, int max) {
        x = x % max;
        if (x < 0) {
            x = max + x;
        }
        return x;
    }
}
