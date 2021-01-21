package com.myspring.framework;

import com.myspring.framework.helper.BeanHelper;
import com.myspring.framework.helper.ClassHelper;
import com.myspring.framework.helper.ControllerHelper;
import com.myspring.framework.helper.IocHelper;
import com.myspring.framework.util.ClassUtil;

public final class HelperLoader {
  public static void init() {
    Class<?>[] classList = {
        ClassHelper.class,
        BeanHelper.class,
        IocHelper.class,
        ControllerHelper.class
    };
    for (Class<?> cls : classList) {
      ClassUtil.loadClass(cls.getName());
    }
  }
}
