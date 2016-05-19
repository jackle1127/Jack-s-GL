public class JacksVector {
    float x, y, z;
    private float tempX;
    private float tempY;
    private float tempZ;
    private float sinAngle;
    private float cosAngle;
    JacksVector() {
        x = 0;
        y = 0;
        z = 0;
    }

    JacksVector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    JacksVector(JacksVertex v1, JacksVertex v2) {
        this(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
    }
    
    void setXYZ(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    void setXYZ(JacksVertex v1, JacksVertex v2) {
        setXYZ(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
    }

    void normalize() {
        float length = length();
        x /= length;
        y /= length;
        z /= length;
    }

    JacksVector add(JacksVector ... others) {
        for (JacksVector other : others) {
            x += other.x;
            y += other.y;
            z += other.z;
        }
        return this;
    }
    
    void flip() {
        x = -x;
        y = -y;
        z = -z;
    }
    
    void copyXYZ(JacksVector vector) {
        setXYZ(vector.x, vector.y, vector.z);
    }
    
    void transform(JacksOrigin origin) {
        tempX = x * origin.x.x
                + y * origin.y.x
                + z * origin.z.x;
        tempY = x * origin.x.y
                + y * origin.y.y
                + z * origin.z.y;
        z = x * origin.x.z
                + y * origin.y.z
                + z * origin.z.z;
        x = tempX;
        y = tempY;
    }
    
    JacksVector multiply(float alpha) {
        x *= alpha;
        y *= alpha;
        z *= alpha;
        return this;
    }

    JacksVector crossProduct(JacksVector other) {
        tempX = this.y * other.z - this.z * other.y;
        tempY = this.z * other.x - this.x * other.z;
        z = this.x * other.y - this.y * other.x;
        x = tempX;
        y = tempY;
        return this;
    }
    
    float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    float dotProduct(JacksVector other) {
        return (this.x * other.x + this.y * other.y + this.z * other.z);
    }
    
    JacksVector rotateX(float angle) {
        cosAngle = (float) Math.cos(angle);
        sinAngle = (float) Math.sin(angle);
        tempY = y * cosAngle - z * sinAngle;
        z = y * sinAngle + z * cosAngle;
        y = tempY;
        return this;
    }
    
    JacksVector rotateY(float angle) {
        cosAngle = (float) Math.cos(angle);
        sinAngle = (float) Math.sin(angle);
        tempZ = z * cosAngle - x * sinAngle;
        x = z * sinAngle + x * cosAngle;
        z = tempZ;
        return this;
    }
    
    JacksVector rotateZ(float angle) {
        cosAngle = (float) Math.cos(angle);
        sinAngle = (float) Math.sin(angle);
        tempX = x * cosAngle - y * sinAngle;
        y = x * sinAngle + y * cosAngle;
        x = tempX;
        return this;
    }

    protected JacksVector clone() {
        return new JacksVector(x, y, z);
    }

    static JacksVector addNewVector(JacksVector v1, JacksVector ... others) {
        JacksVector result = v1.clone();
        for (JacksVector other : others) {
            result.x += other.x;
            result.y += other.y;
            result.z += other.z;
        }
        return result;
    }
    
    static float dotProduct(JacksVector v1, JacksVector v2) {
        return (v1.x * v2.x + v1.y * v2.y + v1.z * v2.z);
    }
    
    static JacksVector crossProduct(JacksVector v1, JacksVector v2) {
        return new JacksVector(
                v1.y * v2.z - v1.z * v2.y,
                v1.z * v2.x - v1.x * v2.z,
                v1.x * v2.y - v1.y * v2.x);
    }
    
    static JacksVector multiply(float alpha, JacksVector vector) {
        return new JacksVector(alpha * vector.x,
                alpha * vector.y, alpha * vector.z);
    }
    
    public String toString() {
        return x + ", " + y + ", " + z;
    }
}
