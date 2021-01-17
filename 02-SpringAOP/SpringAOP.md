

# SpringAOP

吃水不忘挖井人 感谢[椰子Tyshawn](https://tyshawnlee.blog.csdn.net/)

文章来源：https://blog.csdn.net/litianxiang_kaola/article/details/79121561

（下面在原文基础上 进行了一些修改）



# 1.什么是AOP

AOP(Aspect-oriented Programming) , 名字与OOP(Object-oriented programming) 仅差一个字母, 其实它是对OOP编程的一种补充. 

AOP翻译过来叫面向切面编程, 核心就是这个切面. 

切面表示从业务逻辑中分离出来的横切逻辑, 比如性能监控, 日志记录, 权限控制等, 这些功能都可以从核心业务逻辑代码中抽离出来.

 也就是说, 通过AOP可以解决代码耦合问题, 让职责更加单一.

![](https://github.com/wdkang123/MySpringFramework/blob/main/images/aop01.png?raw=true)

## 1.1转账业务

```java
public interface IAccountService {
    //主业务逻辑: 转账
    void transfer();
}
public class AccountServiceImpl implements IAccountService {
    @Override
    public void transfer() {
        System.out.println("调用dao层,完成转账主业务.");
    }
}
```



## 1.2 先验证身份再转账

```java
public class AccountServiceImpl implements IAccountService {
    @Override
    public void transfer() {
	    System.out.println("对转账人身份进行验证.");
        System.out.println("调用dao层,完成转账主业务.");
    }
}
```



## 1.3 将业务剥离

```java
public class AccountProxy implements IAccountService {
    //目标对象
    private IAccountService target;

    public AccountProxy(IAccountService target) {
        this.target = target;
    }

    /**
     * 代理方法,实现对目标方法的功能增强
     */
    @Override
    public void transfer() {
        before();
        target.transfer();
    }

    /**
     * 身份验证
     */
    private void before() {
        System.out.println("对转账人身份进行验证.");
    }
}

public class Client {
    public static void main(String[] args) {
        //创建目标对象
        IAccountService target = new AccountServiceImpl();
        //创建代理对象
        AccountProxy proxy = new AccountProxy(target);
        proxy.transfer();
    }
}
```



上边这是静态代理 SpringAOP的功能是通过更加强大的动态代理实现的



# 2.AOP术语

- Targe
  目标对象
- Joinpoint
  连接点, 所有可能被增强的方法都是连接点.
- Pointcut
  切入点, 将被增强的方法.
- **Advice**
  增强, 从主业务逻辑中剥离出来的横切逻辑.
- Aspect
  切面, 切入点加上增强就是切面.
- Weaving
  织入, 把切面应用到目标对象上的过程.
- Proxy
  代理对象, 被增强过的目标对象.



**Advice常见类型**

- 前置增强
  org.springframework.aop.MethodBeforeAdvice, 在目标方法执行前实施增强.
- 后置增强
  org.springframework.aop.AfterReturningAdvice, 在目标方法执行后实施增强.
- 环绕增强
  org.aopalliance.intercept.MethodInterceptor, 在目标方法执行前后都实施增强.
- 异常抛出增强
  org.springframework.aop.ThrowsAdvice, 在方法抛出异常后实施增强.
- 引入增强
  org.springframework.aop.IntroductionInterceptor, 对类进行增强, 即在目标类中添加一些新的方法和属性.



# 3.SpringAOP 编程式

下面例子采用的是环绕增强

## 3.1 业务类

```java
public interface IAccountService {
    //主业务逻辑: 转账
    void transfer();
}
public class AccountServiceImpl implements IAccountService {
    @Override
    public void transfer() {
        System.out.println("调用dao层,完成转账主业务.");
    }
}
```



## 3.2 环绕增强

```java
public class AccountAdvice implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        before();
        Object result = invocation.proceed();
        after();
        return result;
    }

    private void before() {
        System.out.println("Before");
    }

    private void after(){
        System.out.println("After");
    }
}
```



## 3.3 测试代码

```java
public class Test {
    public static void main(String[] args) {
        //创建代理工厂
        ProxyFactory proxyFactory = new ProxyFactory();
        //配置目标对象
        proxyFactory.setTarget(new AccountServiceImpl());
        //配置增强
        proxyFactory.addAdvice(new AccountAdvice());

        IAccountService proxy = (IAccountService) proxyFactory.getProxy();
        proxy.transfer();
    }
}

结果:
Before
调用dao层,完成转账主业务.
After

```



# 4.SpringAOP 声明式

## 4.1 业务类

```java
public interface IAccountService {
    //主业务逻辑: 转账
    void transfer();
}
public class AccountServiceImpl implements IAccountService {
    @Override
    public void transfer() {
        System.out.println("调用dao层,完成转账主业务.");
    }
}
```



## 4.2 环绕增强

```java
public class AccountAdvice implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        before();
        Object result = invocation.proceed();
        after();
        return result;
    }

    private void before() {
        System.out.println("Before");
    }

    private void after(){
        System.out.println("After");
    }
}
```



## 4.3 配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx.xsd">

    <!--声明bean-->
    <bean id="accountService" class="org.service.impl.AccountServiceImpl"></bean>
    <bean id="accountAdvice" class="org.aspect.AccountAdvice"></bean>

    <!--配置代理工厂-->
    <bean id="accountProxy" class="org.springframework.aop.framework.ProxyFactoryBean">
        <!--目标接口-->
        <property name="interfaces" value="org.service.IAccountService"/>
        <!--目标对象-->
        <property name="target" ref="accountService"/>
        <!--增强-->
        <property name="interceptorNames" value="accountAdvice"/>
    </bean>
</beans>
```



## 4.4 测试代码

```java
public class Test {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/spring-service.xml");
        IAccountService proxy = (IAccountService) context.getBean("accountProxy");
        proxy.transfer();
    }
}

结果: 
Before
调用dao层,完成转账主业务.
After

```



# 5.SpringAOP 切面

前面的编程式和声明式都没有用到切面 如果我们需要针对某一个类进行增强 那么就可以使用切面来解决这个问题

## 5.1 配置切面(正则)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx.xsd">

    <!--声明bean-->
    <bean id="accountService" class="org.service.impl.AccountServiceImpl"></bean>
    <bean id="accountAdvice" class="org.aspect.AccountAdvice"></bean>

    <!--配置切面-->
    <bean id="accountAspect" class="org.springframework.aop.support.RegexpMethodPointcutAdvisor">
        <!--增强-->
        <property name="advice" ref="accountAdvice"/>
        <!--切入点-->
        <property name="pattern" value="org.service.impl.AccountServiceImpl.transfer.*"/>
    </bean>


    <!--配置代理-->
    <bean id="accountProxy" class="org.springframework.aop.framework.ProxyFactoryBean">
        <!--目标对象-->
        <property name="target" ref="accountService"/>
        <!--切面-->
        <property name="interceptorNames" value="accountAspect"/>
    </bean>
</beans>
```



这里配置的切面是基于郑泽斌表达式的`RegexpMethodPointcutAdvisor` 拦截所有`transfer`开头的方法

## 5.2 配置切面(其他)

除此之外 SpringAOP还提供了以下的配置：

- org.springframework.aop.support.DefaultPointcutAdvisor
  匹配继承了该类的切面.
- org.springframework.aop.support.NameMatchMethodPointcutAdvisor
  根据方法名称进行匹配.
- org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor
  用于匹配静态方法.



# 6.SpringAOP 自动代理

用户自己配置一个或几个代理 还是可以的

但是随着项目的扩大 代理配置会越来越多 这个时候再去手动配置 那是相当的麻烦的

所以SpringAOP提供了自动代理的功能、

## 6.1 配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx.xsd">
    <!--声明bean-->
    <bean id="accountService" class="org.service.impl.AccountServiceImpl"></bean>
    <bean id="accountAdvice" class="org.aspect.AccountAdvice"></bean>

    <!--配置切面-->
    <bean id="accountAspect" class="org.springframework.aop.support.RegexpMethodPointcutAdvisor">
        <!--增强-->
        <property name="advice" ref="accountAdvice"/>
        <!--切入点-->
        <property name="pattern" value="org.service.impl.AccountServiceImpl.transfer.*"/>
    </bean>

    <!--配置自动代理: 自动扫描所有切面类, 并为其生成代理-->
    <bean class="org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator"/>
</beans>
```



## 6.2 测试代码

```java
public class Test{
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/spring-service.xml");
        IAccountService proxy = (IAccountService) context.getBean("accountService");
        proxy.transfer();
    }
}

