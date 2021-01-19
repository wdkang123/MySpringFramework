# 手写框架IOC

吃水不忘挖井人 感谢[椰子Tyshawn](https://tyshawnlee.blog.csdn.net/)

文章来源：https://blog.csdn.net/litianxiang_kaola/article/details/79121561

（下面在原文基础上 进行了一些修改）



# 1.简介

本篇博客主要实现两个功能: Bean容器 和 IOC.



## 1.1 Bean容器 

Bean容器也就是Spring容器 在学习完Spring之后

如果需要一句话来形容Spring 我们经常会说：

Spring是一个容器 管理着应用程序所有的bean的装配的生命周期

从这句话就可以看出Spring容器的重要性

Spring容器其实是一个HashMap的映射 里面存储了应用中所有的bean的实例

key为该bean实例的class对象



**Spring有两种容器**

一种是`BeanFactory` 一种是`ApplicationContext`

BeanFactory采取延迟加载 在第一次调用getBean的时候 才会初始化

而ApplicationContext在启动时 就会加载所有的bean



手写的这个Spring是一个ApplicationContext容器



## 1.2 IOC

**IOC实现的思路如下：**

首先配置一个配置文件定义了应用的基础包 也就是java源码路径

读取基础包 然后通过类加载器获取到应用中所有的class对象 存储到一个集合当中

获取应用中所有bean的class对象 通过反射机制 存储到bean容器中

遍历bean容器中所有bean 为所有带@Autowired注解的属性注入实例

IOC操作要在应用启动时完成 所以必须写在静态代码块中



# 2.手写的实现

## 2.1 定义注解

### 2.1.1 处理器注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
}
```



### 2.1.2 处理器方法注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

    /**
     * 请求路径
     * @return
     */
    String value() default "";

    /**
     * 请求方法
     * @return
     */
    RequestMethod method() default RequestMethod.GET;
}

//请求方法枚举类
public enum RequestMethod {
    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
}
```



### 2.1.3 依赖注入注解

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
}
```



### 2.1.4 业务类注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
}
```



## 2.2 配置文件

该框架是不存在配置文件的 配置文件是用户需要自定义一些配置项 所以创建一个配置文件

而框架需要做的就是读取用户的配置文件 如果用户没有配置 就用默认的配置

**那么框架如何来加载用户自定义的配置文件呢？**



### 2.2.1 ConfigConstant 常量接口

我们要定义一个名为 ConfigConstant的常量接口 让它来维护配置文件中相关的配置项名称

```java
public interface ConfigConstant {
    //配置文件的名称
    String CONFIG_FILE = "handwritten.properties";

    //数据源
    String JDBC_DRIVER = "handwritten.framework.jdbc.driver";
    String JDBC_URL = "handwritten.framework.jdbc.url";
    String JDBC_USERNAME = "handwritten.framework.jdbc.username";
    String JDBC_PASSWORD = "handwritten.framework.jdbc.password";

    //java源码地址
    String APP_BASE_PACKAGE = "handwritten.framework.app.base_package";
    //jsp页面路径
    String APP_JSP_PATH = "handwritten.framework.app.jsp_path";
    //静态资源路径
    String APP_ASSET_PATH = "handwritten.framework.app.asset_path";
}
```



### 2.2.2 PropsUtil工具类

接着使用PropsUtil工具来读取属性文件

（有点长 但是并不难 耐心一点看）

都是些static的静态方法 方便调用

```java
public final class PropsUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropsUtil.class);

    /**
     * 加载属性文件
     */
    public static Properties loadProps(String fileName) {
        Properties props = null;
        InputStream is = null;
        try {
            is = ClassUtil.getClassLoader().getResourceAsStream(fileName);
            if (is == null) {
                throw new FileNotFoundException(fileName + " file is not found");
            }
            props = new Properties();
            props.load(is);
        } catch (IOException e) {
            LOGGER.error("load properties file failure", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("close input stream failure", e);
                }
            }
        }
        return props;
    }

    /**
     * 获取 String 类型的属性值（默认值为空字符串）
     */
    public static String getString(Properties props, String key) {
        return getString(props, key, "");
    }

    /**
     * 获取 String 类型的属性值（可指定默认值）
     */
    public static String getString(Properties props, String key, String defaultValue) {
        String value = defaultValue;
        if (props.containsKey(key)) {
            value = props.getProperty(key);
        }
        return value;
    }

    /**
     * 获取 int 类型的属性值（默认值为 0）
     */
    public static int getInt(Properties props, String key) {
        return getInt(props, key, 0);
    }

    /**
     * 获取 int 类型的属性值（可指定默认值）
     */
    public static int getInt(Properties props, String key, int defaultValue) {
        int value = defaultValue;
        if (props.containsKey(key)) {
            value = Integer.parseInt(props.getProperty(key));
        }
        return value;
    }

    /**
     * 获取 boolean 类型属性（默认值为 false）
     */
    public static boolean getBoolean(Properties props, String key) {
        return getBoolean(props, key, false);
    }

    /**
     * 获取 boolean 类型属性（可指定默认值）
     */
    public static boolean getBoolean(Properties props, String key, boolean defaultValue) {
        boolean value = defaultValue;
        if (props.containsKey(key)) {
            value = Boolean.parseBoolean(props.getProperty(key));
        }
        return value;
    }
}
```



