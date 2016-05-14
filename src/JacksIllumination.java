public class JacksIllumination {
    float dR, dG, dB, sR, sG, sB;

    public JacksIllumination() {
    }

    public JacksIllumination(float dR, float dG, float dB, float sR, float sG, float sB) {
        this.dR = dR;
        this.dG = dG;
        this.dB = dB;
        this.sR = sR;
        this.sG = sG;
        this.sB = sB;
    }
    
    void copyAttribute(JacksIllumination other) {
        this.dR = other.dR;
        this.dG = other.dG;
        this.dB = other.dB;
        this.sR = other.sR;
        this.sG = other.sG;
        this.sB = other.sB;
    }
    
    @Override
    public String toString() {
        return dR + ", " + dG + ", " + dB + ", " + sR + ", " + sG + ", " + sB;
    }
}
