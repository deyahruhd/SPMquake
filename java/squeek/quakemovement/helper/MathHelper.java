package squeek.quakemovement.helper;

public class MathHelper {
    public static double logScale(double x, double pow) {
        return (Math.log (x + Math.exp (-pow)) + pow) / pow;
    }
}
