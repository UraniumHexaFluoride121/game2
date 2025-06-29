package foundation.math;

import java.util.function.Supplier;

public abstract class MathUtil {
    public static float lerp(float a, float b, float t) {
        return (b - a) * t + a;
    }

    public static float normalise(float min, float max, float v) {
        return (v - min) / (max - min);
    }

    public static float map(float fromMin, float fromMax, float toMin, float toMax, float v) {
        return lerp(toMin, toMax, normalise(fromMin, fromMax, v));
    }

    public static float map(float fromMin, float fromMax, float toMin, float toMax, float v, boolean clamp) {
        float lerp = lerp(toMin, toMax, normalise(fromMin, fromMax, v));
        return clamp ? Math.clamp(toMin, toMax, lerp) : lerp;
    }

    public static <T> T compare(float value, float target, T lt, T eq, T gt) {
        return compare(value, target, 0.001f, lt, eq, gt);
    }

    public static <T> T compare(float value, float target, float epsilon, T lt, T eq, T gt) {
        if (value < target - epsilon)
            return lt;
        if (value > target + epsilon)
            return gt;
        return eq;
    }

    public static float linearTo(float from, float to, float v, float deltaTime) {
        float newValue = from + deltaTime * v * Math.signum(to - from);
        if (Math.signum(from - to) != Math.signum(newValue - to))
            return to;
        return newValue;
    }

    public static float expTo(float from, float to, float decay, float deltaTime) {
        float newValue = (float) (to - (to - from) * Math.exp(-decay * deltaTime));
        if (Math.abs(from - to) < 0.01f)
            return to;
        return newValue;
    }

    public static int randIntBetween(int min, int max, Supplier<Double> random) {
        int realMin = Math.min(min, max), realMax = Math.max(min, max);
        return ((int) Math.floor(lerp(realMin, realMax + 1, random.get().floatValue())));
    }

    public static float randFloatBetween(float min, float max, Supplier<Double> random) {
        return (float) (random.get() * (max - min) + min);
    }

    public static boolean randBoolean(float probability, Supplier<Double> random) {
        return random.get() < probability;
    }

    public static float[] findQuadratic(float root1, float root2, float ext) {
        float mid = (root1 + root2) / 2;
        float d = Math.abs(mid - root1);
        float p = -mid * 2;
        float q = -(d * d) + (p * p / 4);
        float a = ext / (mid * mid + p * mid + q);
        if (Float.isNaN(a))
            return null;
        return new float[] {
                a, p * a, q * a
        };
    }

    public static float[] findQuadratic(float root1, float root2, float px, float py) {
        float mid = (root1 + root2) / 2;
        float d = Math.abs(mid - root1);
        float p = -mid * 2;
        float q = -(d * d) + (p * p / 4);
        float a = py / (px * px + p * px + q);
        if (Float.isNaN(a))
            return null;
        return new float[] {
                a, p * a, q * a
        };
    }

    public static float[] solveQuadratic(float a, float b, float c) {
        float p = b / a;
        float q = c / a;
        float d = p * p / 4 - q;
        if (d < 0)
            return null;
        return new float[]{
                ((float) (-(p / 2) - Math.sqrt(d))),
                ((float) (-(p / 2) + Math.sqrt(d)))
        };
    }

    public static float[] solveCubic(float a, float b, float c, float d) {
        float p = c / a, q = d / a;
        float P = p - (b * b) / (3 * a * a), Q = q - (b * c) / (3 * a * a) + (2 * b * b * b) / (27 * a * a * a);
        float D = (Q * Q) / 4 + (P * P * P) / 27;
        float t = -b / (3 * a);

        if (Math.abs(D) < 0.00001)
            D = 0;
        if (D > 0) {
            return new float[]{
                    ((float) (Math.cbrt(-Q / 2 + Math.sqrt(D)) + Math.cbrt(-Q / 2 - Math.sqrt(D)))) + t
            };
        } else if (D == 0) {
            return new float[]{
                    -((float) Math.cbrt(-Q / 2)) + t,
                    2 * ((float) Math.cbrt(-Q / 2)) + t
            };
        } else {
            float P3 = P / 3;
            float angle = ((float) Math.acos((-Q / 2) / Math.sqrt(-P3 * P3 * P3)) / 3);
            return new float[]{
                    ((float) (2 * Math.sqrt(-P3) * Math.cos(angle))) + t,
                    ((float) (2 * Math.sqrt(-P3) * Math.cos(angle + 2 * Math.PI / 3))) + t,
                    ((float) (2 * Math.sqrt(-P3) * Math.cos(angle + 4 * Math.PI / 3))) + t
            };
        }
    }

    public static String floatToString(float v, int decimals) {
        int r = 1;
        for (int i = 0; i < decimals; i++) {
            r *= 10;
        }
        char[] chars = String.valueOf(Math.round(v * r) / (float) r).toCharArray();
        StringBuilder s = new StringBuilder();
        boolean isDecimal = false;
        for (int i = 0; ; i++) {
            if (isDecimal) {
                for (int j = i; j < i + decimals; j++) {
                    if (j < chars.length) {
                        s.append(chars[j]);
                    } else
                        s.append('0');
                }
                break;
            }
            if (i < chars.length) {
                if (chars[i] == '.') {
                    if (decimals == 0)
                        break;
                    isDecimal = true;
                }
                s.append(chars[i]);
            } else {
                break;
            }
        }
        return s.toString();
    }

    public static int min(int... values) {
        int v = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] < v)
                v = values[i];
        }
        return v;
    }

    public static float fraction(float v) {
        return v - ((int) v);
    }
}
