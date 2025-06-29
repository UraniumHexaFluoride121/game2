package foundation.math;

import render.Renderable;

import java.awt.*;

public class HitBox {
    public float up, down, left, right;

    public HitBox(float up, float down, float left, float right, ObjPos origin) {
        this.up = origin.y + up;
        this.down = origin.y - down;
        this.left = origin.x - left;
        this.right = origin.x + right;
    }

    public HitBox(float up, float down, float left, float right) {
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
    }

    public HitBox(ObjPos from, ObjPos to) {
        this(Math.max(from.y, to.y), Math.min(from.y, to.y), Math.min(from.x, to.x), Math.max(from.x, to.x));
    }

    public static HitBox createFromOriginAndSize(float x, float y, float width, float height) {
        return new HitBox(y + height, y, x, x + width);
    }

    public HitBox copy() {
        return new HitBox(up, down, left, right);
    }

    public HitBox scale(float s) {
        up *= s;
        down *= s;
        left *= s;
        right *= s;
        return this;
    }

    public HitBox translate(float x, float y) {
        up += y;
        down += y;
        left += x;
        right += x;
        return this;
    }

    public HitBox translate(ObjPos pos) {
        return translate(pos.x, pos.y);
    }

    public HitBox expand(float amount) {
        up += amount;
        down -= amount;
        left -= amount;
        right += amount;
        return this;
    }

    public HitBox expand(float x, float y) {
        up += y;
        down -= y;
        left -= x;
        right += x;
        return this;
    }

    public HitBox expand(float up, float down, float left, float right) {
        this.up += up;
        this.down -= down;
        this.left -= left;
        this.right += right;
        return this;
    }

    public HitBox expandToFit(HitBox box) {
        return expandToFit(box.getTop(), box.getBottom(), box.getLeft(), box.getRight());
    }

    public HitBox expandToFit(float up, float down, float left, float right) {
        this.up = Math.max(this.up, up);
        this.down = Math.min(this.down, down);
        this.right = Math.max(this.right, right);
        this.left = Math.min(this.left, left);
        return this;
    }

    public HitBox offset(ObjPos pos) {
        return offset(pos.x, pos.y);
    }

    public HitBox offset(float x, float y) {
        up += y;
        down += y;
        right += x;
        left += x;
        return this;
    }

    public float middleX() {
        return (right + left) / 2;
    }

    public float middleY() {
        return (up + down) / 2;
    }

    public float width() {
        return right - left;
    }

    public float height() {
        return up - down;
    }

    public void fill(Graphics2D g, Color c) {
        g.setColor(c);
        g.scale(1d / Renderable.SCALING, 1d / Renderable.SCALING);
        g.fillRect(
                (int) (left * Renderable.SCALING),
                (int) (down * Renderable.SCALING),
                (int) (width() * Renderable.SCALING),
                (int) (height() * Renderable.SCALING)
        );
        g.scale(Renderable.SCALING, Renderable.SCALING);
    }

    public void fillRounded(Graphics2D g, Color c, float rounding) {
        g.setColor(c);
        g.scale(1d / Renderable.SCALING, 1d / Renderable.SCALING);
        g.fillRoundRect(
                (int) (left * Renderable.SCALING),
                (int) (down * Renderable.SCALING),
                (int) (width() * Renderable.SCALING),
                (int) (height() * Renderable.SCALING),
                (int) (rounding * Renderable.SCALING),
                (int) (rounding * Renderable.SCALING)
        );
        g.scale(Renderable.SCALING, Renderable.SCALING);
    }

    public float getTop() {
        return up;
    }

    public float getBottom() {
        return down;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

    public boolean isColliding(HitBox box) {
        return (getRight() > box.getLeft() && box.getRight() > getLeft()) &&
                (getTop() > box.getBottom() && box.getTop() > getBottom());
    }

    public boolean isPositionInside(ObjPos pos) {
        return pos.y < getTop() && pos.y > getBottom() && pos.x < getRight() && pos.x > getLeft();
    }

    public String asString() {
        return "Left: " + getLeft() + ", Right: " + getRight() + ", Bottom: " + getBottom() + ", Top: " + +getTop();
    }
}