结果:
Before
调用dao层,完成转账主业务.
After
```



# 7.Spring + AspectJ

在企业开发中几乎不使用Spring的AOP功能 而是用AspectJ代替

Spring在后期也集成了AspectJ 间接证明了它的强大

这里我们看看Spring + AspectJ



## 7.1 AspectJ 增强类型

- 前置增强
  注解: @Before, 配置: < aop:before>
- 后置增强
  注解: @After, 配置: < aop:after>
- 环绕增强
  注解: @Around, 配置: < aop:around>
- 异常抛出增强
  注解: @AfterThrowing, 配置: < aop:after-throwing>
- 引入增强
  注解: @DeclareParents, 配置: < aop:declare-parents>



## 7.2 切入点表达式

```java
execution(* org.service.impl.AccountServiceImpl.*(..))
```

- execution()表示拦截方法, 括号中可定义需要匹配的规则.
- 第一个 “*” 表示方法的返回值是任意的.
- 第二个 “*” 表示匹配该类中所有的方法.
- (…) 表示方法的参数是任意的.



## 7.3 实例 基于配置

业务类

```java
public interface IAccountService {
    //主业务逻辑: 转账
    void transfer();
}
public class AccountServiceImpl implements IAccountService {
    @Override
    public void transfer() {
        System.out.println("调用dao层,完成转账主业务.");
    }
}
```

增强

```java
public class AccountAdvice{
    //前置增强
    public void myBefore(JoinPoint joinPoint){
        before();
    }

