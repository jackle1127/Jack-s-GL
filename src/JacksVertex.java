import java.util.HashSet;

public class JacksVertex {
    float x, y, z;
    private float tempX;
    private float tempY;
    JacksVector normal = new JacksVector();
    
    JacksVertex() {
        x = 0;
        y = 0;
        z = 0;
    }
    
    JacksVertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    void setXYZ(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    void project(JacksOrigin origin) {
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
        normal.project(origin);
    }
    
    protected JacksVertex clone() {
        JacksVertex result = new JacksVertex(x, y, z);
        result.normal = normal.clone();
        return result;
    }
    
    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
