public class JacksOrigin {
    JacksVector x = new JacksVector(1, 0, 0);
    JacksVector y = new JacksVector(0, 1, 0);
    JacksVector z = new JacksVector(0, 0, 1);
    JacksVector translate = new JacksVector(0, 0, 0);
    
    JacksOrigin() {};
    
    JacksOrigin(JacksOrigin other) {
        x = other.x.clone();
        y = other.y.clone();
        z = other.z.clone();
        translate = other.translate.clone();
    }

    void reset() {
        x.setXYZ(1, 0, 0);
        y.setXYZ(0, 1, 0);
        z.setXYZ(0, 0, 1);
        translate.setXYZ(0, 0, 0);
    }
    
    void rotate(float angleX, float angleY, float angleZ) {
        x.rotateZ(angleZ);
        x.rotateY(angleY);
        x.rotateX(angleX);
        y.rotateZ(angleZ);
        y.rotateY(angleY);
        y.rotateX(angleX);
        z.rotateZ(angleZ);
        z.rotateY(angleY);
        z.rotateX(angleX);
    }
    
    void translate(float translateX, float translateY, float translateZ) {
        translate.add(JacksVector.multiply(translateX, x),
                JacksVector.multiply(translateY, y),
                JacksVector.multiply(translateZ, z));
    }
    
    void copyAttribute(JacksOrigin other) {
        x.copyXYZ(other.x);
        y.copyXYZ(other.y);
        z.copyXYZ(other.z);
        translate.copyXYZ(other.translate);
    }
    
    protected JacksOrigin clone(){
        return new JacksOrigin(this);
    }
    
    public String toString() {
        return "x:" + x.x + ", " + x.y + ", " + x.z + "\n" +
                "y:" + y.x + ", " + y.y + ", " + y.z + "\n" +
                "z:" + z.x + ", " + z.y + ", " + z.z + "\n" +
                "t:" + translate.x + ", " + translate.y + ", " + translate.z + "\n";
    }
}