    //后置增强
    public void myAfter(JoinPoint joinPoint) {
        after();
    }

    //环绕增强
    public Object myAround(ProceedingJoinPoint joinPoint) throws Throwable{
        before();
        Object result  = joinPoint.proceed();
        after();
        return result;
    }

    //抛出异常增强
    public void myThrowing(JoinPoint joinPoint, Throwable e) {
        System.out.println("抛出异常增强： " + e.getMessage());
    }


    private void before() {
        System.out.println("Before");
    }

    private void after(){
        System.out.println("After");
    }
}
```

配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx.xsd">
    <!--声明bean-->
    <bean id="accountService" class="org.service.impl.AccountServiceImpl"></bean>
    <bean id="accountAdvice" class="org.aspect.AccountAdvice"></bean>

    <!--切面-->
    <aop:config>
        <aop:aspect ref="accountAdvice">
            <!--切入点表达式-->
            <aop:pointcut expression="execution(* org.service.impl.AccountServiceImpl.*(..))" id="myPointCut"/>
            <!--环绕增强-->
            <aop:around method="myAround" pointcut-ref="myPointCut"/>
        </aop:aspect>
    </aop:config>
</beans>
```

测试

```java
public class Test{
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/spring-service.xml");
        IAccountService proxy = (IAccountService) context.getBean("accountService");
        proxy.transfer();
    }
}

结果: 
Before
调用dao层,完成转账主业务.
After
```



## 7.4 实例 基于注解

业务类

```java
public interface IAccountService {
    //主业务逻辑: 转账
    void transfer();
}
@Component
public class AccountServiceImpl implements IAccountService {
    @Override
    public void transfer() {
        System.out.println("调用dao层,完成转账主业务.");
    }
}
```

切面

```java
@Component
@Aspect
public class AccountAspect{
    //切入点
    @Pointcut("execution(* org.tyshawn.service.impl.AccountServiceImpl.*(..))")
    private void pointCut(){};

    //前置增强
    @Before("pointCut()")
    public void myBefore(JoinPoint joinPoint){
        before();
    }

    //后置增强
    @After("pointCut()")
    public void myAfter(JoinPoint joinPoint) {
        after();
    }

    //环绕增强
    @Around("pointCut()")
    public Object myAround(ProceedingJoinPoint joinPoint) throws Throwable{
        before();
        Object result  = joinPoint.proceed();
        after();
        return result;
    }

    //抛出异常增强
    @AfterThrowing(value = "pointCut()", throwing = "e")
    public void myThrowing(JoinPoint joinPoint, Throwable e) {
        System.out.println("抛出异常增强： " + e.getMessage());
    }

    private void before() {
        System.out.println("Before");
    }

    private void after(){
        System.out.println("After");
    }
}
```

配置

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd
        http://www.springframework.org/schema/tx
        http://www.springframework.org/schema/tx/spring-tx.xsd">

    <!--注解扫描-->
    <context:component-scan base-package="org.tyshawn"></context:component-scan>
    <!--自动代理 -->
    <aop:aspectj-autoproxy></aop:aspectj-autoproxy>
</beans>
```

测试

```
public class Test{
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/spring-service.xml");
        IAccountService proxy = (IAccountService) context.getBean("accountServiceImpl");
        proxy.transfer();
    }
}
结果: 
Before
Before
调用dao层,完成转账主业务.
After
After

```































