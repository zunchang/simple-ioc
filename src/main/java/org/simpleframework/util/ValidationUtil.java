package org.simpleframework.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ValidationUtil {
    /**
     * Collection是否为noll或size为0
     *
     * @param obj 集合
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> obj) {
        return obj == null || obj.isEmpty();
    }

    /**
     * String是否为noll或size为0
     *
     * @param obj String
     * @return 是否为空
     */
    public static boolean isEmpty(String obj) {
        return obj == null || "".equals(obj);
    }

    public static boolean isEmpty(Object[] obj) {
        return obj == null || obj.length == 0;
    }

    public static boolean isEmpty(Map<?, ?> obj) {
        return obj == null || obj.isEmpty();
    }

}
