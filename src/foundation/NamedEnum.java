package foundation;

import foundation.math.MathUtil;

public interface NamedEnum {
    String getName();

    default String getNameFirstUpper() {
        return MathUtil.firstUpper(getName());
    }
}