### 2.2.3 ConfigHelper 助手类

最后借助PropsUtil工具类 来实现 ConfigHelper助手类

框架通过ConfigHelper类就可以加载用户自定义的配置文件了

从代码中可以看 部分配置有默认值 当用户没有配置时会使用默认配置

```java
public final class ConfigHelper {

    /**
     * 加载配置文件的属性
     */
    private static final Properties CONFIG_PROPS = PropsUtil.loadProps(ConfigConstant.CONFIG_FILE);

    /**
     * 获取 JDBC 驱动
     */
    public static String getJdbcDriver() {
        return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.JDBC_DRIVER);
    }

    /**
     * 获取 JDBC URL
     */
    public static String getJdbcUrl() {
        return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.JDBC_URL);
    }

    /**
     * 获取 JDBC 用户名
     */
    public static String getJdbcUsername() {
        return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.JDBC_USERNAME);
    }

    /**
     * 获取 JDBC 密码
     */
    public static String getJdbcPassword() {
        return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.JDBC_PASSWORD);
    }

    /**
     * 获取应用基础包名
     */
    public static String getAppBasePackage() {
        return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.APP_BASE_PACKAGE);
    }

    /**
     * 获取应用 JSP 路径
     */
    public static String getAppJspPath() {
        return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.APP_JSP_PATH, "/WEB-INF/view/");
    }

    /**
     * 获取应用静态资源路径
     */
    public static String getAppAssetPath() {
        return PropsUtil.getString(CONFIG_PROPS, ConfigConstant.APP_ASSET_PATH, "/asset/");
    }

    /**
     * 根据属性名获取 String 类型的属性值
     */
    public static String getString(String key) {
        return PropsUtil.getString(CONFIG_PROPS, key);
    }

    /**
     * 根据属性名获取 int 类型的属性值
     */
    public static int getInt(String key) {
        return PropsUtil.getInt(CONFIG_PROPS, key);
    }

    /**
     * 根据属性名获取 boolean 类型的属性值
     */
    public static boolean getBoolean(String key) {
        return PropsUtil.getBoolean(CONFIG_PROPS, key);
    }
}
```



## 2.3 class对象集合

在完成了第一步加载配置文件之后 我们接下来的第二步就是将应用中所有的Class对象都放到一个集合当中

### 2.3.1 ClassUtil 工具类

该工具类可以加载全限类名得到Class类 以及获取指定包名下的所有Class类

（又是很长 耐心看）

```java
public final class ClassUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);

    /**
     * 获取类加载器
     */
    public static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 加载类
     * @param className 类名
     * @param isInitialized 是否初始化
     * @return
     */
    public static Class<?> loadClass(String className, boolean isInitialized) {
        Class<?> cls;
        try {
            cls = Class.forName(className, isInitialized, getClassLoader());
        } catch (ClassNotFoundException e) {
            LOGGER.error("load class failure", e);
            throw new RuntimeException(e);
        }
        return cls;
    }

    /**
     * 加载类（默认将初始化类）
     */
    public static Class<?> loadClass(String className) {
        return loadClass(className, true);
    }

    /**
     * 获取指定包名下的所有类
     */
    public static Set<Class<?>> getClassSet(String packageName) {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        try {
            Enumeration<URL> urls = getClassLoader().getResources(packageName.replace(".", "/"));
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url != null) {
                    String protocol = url.getProtocol();
                    if (protocol.equals("file")) {
                        String packagePath = url.getPath().replaceAll("%20", " ");
                        addClass(classSet, packagePath, packageName);
                    } else if (protocol.equals("jar")) {
                        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                        if (jarURLConnection != null) {
                            JarFile jarFile = jarURLConnection.getJarFile();
                            if (jarFile != null) {
                                Enumeration<JarEntry> jarEntries = jarFile.entries();
                                while (jarEntries.hasMoreElements()) {
                                    JarEntry jarEntry = jarEntries.nextElement();
                                    String jarEntryName = jarEntry.getName();
                                    if (jarEntryName.endsWith(".class")) {
                                        String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
                                        doAddClass(classSet, className);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("get class set failure", e);
            throw new RuntimeException(e);
        }
        return classSet;
    }

    private static void addClass(Set<Class<?>> classSet, String packagePath, String packageName) {
        File[] files = new File(packagePath).listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory();
            }
        });
        for (File file : files) {
            String fileName = file.getName();
            if (file.isFile()) {
                String className = fileName.substring(0, fileName.lastIndexOf("."));
                if (StringUtils.isNotEmpty(packageName)) {
                    className = packageName + "." + className;
                }
                doAddClass(classSet, className);
            } else {
                String subPackagePath = fileName;
                if (StringUtils.isNotEmpty(packagePath)) {
                    subPackagePath = packagePath + "/" + subPackagePath;
                }
                String subPackageName = fileName;
                if (StringUtils.isNotEmpty(packageName)) {
                    subPackageName = packageName + "." + subPackageName;
                }
                addClass(classSet, subPackagePath, subPackageName);
            }
        }
    }

    private static void doAddClass(Set<Class<?>> classSet, String className) {
        Class<?> cls = loadClass(className, false);
        classSet.add(cls);
    }
}
```



