
import java.util.HashSet;

public class JacksLight extends JacksObject {

    public static final int TYPE_POINT = 0;
    public static final int TYPE_DIRECTIONAL = 1;
    int lightType;
    float energy = 3;
    int r = 255;
    int g = 255;
    int b = 255;
    private float tempX;
    private float tempY;
    private int id = -1;

    JacksVector direction = new JacksVector(0, -1, 0);
    private static HashSet<Integer> usedLightId = new HashSet<>();

    JacksLight(boolean getNewId) {
        if (getNewId) {
            int newId = findLightId();
            name = "Light " + newId;
            id = newId;
            usedLightId.add(newId);
        }
    }

    JacksLight(int lightType, boolean getNewId) {
        this.lightType = lightType;
        if (getNewId) {
            int newId = findLightId();
            name = "Light " + newId;
            id = newId;
            usedLightId.add(newId);
        }
    }

    JacksLight(int lightType, float energy, int r, int g, int b,
            float x, float y, float z, boolean getNewId) {
        this(getNewId);
        setAttribute(lightType, energy, r, g, b, x, y, z);
    }

    private int findLightId() {
        int result = 0;
        while (usedLightId.contains(result)) {
            result++;
        }
        return result;
    }

    void setAttribute(int lightType, float energy, int r, int g, int b,
            float x, float y, float z) {
        this.lightType = lightType;
        this.energy = energy;
        this.r = r;
        this.g = g;
        this.b = b;
        if (this.r > 255) {
            this.r = 255;
        }
        if (this.g > 255) {
            this.g = 255;
        }
        if (this.b > 255) {
            this.b = 255;
        }
        if (this.r < 0) {
            this.r = 0;
        }
        if (this.g < 0) {
            this.g = 0;
        }
        if (this.b < 0) {
            this.b = 0;
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }

    void copyAttribute(JacksLight other) {
        lightType = other.lightType;
        x = other.x;
        y = other.y;
        z = other.z;
        r = other.r;
        g = other.g;
        b = other.b;
        energy = other.energy;
        direction.copyXYZ(other.direction);
    }

    void tranform(JacksOrigin origin) {
        tempX = origin.translate.x
                + x * origin.x.x
                + y * origin.y.x
                + z * origin.z.x;
        tempY = origin.translate.y
                + x * origin.x.y
                + y * origin.y.y
                + z * origin.z.y;
        z = origin.translate.z
                + x * origin.x.z
                + y * origin.y.z
                + z * origin.z.z;
        x = tempX;
        y = tempY;
        direction.transform(origin);
    }

    @Override
    protected JacksLight clone() {
        JacksLight newLight =  new JacksLight(lightType, energy, r, g, b, x, y, z, false);
        newLight.rotateX = rotateX;
        newLight.rotateY = rotateY;
        newLight.rotateZ = rotateZ;
        newLight.scaleX = scaleX;
        newLight.scaleY = scaleY;
        newLight.scaleZ = scaleZ;
        return newLight;
    }

    void destroy() {
        if (id >= 0) {
            usedLightId.remove((Integer) id);
        }
    }
}
