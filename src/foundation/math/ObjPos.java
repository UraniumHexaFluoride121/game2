package foundation.math;

import render.GameRenderer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.io.Serializable;

public class ObjPos implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public static final ObjPos ORIGIN = new ObjPos();
    public float x;
    public float y;

    public static ObjPos imageSize(BufferedImage image) {
        return new ObjPos(image.getWidth(), image.getHeight());
    }

    public ObjPos(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }

    public ObjPos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public ObjPos(float x) {
        this.x = x;
        this.y = 0;
    }

    public ObjPos() {
        this.x = 0;
        this.y = 0;
    }

    public ObjPos(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public ObjPos copy() {
        return new ObjPos(x, y);
    }

    public ObjPos add(ObjPos other) {
        x += other.x;
        y += other.y;
        return this;
    }

    public ObjPos add(Point other) {
        x += other.x;
        y += other.y;
        return this;
    }

    public ObjPos add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public ObjPos add(float value) {
        this.x += value;
        this.y += value;
        return this;
    }

    public ObjPos min(float value) {
        x = Math.min(x, value);
        y = Math.min(y, value);
        return this;
    }

    public ObjPos min(ObjPos objPos) {
        x = Math.min(x, objPos.x);
        y = Math.min(y, objPos.y);
        return this;
    }

    public ObjPos minLength(float value) {
        if (value < length())
            setLength(value);
        return this;
    }

    public ObjPos max(float value) {
        x = Math.max(x, value);
        y = Math.max(y, value);
        return this;
    }

    public ObjPos max(ObjPos objPos) {
        x = Math.max(x, objPos.x);
        y = Math.max(y, objPos.y);
        return this;
    }

    public ObjPos maxLength(float value) {
        if (value > length())
            setLength(value);
        return this;
    }

    public ObjPos abs() {
        x = Math.abs(x);
        y = Math.abs(y);
        return this;
    }

    public ObjPos addRotated(float value, float radians) {
        x += ((float) (Math.cos(radians) * value));
        y += ((float) (Math.sin(radians) * value));
        return this;
    }

    public ObjPos set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public ObjPos set(ObjPos value) {
        x = value.x;
        y = value.y;
        return this;
    }

    public int xInt() {
        return ((int) x);
    }

    public int yInt() {
        return ((int) y);
    }

    public int xIntCeil() {
        return ((int) Math.ceil(x));
    }

    public int yIntCeil() {
        return ((int) Math.ceil(y));
    }

    public int xIntRound() {
        return Math.round(x);
    }

    public int yIntRound() {
        return Math.round(y);
    }

    public static ObjPos rotated(float value, float radians) {
        return new ObjPos((float) (Math.cos(radians) * value), (float) (Math.sin(radians) * value));
    }

    public ObjPos clamp(float xMin, float xMax, float yMin, float yMax) {
        x = Math.min(xMax, Math.max(xMin, x));
        y = Math.min(yMax, Math.max(yMin, y));
        return this;
    }

    public ObjPos lerp(ObjPos to, float t) {
        return new ObjPos(MathUtil.lerp(x, to.x, t), MathUtil.lerp(y, to.y, t));
    }

    public ObjPos lerpTo(ObjPos to, float v, float t) {
        ObjPos newValue = copy().add(to.copy().subtract(this).setLength(v * t));
        if (Math.signum(x - to.x) != Math.signum(newValue.x - to.x))
            newValue.x = to.x;
        if (Math.signum(y - to.y) != Math.signum(newValue.y - to.y))
            newValue.y = to.y;
        x = newValue.x;
        y = newValue.y;
        return this;
    }

    public ObjPos expTo(ObjPos to, float decay, float deltaTime) {
        x = MathUtil.expTo(x, to.x, decay, deltaTime);
        y = MathUtil.expTo(y, to.y, decay, deltaTime);
        return this;
    }

    public ObjPos signum() {
        return new ObjPos(Math.signum(x), Math.signum(y));
    }

    public ObjPos power(float exp) {
        if (x < 0) {
            x = -(float) Math.pow(-x, exp);
        } else {
            x = (float) Math.pow(x, exp);
        }
        if (y < 0) {
            y = -(float) Math.pow(-y, exp);
        } else {
            y = (float) Math.pow(y, exp);
        }
        return this;
    }

    public ObjPos subtract(ObjPos other) {
        x -= other.x;
        y -= other.y;
        return this;
    }

    public ObjPos subtract(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public ObjPos subtract(float value) {
        this.x -= value;
        this.y -= value;
        return this;
    }

    public ObjPos multiply(ObjPos other) {
        x *= other.x;
        y *= other.y;
        return this;
    }

    public ObjPos multiply(float x, float y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public ObjPos multiply(float value) {
        this.x *= value;
        this.y *= value;
        return this;
    }

    public ObjPos divide(ObjPos other) {
        x /= other.x;
        y /= other.y;
        return this;
    }

    public ObjPos divide(float x, float y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public ObjPos divide(float value) {
        this.x /= value;
        this.y /= value;
        return this;
    }

    public ObjPos inverse() {
        x = -x;
        y = -y;
        return this;
    }

    public ObjPos flipX() {
        x = -x;
        return this;
    }

    public ObjPos flipY() {
        y = -y;
        return this;
    }

    public ObjPos addX(float x) {
        this.x += x;
        return this;
    }

    public ObjPos addY(float y) {
        this.y += y;
        return this;
    }

    public ObjPos subtractX(float x) {
        this.x -= x;
        return this;
    }

    public ObjPos subtractY(float y) {
        this.y -= y;
        return this;
    }

    public ObjPos toInt() {
        x = (int) x;
        y = (int) y;
        return this;
    }

    public ObjPos snapErrorToZero(float epsilon) {
        if (Math.abs(x) < epsilon)
            x = epsilon;
        if (Math.abs(y) < epsilon)
            y = epsilon;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ObjPos))
            return false;
        return (x == ((ObjPos) obj).x && y == ((ObjPos) obj).y);
    }

    @Override
    public int hashCode() {
        return ((int) (x * y));
    }

    public static ObjPos random(float fromX, float fromY, float toX, float toY) {
        return new ObjPos(((float) Math.random()) * (toX - fromX) + fromX, ((float) Math.random()) * (toY - fromY) + fromY);
    }

    public float distance(ObjPos other) {
        return copy().subtract(other).length();
    }

    //the line is defined as all the points line(t) = linePoint + lineDirection * t, for all values of t
    public ObjPos closestPointOnLine(ObjPos linePoint, ObjPos lineDirection) {
        lineDirection = lineDirection.copy().normalise();
        return linePoint.copy().add(lineDirection.copy().multiply(-linePoint.copy().subtract(this).dotProduct(lineDirection)));
    }

    //can be negative based on lineDirection
    public float linePointDistanceToClosestPointOnLine(ObjPos linePoint, ObjPos lineDirection) {
        lineDirection = lineDirection.copy().normalise();
        return -linePoint.copy().subtract(this).dotProduct(lineDirection);
    }

    public float angle() {
        return (float) (Math.atan(y / x));
    }

    public float angleBetweenVectors(ObjPos other) {
        float length = length();
        float otherLength = other.length();
        if (length == 0 || otherLength == 0) {
            return 0;
        }
        return (float) Math.acos(this.dotProduct(other) / (length * otherLength));
    }

    public float angleToPos(ObjPos other) {
        return (float) (Math.atan((other.y - y) / (other.x - x)) + ((other.x - x) >= 0 ? 0 : Math.PI));
    }

    public float correctedAngle() {
        if (x == -0)
            x = 0;
        return x >= 0 ? (float) (Math.atan(y / x)) : (float) (Math.atan(y / x) + Math.PI);
    }

    public float yCorrectedAngle() {
        return x > 0 ? (float) (Math.atan(-y / x)) : (float) (Math.atan(-y / x) + Math.PI);
    }

    public static ObjPos getForceVector(float angle, float length) {
        return new ObjPos((float) Math.cos(angle) * length, (float) Math.sin(angle) * length);
    }

    public ObjPos rotate(float radians) {
        float length = length();
        float angle = correctedAngle();
        if (length == 0)
            return this;
        x = (float) Math.cos(angle + radians) * length;
        y = (float) Math.sin(angle + radians) * length;
        return this;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Point toPoint() {
        return new Point((int) x, (int) y);
    }

    public Point roundToPoint() {
        return new Point(xIntRound(), yIntRound());
    }

    public float dotProduct(ObjPos other) {
        return (x * other.x) + (y * other.y);
    }

    public ObjPos normalise() {
        float length = length();
        if (length == 0)
            return set(1, 0);
        return divide(length);
    }

    public ObjPos setLength(float newLength) {
        float length = length();
        if (length == 0)
            return set(newLength, 0);
        return divide(length / newLength);
    }

    public ObjPos addLength(float addLength) {
        float length = length();
        if (length == 0)
            return set(addLength, 0);
        return divide(length / (length + addLength));
    }

    public static ObjPos getEdgeNormal(ObjPos a, ObjPos b) {
        ObjPos edgeVector = a.copy().subtract(b);
        return new ObjPos(-edgeVector.y, edgeVector.x);
    }

    //for a convex shape, define points counter-clockwise for outwards facing normal
    public ObjPos getNormalVector(ObjPos prevPoint, ObjPos nextPoint) {
        ObjPos prevEdgeNormal = getEdgeNormal(prevPoint, this).normalise();
        ObjPos nextEdgeNormal = getEdgeNormal(this, nextPoint).normalise();
        float radians = prevEdgeNormal.angleBetweenVectors(nextEdgeNormal) / 2;
        return prevEdgeNormal.add(nextEdgeNormal).setLength(((float) (1 / Math.cos(radians))));
    }

    public ObjPos perpendicular() {
        float temp = x;
        x = -y;
        y = temp;
        return this;
    }

    public static ObjPos getEdgeVector(ObjPos a, ObjPos b) {
        return a.copy().subtract(b);
    }

    public ObjPos scale(float scale, ObjPos center) {
        multiply(scale);
        subtract(center.multiply(scale));
        return this;
    }

    public ObjPos scaleToBlocks() {
        x = GameRenderer.scaleFloatToBlocks(x);
        y = GameRenderer.scaleFloatToBlocks(y);
        return this;
    }

    public void affineTranslate(AffineTransform t) {
        t.translate(x, y);
    }

    public void affineScale(AffineTransform t) {
        t.scale(x, y);
    }

    public ObjPos log() {
        x = x == 0 ? 0 : (float) Math.log(Math.abs(x)) * Math.signum(x);
        y = y == 0 ? 0 : (float) Math.log(Math.abs(y)) * Math.signum(y);
        return this;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
