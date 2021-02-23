package net.snakefangox.worldshell.collision;

import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

/**
 * Originally written by Stuff-Stuffs, here until I figure out updating multipart entities
 * https://github.com/Stuff-Stuffs/MultipartEntities
 */
public final class QuaternionD {
    public static final QuaternionD IDENTITY = new QuaternionD(0, 0, 0, 1);
    private final double x;
    private final double y;
    private final double z;
    private final double w;

    public QuaternionD(final double x, final double y, final double z, final double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public QuaternionD(final Vec3d axis, double rotationAngle, final boolean degrees) {
        if (degrees) {
            rotationAngle *= 0.017453292F;
        }

        final double f = Math.sin(rotationAngle / 2.0);
        x = axis.getX() * f;
        y = axis.getY() * f;
        z = axis.getZ() * f;
        w = Math.cos(rotationAngle / 2.0);
    }

    public QuaternionD(double pitch, double yaw, double roll, final boolean degrees) {
        if (degrees) {
            pitch *= 0.017453292F;
            yaw *= 0.017453292F;
            roll *= 0.017453292F;
        }

        final double f = Math.sin(0.5F * pitch);
        final double g = Math.cos(0.5F * pitch);
        final double h = Math.sin(0.5F * yaw);
        final double i = Math.cos(0.5F * yaw);
        final double j = Math.sin(0.5F * roll);
        final double k = Math.cos(0.5F * roll);
        x = f * i * k + g * h * j;
        y = g * h * k - f * i * j;
        z = f * h * k + g * i * j;
        w = g * i * k - f * h * j;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getW() {
        return w;
    }

    public QuaternionD hamiltonProduct(final QuaternionD other) {
        final double f = getX();
        final double g = getY();
        final double h = getZ();
        final double i = getW();
        final double j = other.getX();
        final double k = other.getY();
        final double l = other.getZ();
        final double m = other.getW();
        return new QuaternionD(i * j + f * m + g * l - h * k, i * k - f * l + g * m + h * j, i * l + f * k - g * j + h * m, i * m - f * j - g * k - h * l);
    }

    public Quaternion toFloatQuat() {
        return new Quaternion((float) x, (float) y, (float) z, (float) w);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final QuaternionD that = (QuaternionD) o;

        if (Double.compare(that.x, x) != 0) {
            return false;
        }
        if (Double.compare(that.y, y) != 0) {
            return false;
        }
        if (Double.compare(that.z, z) != 0) {
            return false;
        }
        return Double.compare(that.w, w) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(w);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
