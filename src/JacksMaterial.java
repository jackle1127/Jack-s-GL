
import java.awt.Dimension;
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
    float a = 1;
    float n = 1;
    float environmentReflection = 0;
    int specularExponent = 64;
    private int width = 0;
    private int height = 0;
    private int widthNormal = 0;
    private int heightNormal = 0;
    private int textureIndex;
    private float c0, c1, c2, c3, alphaX, alphaY;
    char[] texture;
    char[] normalMap;
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
        this.specularExponent = specularExponent;
        setTexture(texture);
    }

    void setTexture(BufferedImage texture) {
        width = texture.getWidth();
        height = texture.getHeight();
        loadTexture(texture, 0);
    }

    void setNormalMap(BufferedImage normalMap) {
        widthNormal = normalMap.getWidth();
        heightNormal = normalMap.getHeight();
        loadTexture(normalMap, 1);
    }

    private void loadTexture(BufferedImage image, int type) {
        BufferedImage temp = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        temp.getGraphics().drawImage(image, 0, 0, null);
        byte[] tempByteArray = ((DataBufferByte) temp.getData().getDataBuffer()).getData();
        char[] array = null;
        if (type == 0) {
            texture = new char[tempByteArray.length];
            array = texture;
        } else if (type == 1) {
            normalMap = new char[tempByteArray.length];
            array = normalMap;
        }
        for (int i = 0; i < tempByteArray.length; i++) {
            array[i] = byteToChar(tempByteArray[i]);
        }
    }

    float getB(float x, float y) {
        return (float) (texture[(rotateNumber((int) ((1 - y) * (height)), height) * width
                + rotateNumber((int) (x * (width)), width)) * 3]) / 255.0f;
    }

    float getBInterpolated(float x, float y) {
        c0 = getB(x, y);
        c1 = getB(x + 1f / width, y);
        c2 = getB(x + 1f / width, y + 1f / height);
        c3 = getB(x, y + 1f / height);
        alphaX = (x * width) % 1;
        alphaY = (y * height) % 1;
        return (1 - alphaY) * (alphaX * c1 + (1 - alphaX) * c0)
                + alphaY * (alphaX * c2 + (1 - alphaX) * c3);
    }

    float getG(float x, float y) {
        return (float) (texture[(rotateNumber((int) ((1 - y) * (height)), height) * width
                + rotateNumber((int) (x * (width)), width)) * 3 + 1]) / 255.0f;
    }

    float getGInterpolated(float x, float y) {
        c0 = getG(x, y);
        c1 = getG(x + 1f / width, y);
        c2 = getG(x + 1f / width, y + 1f / height);
        c3 = getG(x, y + 1f / height);
        alphaX = (x * width) % 1;
        alphaY = (y * height) % 1;
        return (1 - alphaY) * (alphaX * c1 + (1 - alphaX) * c0)
                + alphaY * (alphaX * c2 + (1 - alphaX) * c3);
    }

    float getR(float x, float y) {
        return (float) (texture[(rotateNumber((int) ((1 - y) * (height)), height) * width
                + rotateNumber((int) (x * (width)), width)) * 3 + 2]) / 255.0f;
    }

    float getRInterpolated(float x, float y) {
        c0 = getR(x, y);
        c1 = getR(x + 1f / width, y);
        c2 = getR(x + 1f / width, y + 1f / height);
        c3 = getR(x, y + 1f / height);
        alphaX = (x * width) % 1;
        alphaY = (y * height) % 1;
        return (1 - alphaY) * (alphaX * c1 + (1 - alphaX) * c0)
                + alphaY * (alphaX * c2 + (1 - alphaX) * c3);
    }

    int getRGB(float x, float y) {
        textureIndex = (rotateNumber((int) ((1 - y) * (height)), height) * width
                + rotateNumber((int) (x * (width)), width)) * 3;
        return (texture[textureIndex + 2] << 16) + (texture[textureIndex + 1] << 8) + 
                (texture[textureIndex]);
    }
    
    int getNormal(float x, float y) {
        textureIndex = (rotateNumber((int) ((1 - y) * (heightNormal)), heightNormal) * widthNormal
                + rotateNumber((int) (x * (widthNormal)), widthNormal)) * 3;
        return (normalMap[textureIndex + 2] << 16) + (normalMap[textureIndex + 1] << 8) + 
                (normalMap[textureIndex]);
    }
    
    float getNormalZ(float x, float y) {
        return (float) (normalMap[(rotateNumber((int) ((1 - y) * (heightNormal)), heightNormal) * widthNormal
                + rotateNumber((int) (x * (widthNormal)), widthNormal)) * 3]) / 255.0f;
    }

    float getNormalZInterpolated(float x, float y) {
        c0 = getNormalZ(x, y);
        c1 = getNormalZ(x + 1f / widthNormal, y);
        c2 = getNormalZ(x + 1f / widthNormal, y + 1f / heightNormal);
        c3 = getNormalZ(x, y + 1f / heightNormal);
        alphaX = (x * widthNormal) % 1;
        alphaY = (y * widthNormal) % 1;
        return (1 - alphaY) * (alphaX * c1 + (1 - alphaX) * c0)
                + alphaY * (alphaX * c2 + (1 - alphaX) * c3);
    }
    
    float getNormalY(float x, float y) {
        return (float) (normalMap[(rotateNumber((int) ((1 - y) * (heightNormal)), heightNormal) * widthNormal
                + rotateNumber((int) (x * (widthNormal)), widthNormal)) * 3 + 1]) / 255.0f;
    }

    float getNormalYInterpolated(float x, float y) {
        c0 = getNormalY(x, y);
        c1 = getNormalY(x + 1f / widthNormal, y);
        c2 = getNormalY(x + 1f / widthNormal, y + 1f / heightNormal);
        c3 = getNormalY(x, y + 1f / heightNormal);
        alphaX = (x * widthNormal) % 1;
        alphaY = (y * widthNormal) % 1;
        return (1 - alphaY) * (alphaX * c1 + (1 - alphaX) * c0)
                + alphaY * (alphaX * c2 + (1 - alphaX) * c3);
    }
    
    float getNormalX(float x, float y) {
        return (float) (normalMap[(rotateNumber((int) ((1 - y) * (heightNormal)), heightNormal) * widthNormal
                + rotateNumber((int) (x * (widthNormal)), widthNormal)) * 3 + 2]) / 255.0f;
    }

    float getNormalXInterpolated(float x, float y) {
        c0 = getNormalX(x, y);
        c1 = getNormalX(x + 1f / widthNormal, y);
        c2 = getNormalX(x + 1f / widthNormal, y + 1f / heightNormal);
        c3 = getNormalX(x, y + 1f / heightNormal);
        alphaX = (x * widthNormal) % 1;
        alphaY = (y * widthNormal) % 1;
        return (1 - alphaY) * (alphaX * c1 + (1 - alphaX) * c0)
                + alphaY * (alphaX * c2 + (1 - alphaX) * c3);
    }
    
    char byteToChar(byte number) {
        return number >= 0
                ? (char) number
                : (char) (number + 256);
    }

    private int rotateNumber(int x, int max) {
        x = x % max;
        if (x < 0) {
            x = max + x;
        }
        return x;
    }

    BufferedImage getTexture(Dimension size) {
        if (texture == null) {
            return null;
        }
        BufferedImage newImage = new BufferedImage(size.width, size.height,
                BufferedImage.TYPE_3BYTE_BGR);
        for (int y = 0; y < newImage.getHeight(); y++) {
            for (int x = 0; x < newImage.getWidth(); x++) {
                float newX = (float) x / size.width;
                float newY = (float) y / size.height;
                newImage.setRGB(x, y, ((int) (getR(newX, newY) * 255) << 16)
                        + ((int) (getG(newX, newY) * 255) << 8)
                        + (int) (getB(newX, newY) * 255));
            }
        }
        return newImage;
    }

    BufferedImage getNormalMap(Dimension size) {
        if (normalMap == null) {
            return null;
        }
        BufferedImage newImage = new BufferedImage(size.width, size.height,
                BufferedImage.TYPE_3BYTE_BGR);
        for (int y = 0; y < newImage.getHeight(); y++) {
            for (int x = 0; x < newImage.getWidth(); x++) {
                float newX = (float) x / size.width;
                float newY = (float) y / size.height;
                newImage.setRGB(x, y, ((int) (getNormalX(newX, newY) * 255) << 16)
                        + ((int) (getNormalY(newX, newY) * 255) << 8)
                        + (int) (getNormalZ(newX, newY) * 255));
            }
        }
        return newImage;
    }
}
