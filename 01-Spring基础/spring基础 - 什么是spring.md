# Spring基础

Spring框架存在的意义就是为了降低耦合度, 根据不同的代码采用不同的方式, 通过IOC来降低主业务逻辑之间的耦合度, 通过AOP来降低系统级服务(如日志、安全、事务等)和主业务逻辑之间的耦合度. 此外还提供了一个Web层的框架Spring MVC.

吃水不忘挖井人 感谢[椰子Tyshawn](https://tyshawnlee.blog.csdn.net/)

文章来源：https://blog.csdn.net/litianxiang_kaola/article/details/79121561

（下面在原文基础上 进行了一些修改）



## 1.Spring容器的装配

在介绍Spring容器之前 先来介绍什么是bean 

简单来说 被Spring容器管理的对象就叫bean 比如Controller、Action、Service、Dao

```java
<bean id="userControllerId" class="org.a.IOC.UserController"></bean>
<bean id="userServiceId" class="org.a.IOC.UserServiceImpl"></bean>
<bean id="BookDaoId" class="org.a.IOC.UserDaoImpl"></bean>
```



### 1.1Spring容器中所有对象的实例化装配

有两种方式：

#### 1.1.1 BeanFactory

#### 1.1.2 ApplicationContext

#### 1.1.3 两者的关系

ApplicationContext是BeanFactory的一个子接口

Application在BeanFactory基础上补充了几个功能：

1）更容易与Spring的AOP特性集成

2）消息资源处理（用于国际化）

3）事件发布

4）应用程序层特定的上下文 如web应用程序中使用的WebApplicationContext



**对于上面两者的区别 需要记住的是：**

1）BeanFactory 采用的是延迟加载策略 第一次调用getBean()时 才去读配置信息 生成某个bean的实例

2）ApplicationContext在初始化时就会读取配置信息 生成所有bean的实例

3）上面两种特征导致的结果就是 如果配置信息有错 BeanFactory 在调用getBean时 才会抛出异常 而ApplicationContext在初始化的时候就会抛异常 帮助我们及时的检查配置是否正常

4）两种都支持BeanPostProcessor和BeanFactoryProcessor 但BeanFactory需要手动注册 而ApplicationContext是自动注册



### 1.2 配置元数据

Spring容器通过读取元数据来获取实例化、装配的对象、元数据的三种格式 分别是XML文件 java注解和java代码 

下面只介绍**XML格式**和**java注解格式**的元数据

#### 1.2.1 基于XML文件的元数据

ApplicationContext的两个实现类 ClassPathApplicationContext 和 FileSystemXmlApplicationContext用来加载xml格式的元数据

```xml
<beans>
    <bean id="BookServiceId" class="org.tyshawn.service.impl.BookServiceImpl">
        <property name="bookDao" ref="BookDaoId"></property>
    </bean>
    <bean id="BookDaoId" class="org.tyshawn.dao.Impl.BookDaoImpl"></bean>
</beans>
```



**ClassPathApplicationContext和FileSystemXmlApplication的区别**

ClassPathXmlApplicationContext是基于**类路径** 而 FileSystemXmlApplicationContext**基于文件的路径**

```java
ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
ApplicationContext context = new FileSystemXmlApplicationContext("D:/springtest/src/main/resources/spring/bean.xml");
```



#### 1.2.2 基于注解的元数据

ApplicationContext的实现类AnnotationConfigApplicationContext用来加载注解格式的元数据

```java
@Configuration
@ComponentScan(basePackages = "org.tyshawn")
public class AppConfig {
}
// main
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
```



## 2.IoC

IoC是控制反转 是一种思想 Ioc意味着是将你设计好的对象交给容器控制 而不是传统的在你的对象内部直接控制

首先通过对象实例上设置的属性来定义bean之间的依赖关系 然后Spring容器在创建的时候注入这些依赖项（DI）

**（DI其实就是IOC的另外一种说法）**



### 2.1 依赖注入

依赖注入有两种 分别是基于**构造方法**的注入和基于**Setter**方法的注入



#### 2.1.1 基于构造方法的注入

```java
public interface IBookDao {
    void insert();
}

public class BookDaoImpl implements IBookDao {
    @Override
    public void insert() {
        System.out.println("add book");
    }
}

public interface IBookService {
    void addBook();
}

public class BookServiceImpl implements IBookService {
    private IBookDao bookDao;
    public BookServiceImpl(IBookDao bookDao) {
        this.bookDao = bookDao;
    }

    @Override
    public void addBook() {
        this.bookDao.insert();
    }
}

<beans>
    <bean id="BookServiceId" class="org.tyshawn.service.impl.BookServiceImpl">
        <constructor-arg ref="BookDaoId"/>
    </bean>
    <bean id="BookDaoId" class="org.tyshawn.dao.Impl.BookDaoImpl"></bean>
</beans>

public class Test{
    public static void main(String[] args) throws ParseException {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        IBookService bookService = (IBookService) context.getBean("BookServiceId");
        bookService.addBook();
    }
}

```



#### 2.1.2基于Setter的方法注入

```java
public interface IBookDao {
    void insert();
}

public class BookDaoImpl implements IBookDao {
    @Override
    public void insert() {
        System.out.println("add book");
    }
}

public interface IBookService {
    void addBook();
}

public class BookServiceImpl implements IBookService {
    private IBookDao bookDao;
    public void setBookDao(IBookDao bookDao) {
        this.bookDao = bookDao;
    }

    @Override
    public void addBook() {
        this.bookDao.insert();
    }
}

<beans>
    <bean id="BookServiceId" class="org.tyshawn.service.impl.BookServiceImpl">
        <property name="bookDao" ref="BookDaoId"></property>
    </bean>
    <bean id="BookDaoId" class="org.tyshawn.dao.Impl.BookDaoImpl"></bean>
</beans>

public class Test{
    public static void main(String[] args) throws ParseException {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        IBookService bookService = (IBookService) context.getBean("BookServiceId");
        bookService.addBook();
    }
}
```



## 3.Bean作用域

### 3.1 作用域分类

| 作用域      | 描述                                                      |
| ----------- | --------------------------------------------------------- |
| singleton   | 默认. bean在每一个Spring容器内只有一个实例                |
| prototype   | 每次从Spring容器中获取到的bean都是一个新的实例            |
| request     | bean在每一个 HTTP Request 中只有一个实例, 只支持Web应用   |
| session     | bean在每一个 HTTP Session 中只有一个实例, 只支持Web应用   |
| application | bean在每一个 ServletContext 中只有一个实例, 只支持Web应用 |
| websocket   | bean在每一个 WebSocket 中只有一个实例, 只支持Web应用      |

示例（注意scope）

```java
<bean id="accountService" class="com.something.DefaultAccountService" scope="singleton"/>
<bean id="accountService" class="com.something.DefaultAccountService" scope="prototype"/>
<bean id="loginAction" class="com.something.LoginAction" scope="request"/>
<bean id="userPreferences" class="com.something.UserPreferences" scope="session"/>
<bean id="appPreferences" class="com.something.AppPreferences" scope="application"/>
```



### 3.2 作用域的区别

**singleton和application的区别**

1）在作用域为singleton时 bean在每一个Spring容器内只有一个实例 而应用可以有很多个容器

2）在作用域为application时 bean在整个应用中只有一个实例

3）作用域application只支持web应用

```java
//可以看到, 从两个容器中获取的bean不是同一个
public class Test{
    public static void main(String[] args) throws ParseException {
        ApplicationContext context1 = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        ApplicationContext context2 = new FileSystemXmlApplicationContext("D:\\springtest\\src\\main\\resources\\spring\\bean.xml");
        IBookService bookService1 = (IBookService) context1.getBean("BookServiceId");
        IBookService bookService2 = (IBookService) context2.getBean("BookServiceId");
        System.out.println(bookService1);
        System.out.println(bookService2);
    }
}

org.tyshawn.service.impl.BookServiceImpl@23faf8f2
org.tyshawn.service.impl.BookServiceImpl@2d6eabae
```



### 3.3 依赖多例bean依赖的单例bean

一个bean的作用域是singleton(单例) 而它的属性作用域是protype(每次都是一个新的bean)

```java
<bean id="BookServiceId" class="org.tyshawn.service.impl.BookServiceImpl" scope="singleton">
    <constructor-arg ref="BookDaoId"/>
</bean>
<bean id="BookDaoId" class="org.tyshawn.dao.Impl.BookDaoImpl" scope="prototype"></bean>
```

**我们想要的效果：**

每次获取BookService时都是同一个bean 而它的属性BookDaoId都是一个新的bean 

**实际的情况：**

但这种情况是不可能的 因为BookServiceId 只会实例化 装载一次 想要达到我们的期望的效果 需要使用**方法注入**

**解决方案：**

我们使用方法注入：Spring框架通过**CGLIB**字库的字节码生成器来动态生成覆盖方法的子类来实现此方法注入

```java
public class BookServiceImpl implements IBookService {
    private IBookDao bookDao;

    public IBookDao getBookDao() {
        return bookDao;
    }

    @Override
    public void addBook() {
        IBookDao bookDao = getBookDao();
        System.out.println(bookDao);
    }
}

<bean id="BookServiceId" class="org.tyshawn.service.impl.BookServiceImpl" scope="singleton">
    <lookup-method name="getBookDao" bean="BookDaoId"></lookup-method>
</bean>
<bean id="BookDaoId" class="org.tyshawn.dao.Impl.BookDaoImpl" scope="prototype"></bean>

public class Test{
    public static void main(String[] args) throws ParseException {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        BookServiceImpl bookService1 = (BookServiceImpl) context.getBean("BookServiceId");
        BookServiceImpl bookService2 = (BookServiceImpl) context.getBean("BookServiceId");
        bookService1.addBook();
        bookService2.addBook();
    }
}

org.tyshawn.dao.Impl.BookDaoImpl@6121c9d6
org.tyshawn.dao.Impl.BookDaoImpl@87f383f

```



## 4.Bean的生命周期

### 4.1 生命周期回调

#### 4.1.1 初始化回调

在spring容器将bean实例化 设置属性值之后将会执行初始化回调 初始化回调有两种设置方式

**方式一(推荐)**

```java

<bean id="exampleInitBean1" class="org.tyshawn.example.ExampleBean" init-method="init"/>

public class ExampleBean {
    public void init() {
        System.out.println("do some initialization work.");
    }
}
```

**方式二(不推荐)**

```java
<bean id="exampleInitBean2" class="org.tyshawn.example.AnotherExampleBean"/>
// 实现了InitializingBean接口
public class AnotherExampleBean implements InitializingBean {
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("do some initialization work.");
    }
}
```

**执行**

```java
public class Test{
    public static void main(String[] args) throws ParseException {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        ExampleBean example1 = (ExampleBean) context.getBean("exampleInitBean1");
        AnotherExampleBean example2 = (AnotherExampleBean) context.getBean("exampleInitBean2");
        System.out.println(example1);
        System.out.println(example2);
    }
}
```



#### 4.1.2 销毁回调

```java
方式一(推荐)
<bean id="exampleDestoryBean1" class="org.tyshawn.example.ExampleBean" destroy-method="destory"/>

public class ExampleBean {
    public void destroy() {
        System.out.println("do some destruction  work.");
    }
}
```



```java
方式二(不推荐)
<bean id="exampleDestoryBean2" class="org.tyshawn.example.AnotherExampleBean"/>

public class AnotherExampleBean implements DisposableBean {
    @Override
    public void destroy() {
        System.out.println("do some destruction  work.");
    }
}

public class Test{
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        ExampleBean example1 = (ExampleBean) context.getBean("exampleDestoryBean1");
        AnotherExampleBean example2 = (AnotherExampleBean) context.getBean("exampleDestoryBean2");
	  //当容器被关闭时, 容器内的bean就被销毁了
        context.registerShutdownHook();
    }
}
```



#### 4.1.3 初始化回调、销毁回调 同时配置

```java
<bean id="exampleDestoryBean2" class="org.tyshawn.example.AnotherExampleBean" destroy-method="cleanup"/>

public class AnotherExampleBean implements DisposableBean {
    @Override
    public void destroy() {
        System.out.println("do some destruction work.");
    }

    public void cleanup() {
        System.out.println("do some cleanup work.");
    }
}

public class Test{
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        AnotherExampleBean example2 = (AnotherExampleBean) context.getBean("exampleDestoryBean2");
        context.registerShutdownHook();
    }
}

do some destruction work.
do some cleanup work.

```

结果是两种方式都执行 DisposableBean/InitializingBean在前 destroy-method/init-method 在后



#### 4.1.4 启动回调 和 关闭回调

如果Spring容器中的bean实现了 Liftcycle接口 当Spring容器启动时 将会调用这些bean的start方法 当Spring容器关闭时 将会调用这些bean的stop方法

```java
public interface Lifecycle {
    void start();
    void stop();
    boolean isRunning();
}
```

在很多情况下 start方法和stop方法的调用顺序是重要的 

如果两个bean存在依赖关系 比如a依赖b 这是a先调用start方法 b先调用stop方法 但如果我们不知道依赖关系 却想让a在b之前调用start方法 

我们就可以用**SmartLifecycle**接口代替**Lifecycle**接口

```java
public interface Phased {
    int getPhase();
}

public interface SmartLifecycle extends Lifecycle, Phased {
    boolean isAutoStartup();
    void stop(Runnable callback);
}
```



**SmartLifecycle接口的方法介绍**

1）当isAutoStartup 返回true Spring容器启动时会调用bean的start方法

2）当isRunning 返回true Spring 容器销毁时会调用bean的stop方法

3）getPhase返回的是优先级 当有多个bean时 返回值最大的先执行start方法 销毁时顺序相反 容器内没有实现SmartLifecycle接口 而实现Lifecycle接口的bean返回值是0 负数代表的是最高优先级



```java
  <bean id="exampleDestoryBean" class="org.tyshawn.example.ExampleBean" />

  public class ExampleBean implements SmartLifecycle {
      private boolean isRunning = false;
  
      @Override
      public boolean isAutoStartup() {
          return true;
      }
  
      @Override
      public void stop(Runnable runnable) {
          runnable.run();
          System.out.println("stop runnable ...");
      }
  
      @Override
      public void start() {
          isRunning = true;
          System.out.println("start run ...");
      }
  
      @Override
      public void stop() {
          System.out.println("stop run ...");
      }
  
      @Override
      public boolean isRunning() {
          return isRunning;
      }
  
      @Override
      public int getPhase() {
          return -1;
      }
  }
  
  public class Test{
      public static void main(String[] args) throws Exception {
          ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
          ExampleBean example = (ExampleBean) context.getBean("exampleDestoryBean");
          System.out.println(example);
          context.registerShutdownHook();
      }
  }
  
  start run ...
  org.tyshawn.example.ExampleBean@1b26f7b2
  stop runnable ...

```



#### 4.1.5 各种回调的执行顺序

在了解初始化回调 销毁回调 和 启动 关闭 回调之后 

如果它们同时存在时 它们的执行顺序是怎么样的

```java
<bean id="exampleBean" class="org.tyshawn.example.ExampleBean" init-method="init" destroy-method="destroy"/>

public class ExampleBean implements SmartLifecycle{
    private boolean isRunning = false;

    public void init() {
        System.out.println("do some init work.");
    }

    public void destroy() {
        System.out.println("do some destruction  work.");
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable runnable) {
        runnable.run();
        System.out.println("stop runnable ...");
    }

    @Override
    public void start() {
        isRunning = true;
        System.out.println("start run ...");
    }

    @Override
    public void stop() {
        System.out.println("stop run ...");
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPhase() {
        return -1;
    }
}

public class Test{
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        ExampleBean example1 = (ExampleBean) context.getBean("exampleBean");
        context.registerShutdownHook();
    }
}

do some init work.
start run ...
stop runnable ...
do some destruction work.

```

可以看出 容器启动时 先执行初始化回调 再执行启动回调 容器关闭时 先执行关闭回调 再执行销毁回调



### 4.2 XAware 感知

spring提供了一系列以Ware结尾的接口 翻译为XXX可感知 

作用是如果某个bean需要Spring提供一些特定的依赖 就实现对应的XXX Aware接口

#### 4.2.1 常用的Aware

| 接口                    | 作用                       |
| ----------------------- | -------------------------- |
| BeanNameAware           | 提供声明bean时的id         |
| BeanFactoryAware        | 提供BeanFactory容器        |
| ApplicationContextAware | 提供ApplicationContext容器 |



##### 4.2.1.1 BeanNameAware

```java
<bean id="exampleDestoryBean" class="org.tyshawn.example.ExampleBean" />

public class ExampleBean implements BeanNameAware{

    @Override
    public void setBeanName(String beanName) {
        System.out.println("beanName: " + beanName);
    }
}

public class Test{
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        ExampleBean example = (ExampleBean) context.getBean("exampleDestoryBean");
    }
}

beanName: exampleDestoryBean

```



##### 4.2.1.2 BeanFactoryAware

```java
<bean id="exampleBean" class="org.tyshawn.example.ExampleBean" />

public class AnotherExampleBean implements BeanFactoryAware {

    @Override
    public void setFactory(BeanFactory factory) throws BeansException {
        System.out.println("factory: " + factory);
    }
}

public class Test{
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        ExampleBean example = (ExampleBean) context.getBean("exampleBean");
    }
}

BeanFactory: org.springframework.beans.factory.support.DefaultListableBeanFactory@2344fc66: defining beans [exampleBean]; root of factory hierarchy

```



##### 4.2.1.3 ApplicationContextAware

```java
<bean id="exampleBean" class="org.tyshawn.example.AnotherExampleBean" />

public class AnotherExampleBean implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("ApplicationContext: " + applicationContext);
    }
}

public class Test{
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        AnotherExampleBean example = (AnotherExampleBean) context.getBean("exampleBean");
    }
}

ApplicationContext: org.springframework.context.support.ClassPathXmlApplicationContext@573fd745: startup date [Thu Apr 18 18:59:43 CST 2019]; root of context hierarchy

```



### 4.3 BeanPostProcessor

如果想在Spring容器完成bean的初始化前后加一些特定的逻辑 可以向容器注册一个或多个定制BeanPostProcessor实现 当有多个BeanPostProcessor定制时 我们同时要实现Ordered接口

```
<bean id="exampleBean" class="org.tyshawn.example.AnotherExampleBean" init-method="init"/>
<bean class="org.tyshawn.example.CustomBeanPostProcessor1"/>
<bean class="org.tyshawn.example.CustomBeanPostProcessor2"/>

public class CustomBeanPostProcessor1 implements BeanPostProcessor, Ordered {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("bean执行init()方法之前的定制逻辑1.");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("bean执行init()方法之后的定制逻辑1.");
        return bean;
    }

    @Override
    public int getOrder() {
        return 2;
    }
}

public class CustomBeanPostProcessor2 implements BeanPostProcessor, Ordered {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("bean执行init()方法之前的定制逻辑2.");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("bean执行init()方法之后的定制逻辑2.");
        return bean;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}

public class AnotherExampleBean {
    public void init() {
        System.out.println("init() ...");
    }
}

public class Test{
    public static void main(String[] args) throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        AnotherExampleBean example = (AnotherExampleBean) context.getBean("exampleBean");
    }
}

bean执行init()方法之前的定制逻辑2.
bean执行init()方法之前的定制逻辑1.
init() ...
bean执行init()方法之后的定制逻辑2.
bean执行init()方法之后的定制逻辑1.

```



### 4.4 Bean的生命周期总结

**bean在spring容器中的生命周期**

1） 实例化

2）设置属性值

3）调用BeanNameAware的setBeanName方法

4）调用BeanFactoryAware的setBeanFactory方法

5）调用ApplicationContext的setApplicationContext的方法

6）调用BeanPostProcessor的PostBeforeInitialization方法

7）调用InitializingBean的afterPropertiesSet方法

8）调用xml配置的初始化方法

9）调用BeanPostProcessor的postProcessAfterInitialization方法

10）容器启动

11）bean可以使用了

12）容器关闭

13）调用DisposableBean的destory方法

14）调用xml配置的销毁方法



```java
<bean id="exampleBean" class="org.tyshawn.example.ExampleBean" init-method="init" destroy-method="dest"/>
<bean class="org.tyshawn.example.CustomBeanPostProcessor"/>

public class CustomBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("调用BeanPostProcessor的postProcessBeforeInitialization()方法");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("调用BeanPostProcessor的postProcessAfterInitialization()方法");
        return bean;
    }
}

public class ExampleBean implements InitializingBean, DisposableBean, SmartLifecycle, BeanNameAware, BeanFactoryAware, ApplicationContextAware {
    private boolean isRunning = false;

    public void init() {
        System.out.println("调用xml配置的初始化方法");
    }

    public void dest() {
        System.out.println("调用xml配置的销毁方法");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("调用InitializingBean的afterPropertiesSet()方法");
    }

    @Override
    public void destroy() {
        System.out.println("调用DisposableBean的destory()方法");
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable runnable) {
        runnable.run();
        System.out.println("容器关闭.");
    }

    @Override
    public void start() {
        isRunning = true;
        System.out.println("容器启动.");
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPhase() {
        return -1;
    }

    @Override
    public void setBeanName(String beanName) {
        System.out.println("调用BeanNameAware的setBeanName()方法");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("调用BeanFactoryAware的setBeanFactory()方法");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("调用ApplicationContext的setApplicationContext()方法");
    }

    public void sayHello() {
        System.out.println("bean可以使用了.");
    }
}

public class Test{
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext  context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
        ExampleBean example = (ExampleBean) context.getBean("exampleBean");
        example.sayHello();
        context.registerShutdownHook();
    }
}

调用BeanNameAware的setBeanName()方法
调用BeanFactoryAware的setBeanFactory()方法
调用ApplicationContext的setApplicationContext()方法
调用BeanPostProcessor的postProcessBeforeInitialization()方法
调用InitializingBean的afterPropertiesSet()方法
调用xml配置的初始化方法
调用BeanPostProcessor的postProcessAfterInitialization()方法
容器启动.
bean可以使用了.
容器关闭.
调用DisposableBean的destory()方法
调用xml配置的销毁方法
```



## 5.FactoryBean

### 5.1 FactoryBean和BeanFactory的区别 

BeanFactory是一个Spring容器 应用中所有bean的实例都存储在其中

FactoryBean是一个生成的Bean的工厂 一般情况下都是用XML文件来配置Bean 但如果有复杂的初始化逻辑 相对于冗长的xml 用java代码可以更好地表达 这时可以创建自己的FactoryBean 在该类中编写复杂的初始化逻辑 然后将定制的FactoryBean插入容器中

```java
public interface FactoryBean<T> {
    T getObject() throws Exception; 

    Class<?> getObjectType();

    boolean isSingleton();
}
```



### 5.2 FactoryBean接口的方法

1) getObject 返回工厂创建的对象的实例