### 2.3.2 ClassHelper 助手类

借助ClassUtil实现ClassHelper助手类

这个类很重要

ClassHelper助手类在自身加载的时候通过ConfigHelper助手类获取应用的基础包名

然后通过ClassUtil工具类来获取基础包名下所有类

存储到CLASS_SET集合中 除此之外 其他的方法在后边的代码中会被用到

```java
public final class ClassHelper {

    /**
     * 定义类集合（存放基础包名下的所有类）
     */
    private static final Set<Class<?>> CLASS_SET;

    static {
        //获取基础包名
        String basePackage = ConfigHelper.getAppBasePackage();
        //获取基础包名下所有类
        CLASS_SET = ClassUtil.getClassSet(basePackage);
    }

    /**
     * 获取基础包名下的所有类
     */
    public static Set<Class<?>> getClassSet() {
        return CLASS_SET;
    }

    /**
     * 获取基础包名下所有 Service 类
     */
    public static Set<Class<?>> getServiceClassSet() {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        for (Class<?> cls : CLASS_SET) {
            if (cls.isAnnotationPresent(Service.class)) {
                classSet.add(cls);
            }
        }
        return classSet;
    }

    /**
     * 获取基础包名下所有 Controller 类
     */
    public static Set<Class<?>> getControllerClassSet() {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        for (Class<?> cls : CLASS_SET) {
            if (cls.isAnnotationPresent(Controller.class)) {
                classSet.add(cls);
            }
        }
        return classSet;
    }

    /**
     * 获取基础包名下所有 Bean 类（包括：Controller、Service）
     */
    public static Set<Class<?>> getBeanClassSet() {
        Set<Class<?>> beanClassSet = new HashSet<Class<?>>();
        beanClassSet.addAll(getServiceClassSet());
        beanClassSet.addAll(getControllerClassSet());
        return beanClassSet;
    }

    /**
     * 获取基础包名下某父类的所有子类 或某接口的所有实现类
     */
    public static Set<Class<?>> getClassSetBySuper(Class<?> superClass) {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        for (Class<?> cls : CLASS_SET) {
            //isAssignableFrom() 指 superClass 和 cls 是否相同或 superClass 是否是 cls 的父类/接口
            if (superClass.isAssignableFrom(cls) && !superClass.equals(cls)) {
                classSet.add(cls);
            }
        }
        return classSet;
    }

    /**
     * 获取基础包名下带有某注解的所有类
     */
    public static Set<Class<?>> getClassSetByAnnotation(Class<? extends Annotation> annotationClass) {
        Set<Class<?>> classSet = new HashSet<Class<?>>();
        for (Class<?> cls : CLASS_SET) {
            if (cls.isAnnotationPresent(annotationClass)) {
                classSet.add(cls);
            }
        }
        return classSet;
    }
}
```



## 2.4 Bean容器

在将应用程序中所有的Class对象存储到CLASS_SET集合中后 我们就可以构建Bean容器了

### 2.4.1 ReflectUtil工具类

我们需要一个反射工具类 进行各种反射操作

