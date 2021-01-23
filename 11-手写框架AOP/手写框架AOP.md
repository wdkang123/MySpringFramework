# 手写框架AOP

吃水不忘挖井人 感谢[椰子Tyshawn](https://tyshawnlee.blog.csdn.net/)

文章来源：https://blog.csdn.net/litianxiang_kaola/article/details/79121561

（下面在原文基础上 进行了一些修改）



# 1.简介

前面两篇博客已经实现了Bean容器, IOC功能和MVC功能, 

本篇博客来实现AOP功能和事务管理. 

在看下面的内容之前, 一定要先回顾下Spring AOP和动态代理.



# 2.代理框架

## 2.1 定义注解

### 2.1.1 切面注解

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {
    /**
     * 包名
     */
    String pkg() default "";

    /**
     * 类名
     */
    String cls() default "";
}
```



### 2.1.2 事务注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
}
```



## 2.2 搭建代理框架

这个代理是基于动态代理搭建的 但加入了一个链式代理的功能

目的是为了解决多重代理的问题

也就是目标对象方法被多次增强

### 2.2.1 Proxy接口

定义一个最上层的代理接口

其中 doProxy（） 执行的是链式代理

具体可以看后面的介绍

```java
public interface Proxy {
    /**
     * 执行链式代理
     * 所谓链式代理, 就是说, 可将多个代理通过一条链子串起来, 一个个地去执行, 执行顺序取决于添加到链上的先后顺序
     */
    Object doProxy(ProxyChain proxyChain) throws Throwable;
}
```

### 2.2.2 ProxyChain类

这是一个代理链类 proxyList存储的是代理列表（也就是增强列表）

当执行doProxyChain（）方法时 会按照顺序执行增强

最后再执行目标方法

```java
public class ProxyChain {

    private final Class<?> targetClass; //目标类
    private final Object targetObject; //目标对象
    private final Method targetMethod; //目标方法
    private final MethodProxy methodProxy; //方法代理
    private final Object[] methodParams; //方法参数

    private List<Proxy> proxyList = new ArrayList<>(); //代理列表
    private int proxyIndex = 0; //代理索引

    public ProxyChain(Class<?> targetClass, Object targetObject, Method targetMethod, MethodProxy methodProxy, Object[] methodParams, List<Proxy> proxyList) {
        this.targetClass = targetClass;
        this.targetObject = targetObject;
        this.targetMethod = targetMethod;
        this.methodProxy = methodProxy;
        this.methodParams = methodParams;
        this.proxyList = proxyList;
    }

    public Object[] getMethodParams() {
        return methodParams;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Method getTargetMethod() {
        return targetMethod;
    }

    /**
     * 递归执行
     */
    public Object doProxyChain() throws Throwable {
        Object methodResult;
        if (proxyIndex < proxyList.size()) {
            //执行增强方法
            methodResult = proxyList.get(proxyIndex++).deProxy(this);
        } else {
            //目标方法最后执行且只执行一次
            methodResult = methodProxy.invokeSuper(targetObject, methodParams);
        }
        return methodResult;
    }
}
```



### 2.2.3 AspectProxy类

AspectProxy是一个切面抽象类 实现了Proxy接口

类中定义了切入点判断和各种增强

当执行doProxy（）方法时 会先进行切入点判断

再执行 前置增强 代理链的下一个doProxyChain（）方法 后置增强等等

```java
public abstract class AspectProxy implements Proxy {

    private static final Logger logger = LoggerFactory.getLogger(AspectProxy.class);

    @Override
    public final Object doProxy(ProxyChain proxyChain) throws Throwable {
        Object result = null;

        Class<?> cls = proxyChain.getTargetClass();
        Method method = proxyChain.getTargetMethod();
        Object[] params = proxyChain.getMethodParams();

        begin();
        try {
            if (intercept(method, params)) {
                before(method, params);
                result = proxyChain.doProxyChain();
                after(method, params);
            } else {
                result = proxyChain.doProxyChain();
            }
        } catch (Exception e) {
            logger.error("proxy failure", e);
            error(method, params, e);
            throw e;
        } finally {
            end();
        }

        return result;
    }

    /**
     * 开始增强
     */
    public void begin() {
    }

    /**
     * 切入点判断
     */
    public boolean intercept(Method method, Object[] params) throws Throwable {
        return true;
    }

    /**
     * 前置增强
     */
    public void before(Method method, Object[] params) throws Throwable {
    }

    /**
     * 后置增强
     */
    public void after(Method method, Object[] params) throws Throwable {
    }

    /**
     * 异常增强
     */
    public void error(Method method, Object[] params, Throwable e) {
    }

    /**
     * 最终增强
     */
    public void end() {
    }
}
```



### 2.2.4 ProxyFactory类

这是一个代理工厂类 我们通过这个类来梳理上面的代理逻辑

当调用`ProxyFactory.createProxy（final Class<?> targetClass, final List proxyList）` 方法来创建一个代理对象后

每次执行方法时 都会调用intercept（）方法 从而创建一个ProxyChain对象

并调用该对象的doProxyChain（）方法 调用doProxyChain（）方法时 首先递归的执行增强

最后再执行目标方法

```java
public class ProxyFactory {
 
    /**
     * 输入一个目标类和一组Proxy接口实现, 输出一个代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(final Class<?> targetClass, final List<Proxy> proxyList) {
        return (T) Enhancer.create(targetClass, new MethodInterceptor() {
            /**
             * 代理方法, 每次调用目标方法时都会先创建一个 ProxyChain 对象, 然后调用该对象的 doProxyChain() 方法.
             */
            @Override
            public Object intercept(Object targetObject, Method targetMethod, Object[] methodParams, MethodProxy methodProxy) throws Throwable {
                return new ProxyChain(targetClass, targetObject, targetMethod, methodProxy, methodParams, proxyList).doProxyChain();
            }
        });
    }
}
```



### 2.2.5 AopHelper 助手类

AopHelper 助手类 用来初始化整个框架 逻辑如下：

框架中所有使用的Bean实例 都是从Bean容器中获取的 然后执行该实例的方法

基于此 初始化AOP框架实际上就是用代理对象覆盖掉Bean容器中的目标对象

这样根据目标类的Class对象从Bean容器中获取到的就是代理对象 从而达到目标对象增强的目的

```java
public final class AopHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AopHelper.class);

    static {
        try {
            //切面类-目标类集合的映射
            Map<Class<?>, Set<Class<?>>> aspectMap = createAspectMap();
            //目标类-切面对象列表的映射
            Map<Class<?>, List<Proxy>> targetMap = createTargetMap(aspectMap);
            //把切面对象织入到目标类中, 创建代理对象
            for (Map.Entry<Class<?>, List<Proxy>> targetEntry : targetMap.entrySet()) {
                Class<?> targetClass = targetEntry.getKey();
                List<Proxy> proxyList = targetEntry.getValue();
                Object proxy = ProxyFactory.createProxy(targetClass, proxyList);
                //覆盖Bean容器里目标类对应的实例, 下次从Bean容器获取的就是代理对象了
                BeanHelper.setBean(targetClass, proxy);
            }
        } catch (Exception e) {
            LOGGER.error("aop failure", e);
        }
    }

    /**
     * 获取切面类-目标类集合的映射
     */
    private static Map<Class<?>, Set<Class<?>>> createAspectMap() throws Exception {
        Map<Class<?>, Set<Class<?>>> aspectMap = new HashMap<Class<?>, Set<Class<?>>>();
        addAspectProxy(aspectMap);
        return aspectMap;
    }

    /**
     *  获取普通切面类-目标类集合的映射
     */
    private static void addAspectProxy(Map<Class<?>, Set<Class<?>>> aspectMap) throws Exception {
        //所有实现了AspectProxy抽象类的切面
        Set<Class<?>> aspectClassSet = ClassHelper.getClassSetBySuper(AspectProxy.class);
        for (Class<?> aspectClass : aspectClassSet) {
            if (aspectClass.isAnnotationPresent(Aspect.class)) {
                Aspect aspect = aspectClass.getAnnotation(Aspect.class);
                //与该切面对应的目标类集合
                Set<Class<?>> targetClassSet = createTargetClassSet(aspect);
                aspectMap.put(aspectClass, targetClassSet);
            }
        }
    }

    /**
     * 根据@Aspect定义的包名和类名去获取对应的目标类集合
     */
    private static Set<Class<?>> createTargetClassSet(Aspect aspect) throws Exception {
        Set<Class<?>> targetClassSet = new HashSet<Class<?>>();
        // 包名
        String pkg = aspect.pkg();
        // 类名
        String cls = aspect.cls();
        // 如果包名与类名均不为空，则添加指定类
        if (!pkg.equals("") && !cls.equals("")) {
            targetClassSet.add(Class.forName(pkg + "." + cls));
        } else if (!pkg.equals("")) {
            // 如果包名不为空, 类名为空, 则添加该包名下所有类
            targetClassSet.addAll(ClassUtil.getClassSet(pkg));
        }
        return targetClassSet;
    }

    /**
     * 将切面类-目标类集合的映射关系 转化为 目标类-切面对象列表的映射关系
     */
    private static Map<Class<?>, List<Proxy>> createTargetMap(Map<Class<?>, Set<Class<?>>> aspectMap) throws Exception {
        Map<Class<?>, List<Proxy>> targetMap = new HashMap<Class<?>, List<Proxy>>();
        for (Map.Entry<Class<?>, Set<Class<?>>> proxyEntry : aspectMap.entrySet()) {
            //切面类
            Class<?> aspectClass = proxyEntry.getKey();
            //目标类集合
            Set<Class<?>> targetClassSet = proxyEntry.getValue();
            //创建目标类-切面对象列表的映射关系
            for (Class<?> targetClass : targetClassSet) {
                //切面对象
                Proxy aspect = (Proxy) aspectClass.newInstance();
                if (targetMap.containsKey(targetClass)) {
                    targetMap.get(targetClass).add(aspect);
                } else {
                    //切面对象列表
                    List<Proxy> aspectList = new ArrayList<Proxy>();
                    aspectList.add(aspect);
                    targetMap.put(targetClass, aspectList);
                }
            }
        }
        return targetMap;
    }
}

```



### 2.2.6 HelperLoader类

在手写SpringMVC这篇博客里 我们定义了这个类 目的是为了集中加载`ClassHelper`、`BeanHelper`、`IocHelper`、`ControllerHelper`

这四个助手类 这里还要加上`AopHelper`类

```java
public final class HelperLoader {
    public static void init() {
        Class<?>[] classList = {
            ClassHelper.class,
            BeanHelper.class,
            AopHelper.class,
            IocHelper.class,
            ControllerHelper.class
        };
        for (Class<?> cls : classList) {
            ClassUtil.loadClass(cls.getName());
        }
    }
}

```



# 3.AOP实例

我们创建一个监控接口性能的切面

当接口调用后 打印接口的执行时间

## 3.1 业务类

```java
public interface IUserService {
    List<User> getAllUser();
}

@Service
public class UserService implements IUserService {
    /**
     * 获取所有用户
     */
    public List<User> getAllUser() {
        List<User> userList = new ArrayList<>();
        userList.add(new User(1, "Tom", 22));
        userList.add(new User(2, "Alic", 12));
        userList.add(new User(3, "Bob", 32));
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return userList;
    }
}
```



## 3.2 处理器

```java
@Controller
public class UserController {
    @Autowired
    private IUserService userService;

    /**
     * 用户列表
     * @return
     */
    @RequestMapping(value = "/userList", method = RequestMethod.GET)
    public View getUserList() {
        List<User> userList = userService.getAllUser();
        return new View("index.jsp").addModel("userList", userList);
    }
}
```



## 3.3 接口性能监控切面

目标类`com.tyshawn.controller `（这个地方切换成自己包 别弄错了 不然没效果的）包下的`UserController`

切入点getUserList()方法

日志记录切入点方法执行时间

```java
@Aspect(pkg = "com.tyshawn.controller", cls = "UserController")
public class EfficientAspect extends AspectProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(EfficientAspect.class);

    private long begin;

    /**
     * 切入点判断
     */
    @Override
    public boolean intercept(Method method, Object[] params) throws Throwable {
        return method.getName().equals("getUserList");
    }

    @Override
    public void before(Method method, Object[] params) throws Throwable {
        LOGGER.debug("---------- begin ----------");
        begin = System.currentTimeMillis();
    }

    @Override
    public void after(Method method, Object[] params) throws Throwable {
        LOGGER.debug(String.format("time: %dms", System.currentTimeMillis() - begin));
        LOGGER.debug("----------- end -----------");
    }
}

```



## 3.4 结果

```java
http://localhost:8081/handwritten/userList

---------- begin ----------
time: 1001ms
----------- end -----------

```



# 4.事务管理

我们要达到目的是 当我们在目标方法上加@Transactional注解后

该方法就拥有了事务管理

**用AOP实现的思路是** 

（1）前置增强为开启事务

（2）后置增强为提交事务

（3）异常增强为回滚事务

## 4.1 DatabaseHelper 助手类

DatabaseHelper为数据库操作助手类

可以通过助手类进行增删改查、事务等一系列的数据库操作

（一大波代码·· 做好心理准备啊···）

```java
public final class DatabaseHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    private static final ThreadLocal<Connection> CONNECTION_HOLDER;

    private static final QueryRunner QUERY_RUNNER;

    private static final BasicDataSource DATA_SOURCE;

    static {
        CONNECTION_HOLDER = new ThreadLocal<Connection>();

        QUERY_RUNNER = new QueryRunner();

        DATA_SOURCE = new BasicDataSource();
        DATA_SOURCE.setDriverClassName(ConfigHelper.getJdbcDriver());
        DATA_SOURCE.setUrl(ConfigHelper.getJdbcUrl());
        DATA_SOURCE.setUsername(ConfigHelper.getJdbcUsername());
        DATA_SOURCE.setPassword(ConfigHelper.getJdbcPassword());
    }

    /**
     * 获取数据源
     */
    public static DataSource getDataSource() {
        return DATA_SOURCE;
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() {
        Connection conn = CONNECTION_HOLDER.get();
        if (conn == null) {
            try {
                conn = DATA_SOURCE.getConnection();
            } catch (SQLException e) {
                LOGGER.error("get connection failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.set(conn);
            }
        }
        return conn;
    }

    /**
     * 开启事务
     */
    public static void beginTransaction() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.setAutoCommit(false);
            } catch (SQLException e) {
                LOGGER.error("begin transaction failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.set(conn);
            }
        }
    }

    /**
     * 提交事务
     */
    public static void commitTransaction() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.commit();
                conn.close();
            } catch (SQLException e) {
                LOGGER.error("commit transaction failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.remove();
            }
        }
    }

    /**
     * 回滚事务
     */
    public static void rollbackTransaction() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                LOGGER.error("rollback transaction failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.remove();
            }
        }
    }

    /**
     * 查询实体
     */
    public static <T> T queryEntity(Class<T> entityClass, String sql, Object... params) {
        T entity;
        try {
            Connection conn = getConnection();
            entity = QUERY_RUNNER.query(conn, sql, new BeanHandler<T>(entityClass), params);
        } catch (SQLException e) {
            LOGGER.error("query entity failure", e);
            throw new RuntimeException(e);
        }
        return entity;
    }

    /**
     * 查询实体列表
     */
    public static <T> List<T> queryEntityList(Class<T> entityClass, String sql, Object... params) {
        List<T> entityList;
        try {
            Connection conn = getConnection();
            entityList = QUERY_RUNNER.query(conn, sql, new BeanListHandler<T>(entityClass), params);
        } catch (SQLException e) {
            LOGGER.error("query entity list failure", e);
            throw new RuntimeException(e);
        }
        return entityList;
    }

    /**
     * 执行更新语句（包括：update、insert、delete）
     */
    public static int update(String sql, Object... params) {
        int rows;
        try {
            Connection conn = getConnection();
            rows = QUERY_RUNNER.update(conn, sql, params);
        } catch (SQLException e) {
            LOGGER.error("execute update failure", e);
            throw new RuntimeException(e);
        }
        return rows;
    }

    /**
     * 插入实体
     */
    public static <T> boolean insertEntity(Class<T> entityClass, Map<String, Object> fieldMap) {
        if (MapUtils.isEmpty(fieldMap)) {
            LOGGER.error("can not insert entity: fieldMap is empty");
            return false;
        }

        String sql = "INSERT INTO " + entityClass.getSimpleName();
        StringBuilder columns = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");
        for (String fieldName : fieldMap.keySet()) {
            columns.append(fieldName).append(", ");
            values.append("?, ");
        }
        columns.replace(columns.lastIndexOf(", "), columns.length(), ")");
        values.replace(values.lastIndexOf(", "), values.length(), ")");
        sql += columns + " VALUES " + values;

        Object[] params = fieldMap.values().toArray();

        return update(sql, params) == 1;
    }

    /**
     * 更新实体
     */
    public static <T> boolean updateEntity(Class<T> entityClass, long id, Map<String, Object> fieldMap) {
        if (MapUtils.isEmpty(fieldMap)) {
            LOGGER.error("can not update entity: fieldMap is empty");
            return false;
        }

        String sql = "UPDATE " + entityClass.getSimpleName() + " SET ";
        StringBuilder columns = new StringBuilder();
        for (String fieldName : fieldMap.keySet()) {
            columns.append(fieldName).append(" = ?, ");
        }
        sql += columns.substring(0, columns.lastIndexOf(", ")) + " WHERE id = ?";

        List<Object> paramList = new ArrayList<Object>();
        paramList.addAll(fieldMap.values());
        paramList.add(id);
        Object[] params = paramList.toArray();

        return update(sql, params) == 1;
    }

    /**
     * 删除实体
     */
    public static <T> boolean deleteEntity(Class<T> entityClass, long id) {
        String sql = "DELETE FROM " + entityClass.getSimpleName() + " WHERE id = ?";
        return update(sql, id) == 1;
    }
}
```



## 4.2 TransactionProxy类

TransactionProxy为事务的切面类 同样实现了Proxy接口

其doProxy（）方法是先判断代理方法上有没有@Transactional注解

如果有的话加上事务管理 没有的话就直接执行

```java
public class TransactionProxy implements Proxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionProxy.class);

    @Override
    public Object doProxy(ProxyChain proxyChain) throws Throwable {
        Object result;
        Method method = proxyChain.getTargetMethod();
        //加了@Transactional注解的方法要做事务处理
        if (method.isAnnotationPresent(Transactional.class)) {
            try {
                DatabaseHelper.beginTransaction();
                LOGGER.debug("begin transaction");
                result = proxyChain.doProxyChain();
                DatabaseHelper.commitTransaction();
                LOGGER.debug("commit transaction");
            } catch (Exception e) {
                DatabaseHelper.rollbackTransaction();
                LOGGER.debug("rollback transaction");
                throw e;
            }
        } else {
            result = proxyChain.doProxyChain();
        }
        return result;
    }
}

```



## 4.3 AopHelper类

前面的AOP部分我们已经知道了AopHelper的作用以及实现逻辑

事务代理比普通代理的差别是 我们默认会在所有的Service对象都被代理了

也就是说通过Service的Class对象 从Bean容器中得到的都是代理对象

我们在执行代理方法时 会判断目标方法上是否存在@Transactional注解

有就加上事务管理 没有就直接执行

如上面的代码 `TransactionProxy.doProxy()`

（哇·· 代码好长！）

```java
public final class AopHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AopHelper.class);

    static {
        try {
            //切面类-目标类集合的映射
            Map<Class<?>, Set<Class<?>>> aspectMap = createAspectMap();
            //目标类-切面对象列表的映射
            Map<Class<?>, List<Proxy>> targetMap = createTargetMap(aspectMap);
            //把切面对象织入到目标类中, 创建代理对象
            for (Map.Entry<Class<?>, List<Proxy>> targetEntry : targetMap.entrySet()) {
                Class<?> targetClass = targetEntry.getKey();
                List<Proxy> proxyList = targetEntry.getValue();
                Object proxy = ProxyFactory.createProxy(targetClass, proxyList);
                //覆盖Bean容器里目标类对应的实例, 下次从Bean容器获取的就是代理对象了
                BeanHelper.setBean(targetClass, proxy);
            }
        } catch (Exception e) {
            LOGGER.error("aop failure", e);
        }
    }

    /**
     * 获取切面类-目标类集合的映射
     */
    private static Map<Class<?>, Set<Class<?>>> createAspectMap() throws Exception {
        Map<Class<?>, Set<Class<?>>> aspectMap = new HashMap<Class<?>, Set<Class<?>>>();
        addAspectProxy(aspectMap);
        addTransactionProxy(aspectMap);
        return aspectMap;
    }

    /**
     *  获取普通切面类-目标类集合的映射
     */
    private static void addAspectProxy(Map<Class<?>, Set<Class<?>>> aspectMap) throws Exception {
        //所有实现了AspectProxy抽象类的切面
        Set<Class<?>> aspectClassSet = ClassHelper.getClassSetBySuper(AspectProxy.class);
        for (Class<?> aspectClass : aspectClassSet) {
            if (aspectClass.isAnnotationPresent(Aspect.class)) {
                Aspect aspect = aspectClass.getAnnotation(Aspect.class);
                //与该切面对应的目标类集合
                Set<Class<?>> targetClassSet = createTargetClassSet(aspect);
                aspectMap.put(aspectClass, targetClassSet);
            }
        }
    }

    /**
     *  获取事务切面类-目标类集合的映射
     */
    private static void addTransactionProxy(Map<Class<?>, Set<Class<?>>> aspectMap) {
        Set<Class<?>> serviceClassSet = ClassHelper.getClassSetByAnnotation(Service.class);
        aspectMap.put(TransactionProxy.class, serviceClassSet);
    }

    /**
     * 根据@Aspect定义的包名和类名去获取对应的目标类集合
     */
    private static Set<Class<?>> createTargetClassSet(Aspect aspect) throws Exception {
        Set<Class<?>> targetClassSet = new HashSet<Class<?>>();
        // 包名
        String pkg = aspect.pkg();
        // 类名
        String cls = aspect.cls();
        // 如果包名与类名均不为空，则添加指定类
        if (!pkg.equals("") && !cls.equals("")) {
            targetClassSet.add(Class.forName(pkg + "." + cls));
        } else if (!pkg.equals("")) {
            // 如果包名不为空, 类名为空, 则添加该包名下所有类
            targetClassSet.addAll(ClassUtil.getClassSet(pkg));
        }
        return targetClassSet;
    }

    /**
     * 将切面类-目标类集合的映射关系 转化为 目标类-切面对象列表的映射关系
     */
    private static Map<Class<?>, List<Proxy>> createTargetMap(Map<Class<?>, Set<Class<?>>> aspectMap) throws Exception {
        Map<Class<?>, List<Proxy>> targetMap = new HashMap<Class<?>, List<Proxy>>();
        for (Map.Entry<Class<?>, Set<Class<?>>> proxyEntry : aspectMap.entrySet()) {
            //切面类
            Class<?> aspectClass = proxyEntry.getKey();
            //目标类集合
            Set<Class<?>> targetClassSet = proxyEntry.getValue();
            //创建目标类-切面对象列表的映射关系
            for (Class<?> targetClass : targetClassSet) {
                //切面对象
                Proxy aspect = (Proxy) aspectClass.newInstance();
                if (targetMap.containsKey(targetClass)) {
                    targetMap.get(targetClass).add(aspect);
                } else {
                    //切面对象列表
                    List<Proxy> aspectList = new ArrayList<Proxy>();
                    aspectList.add(aspect);
                    targetMap.put(targetClass, aspectList);
                }
            }
        }
        return targetMap;
    }
}
```



# 5.事务管理实例

## 5.1 业务类

```java
public interface IUserService {
    List<User> getAllUser();

    User GetUserInfoById(Integer id);

    boolean updateUser(int id, Map<String, Object> fieldMap);
}

@Service
public class UserService implements IUserService {
    /**
     * 获取所有用户
     */
    public List<User> getAllUser() {
        String sql = "SELECT * FROM user";
        return DatabaseHelper.queryEntityList(User.class, sql);
    }

    /**
     * 根据id获取用户信息
     */
    public User GetUserInfoById(Integer id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        return DatabaseHelper.queryEntity(User.class, sql, id);
    }

    /**
     * 修改用户信息
     */
    @Transactional
    public boolean updateUser(int id, Map<String, Object> fieldMap) {
        return DatabaseHelper.updateEntity(User.class, id, fieldMap);
    }
}
```



## 5.2 处理器

```java
@Controller
public class UserController {
    @Autowired
    private IUserService userService;

    /**
     * 用户列表
     *
     * @return
     */
    @RequestMapping(value = "/userList", method = RequestMethod.GET)
    public View getUserList() {
        List<User> userList = userService.getAllUser();
        return new View("index.jsp").addModel("userList", userList);
    }

    /**
     * 用户详情
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/userInfo", method = RequestMethod.GET)
    public Data getUserInfo(Param param) {
        String id = (String) param.getParamMap().get("id");
        User user = userService.GetUserInfoById(Integer.parseInt(id));

        return new Data(user);
    }

    @RequestMapping(value = "/userEdit", method = RequestMethod.GET)
    public Data editUser(Param param) {
        String id = (String) param.getParamMap().get("id");
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("age", 911);
        userService.updateUser(Integer.parseInt(id), fieldMap);

        return new Data("Success.");
    }
}
```



## 5.3 JSP页面

```html
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="BASE" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <title>用户信息</title>
</head>
<body>
<h1>用户信息</h1>
<table>
    <tr>
        <th>用户id</th>
        <th>名称</th>
        <th>年龄</th>
    </tr>
    <c:forEach var="userinfo" items="${userList}">
        <tr>
            <td>${userinfo.id}</td>
            <td>${userinfo.name}</td>
            <td>${userinfo.age}</td>
            <td>
                <a href="${BASE}/userInfo?id=${userinfo.id}">详情</a>
                <a href="${BASE}/userEdit?id=${userinfo.id}">编辑</a>
            </td>
        </tr>
    </c:forEach>
</table>
</body>
</html>
```



## 5.4 结果

http://localhost:8081/handwritten/userList

（注意自己的项目地址 别搞错了）

```
点击编辑, 控制台打印: 
begin transaction
commit transaction
```



# 6.结束语

## 6.1 原作者的结束语

到此为止, handwritten-mvc-framwork 框架的所有功能都已实现, 包括Bean容器, IOC, MVC, AOP, 事务管理, 大家可以多看看源代码, 思路理顺之后一定会收获良多.

## 6.2 我的结束语

代码要想能一次性写出来 真的是难到不行 不过手敲这个环节还是少不了的 

一定要把所有的代码手敲一次 



每一个大的模块 比如完成Ioc、完成MVC、完成AOP 编写完后 记得丢到tomcat里去运行

我这里的方式是：通过maven打包成jar 将jar包导入到新项目中（有tomcat插件的项目）

接着编写完controller、service、serviceImpl、pojo等等 最后用maven build运行起来

（不懂的小伙伴可以想见 TestMySpringFramework 这个项目 我都打包好啦！）



抄代码也是一种学习吧！重要的是 你要手敲 全都手敲 最后要确保能运行起来

至于代码的整体逻辑·· 

emmm 慢慢品吧 还是挺难的 不过看不懂也没关系 先写出来 以后的事情以后再说！

`长风破浪会有时 直挂云帆济沧海` 

祝好！



