2) getObjectType 返回对象类型

3) isSingleton 是否为单例

```java
  <bean id="tool" class="org.tyshawn.example.ToolFactory">
      <property name="toolName" value="iphone xs"/>
  </bean>	
  
  public class Tool {
      private String name;
  
      public Tool(String name) {
          this.name = name;
      }
  
      public String getName() {
          return name;
      }
  
      public void setName(String name) {
          this.name = name;
      }
  }
  
  public class ToolFactory implements FactoryBean<Tool> {
      private String toolName;
  
      @Override
      public Tool getObject() throws Exception {
          return new Tool(toolName);
      }
  
      @Override
      public Class<?> getObjectType() {
          return Tool.class;
      }
  
      @Override
      public boolean isSingleton() {
          return false;
      }
  
      public String getToolName() {
          return toolName;
      }
  
      public void setToolName(String toolName) {
          this.toolName = toolName;
      }
  }
  
  public class Test{
      public static void main(String[] args) throws Exception {
          ConfigurableApplicationContext  context = new ClassPathXmlApplicationContext("classpath:spring/bean.xml");
          Tool tool = (Tool) context.getBean("tool");
          System.out.println(tool.getName());
      }
  }
  
  iphone xs	

```



### 5.3 容器配置

#### 5.3. 1 注解和XML进行容器配置