```java
public final class ReflectionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtil.class);

    /**
     * 创建实例
     */
    public static Object newInstance(Class<?> cls) {
        Object instance;
        try {
            instance = cls.newInstance();
        } catch (Exception e) {
            LOGGER.error("new instance failure", e);
            throw new RuntimeException(e);
        }
        return instance;
    }

    /**
     * 创建实例（根据类名）
     */
    public static Object newInstance(String className) {
        Class<?> cls = ClassUtil.loadClass(className);
        return newInstance(cls);
    }

    /**
     * 调用方法
     */
    public static Object invokeMethod(Object obj, Method method, Object... args) {
        Object result;
        try {
            method.setAccessible(true);
            result = method.invoke(obj, args);
        } catch (Exception e) {
            LOGGER.error("invoke method failure", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 设置成员变量的值
     */
    public static void setField(Object obj, Field field, Object value) {
        try {
            field.setAccessible(true); //去除私有权限
            field.set(obj, value);
        } catch (Exception e) {
            LOGGER.error("set field failure", e);
            throw new RuntimeException(e);
        }
    }
}
```



### 2.4.2 Bean容器助手类

BeanHelper在类加载时 会创建一个Bean容器 BEAN_MAP

然后获取到应用中所有的bean的Class对象

再通过反射创建bean实例 存储到BEAN_MAP中

```java
public final class BeanHelper {

    /**
     * BEAN_MAP相当于一个Spring容器, 拥有应用所有bean的实例
     */
    private static final Map<Class<?>, Object> BEAN_MAP = new HashMap<Class<?>, Object>();

    static {
        //获取应用中的所有bean
        Set<Class<?>> beanClassSet = ClassHelper.getBeanClassSet();
        //将bean实例化, 并放入bean容器中
        for (Class<?> beanClass : beanClassSet) {
            Object obj = ReflectionUtil.newInstance(beanClass);
            BEAN_MAP.put(beanClass, obj);
        }
    }

    /**
     * 获取 Bean 容器
     */
    public static Map<Class<?>, Object> getBeanMap() {
        return BEAN_MAP;
    }

    /**
     * 获取 Bean 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> cls) {
        if (!BEAN_MAP.containsKey(cls)) {
            throw new RuntimeException("can not get bean by class: " + cls);
        }
        return (T) BEAN_MAP.get(cls);
    }

    /**
     * 设置 Bean 实例
     */
    public static void setBean(Class<?> cls, Object obj) {
        BEAN_MAP.put(cls, obj);
    }
}
```



## 2.5 实现IOC功能

最后就是实现IOC了 要做的就是遍历Bean容器中的bean

为所有带@Autowired注解的属性注入实例 

这个实例 Bean容器中获取

```java
public final class IocHelper {

    /**
     * 遍历bean容器所有bean的属性, 为所有带@Autowired注解的属性注入实例
     */
    static {
        //遍历bean容器里的所有bean
        Map<Class<?>, Object> beanMap = BeanHelper.getBeanMap();
        if (MapUtils.isNotEmpty(beanMap)) {
            for (Map.Entry<Class<?>, Object> beanEntry : beanMap.entrySet()) {
                //bean的class类
                Class<?> beanClass = beanEntry.getKey();
                //bean的实例
                Object beanInstance = beanEntry.getValue();
                //暴力反射获取属性
                Field[] beanFields = beanClass.getDeclaredFields();
                //遍历bean的属性
                if (ArrayUtils.isNotEmpty(beanFields)) {
                    for (Field beanField : beanFields) {
                        //判断属性是否带Autowired注解
                        if (beanField.isAnnotationPresent(Autowired.class)) {
                            //属性类型
                            Class<?> beanFieldClass = beanField.getType();
                            //如果beanFieldClass是接口, 就获取接口对应的实现类
                            beanFieldClass = findImplementClass(beanFieldClass);
                            //获取Class类对应的实例
                            Object beanFieldInstance = beanMap.get(beanFieldClass);
                            if (beanFieldInstance != null) {
                                ReflectionUtil.setField(beanInstance, beanField, beanFieldInstance);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取接口对应的实现类
     */
    public static Class<?> findImplementClass(Class<?> interfaceClass) {
        Class<?> implementClass = interfaceClass;
        //接口对应的所有实现类
        Set<Class<?>> classSetBySuper = ClassHelper.getClassSetBySuper(interfaceClass);
        if (CollectionUtils.isNotEmpty(classSetBySuper)) {
            //获取第一个实现类
            implementClass = classSetBySuper.iterator().next();
        }
        return implementClass;
    }
}
```



以上就是Bean容器和IOC功能的全部内容 当应用启动之后 就会生成Bean容器 并实现IOC功能

所有加了@Autowired注解的属性 都在应用启动后有了实例

























