import java.util.Arrays;

public class JacksFace {
    int vertexList[] = new int[0];
    UVCoordinate uv[] = new UVCoordinate[0];
    JacksVector normal = new JacksVector();
    JacksMaterial material = new JacksMaterial();
    JacksGeometry parent;
    boolean smooth = false;
    
    JacksFace(JacksGeometry parent) {
        this.parent = parent;
    }
    
    JacksFace(JacksGeometry parent, int...vertices) {
        this(parent);
        setVertices(vertices);
    }
    
    void addVertex(int vertex) {
        vertexList = Arrays.copyOf(vertexList, vertexList.length + 1);
        vertexList[vertexList.length - 1] = vertex;
    }
    
    void setVertices(int[] vertices) {
        vertexList = Arrays.copyOf(vertices, vertices.length);
    }
    
    void addUV(UVCoordinate newUV) {
        uv = Arrays.copyOf(uv, uv.length + 1);
        uv[uv.length - 1] = newUV;
    }
    
    void setUV(UVCoordinate[] newUV) {
        uv = Arrays.copyOf(newUV, newUV.length);
    }
    
    void calculateNormal() {
        normal = new JacksVector(parent.vertexList[vertexList[0]],
                parent.vertexList[vertexList[1]]);
        normal.crossProduct(new JacksVector(parent.vertexList[vertexList[1]],
                parent.vertexList[vertexList[2]]));
        normal.normalize();
    }
    
    static class UVCoordinate {
        float u, v;

        public UVCoordinate() {
        }

        public UVCoordinate(float u, float v) {
            this.u = u;
            this.v = v;
        }

        @Override
        protected UVCoordinate clone() {
            return new UVCoordinate(u, v);
        }
    }
}