@ComponentScan用来配置类扫描路径

```java
@Configuration
@ComponentScan(basePackages = "org.example")
public class AppConfig  {
}
```

上述等同与 XML配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">
    <context:component-scan base-package="org.example"/>
</beans>
```



#### 5.3.2 声明bean

在基于xml配置中 下面代码生成了一个bean

```java
<bean id="bookDaoId" class="org.tyshawn.dao.Impl.BookDaoImpl"></bean>
```



在基于注解的容器配置中 可以用注解声明一个bean

`@Component` 声明一个bean

`@Controller` 声明一个web层的bean

`@Service` 声明一个service层的bean

`@Repository` 声明一个DAO层的bean



#### 5.3.3 自动注入属性

@Autowired 为按类型注入属性

如果注入属性有多个时 需要通过`@Qualifier`指定名称

`@Reource=@Autowired + @Qualifier`

```java
@Configuration
@ComponentScan(basePackages = "org.tyshawn")
public class AppConfig {
}

@Repository("bookDao")
public class BookDaoImpl implements IBookDao {
    @Override
    public void insert() {
        System.out.println("add book");
    }
}

@Service("bookService")
public class BookServiceImpl implements IBookService {
//    @Autowired
//    @Qualifier(value = "bookDao")
    @Resource(name = "bookDao")
    private IBookDao bookDao;

    @Override
    public void addBook() {
        bookDao.insert();
    }
}

public class Test{

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        IBookService bookService = context.getBean("bookService", BookServiceImpl.class);
        bookService.addBook();
    }
}
add book

```



#### 5.3.4 作用域

`@Scope("singleton")`

`@Scope("prototype")`

`@RequestScope`

`@SessionScope`

`@ApplicationScope`

```java
  @Configuration
  @ComponentScan(basePackages = "org.tyshawn")
  public class AppConfig {
  }
  
  @Component("exampleBean")
  @Scope("prototype")
  public class ExampleBean {
  }
  
  public class Test{
      public static void main(String[] args) {
          ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
          ExampleBean exampleBean1 = context.getBean("exampleBean", ExampleBean.class);
          ExampleBean exampleBean2 = context.getBean("exampleBean", ExampleBean.class);
          System.out.println(exampleBean1);
          System.out.println(exampleBean2);
      }
  }
  org.tyshawn.example.ExampleBean@64c87930
  org.tyshawn.example.ExampleBean@400cff1a

```



#### 5.3.5 生命周期回调

`@PostConstruct` 初始化回调

`@PreDestroy` 销毁回调

```java
  @Configuration
  @ComponentScan(basePackages = "org.tyshawn")
  public class AppConfig {
  }
  
  @Component("exampleBean")
  public class ExampleBean {
      @PostConstruct
      public void init() {
          System.out.println("初始化方法");
      }
  
      @PreDestroy
      public void destroy() {
          System.out.println("销毁方法");
      }
  }
  
  public class Test{
  
      public static void main(String[] args) {
          AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
          ExampleBean exampleBean = context.getBean("exampleBean", ExampleBean.class);
          System.out.println(exampleBean);
          context.registerShutdownHook();
      }
  }
  
  初始化方法
  org.tyshawn.example.ExampleBean@5d47c63f
  销毁方法

```





至此spring基础的一些内容结束

内容好多·· 我哭了···



































