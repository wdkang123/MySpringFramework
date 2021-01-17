# Spring事务管理



# 1.事务的基础

## 1.1 事务的特性 - ACID

- 原子性(atomicity)
  一个事务必须被视为一个不可分割的最小工作单元, 整个事务中的所有操作要么全部执行成功, 要么全部失败回滚, 对于一个事务来说, 不可能只执行其中的一部分操作.
- 一致性(consistency)
  事务必须使数据库从一个一致性的状态变换到另一个一致性的状态, 也就是说一个事务执行之前和执行之后都必须处于一致性的状态. 拿转账来说, 假设用户A和用户B两者的钱加起来一共是5000, 那么不管A和B之间如何转账, 转几次账, 事务结束后两个用户的钱相加起来应该还得是5000, 这就是事务的一致性.
- 隔离性(isolation)
  并发的事务之间是相互隔离的, 一个事务所做的修改在最终提交之前, 对其他事务是不可见的. 例如, 账户A有5000元存款, 执行完转账语句(-500), 只要该事务没有提交, 对其他事务来说账户余额还是5000元.
- 持久性(durability)
  事务一旦提交, 其对数据库的修改就是永久性的, 即使系统崩溃, 修改的数据也不会丢失.



## 1.2 并发的问题 - LDNO

- 更新丢失(Lost Update)
  多个事务修改同一行记录(都未提交), 后面的修改覆盖了前面的修改.
- 脏读(Dirty Reads)
  一个事务可以读取另一个事务未提交的数据.
- 不可重复读(Non-Repeatable Reads)
  同一个事务中执行两次相同的查询, 可能得到不一样的结果. 这是因为在查询间隔内，另一个事务修改了该记录并提交了事务.
- 幻读(Phantom Reads)
  当某个事务在读取某个范围内的记录时, 另一个事务又在该范围内插入了新的记录, 当之前的事务再次读取该范围的记录时, 会产生幻行.



## 1.3 隔离的级别 - UCPS

在MySQL常用的存储引擎中 

**只有InnoDB支持事务** 

所以这里说的隔离级别指的是InnoDB下的事务隔离级别

- READ UNCOMMITTED(读未提交)
  在该隔离级别, 事务中的修改即使没有提交, 对其他事务也都是可见的. 避免了更新丢失的发生.
- READ COMMITTED(读已提交)
  在该隔离级别, 一个事务只能看见已经提交的事务所做的修改. 避免了更新丢失和脏读.
- REPEATABLE READ(可重复读)
  MySQL默认的隔离级别, 该级别保证了在同一个事务中多次读取同样的记录的结果是一致的. 避免了更新丢失、脏读、不可重复读和幻读. (注意看MySQL官网, RR隔离级别下解决了幻读问题)
- SERIALIZABLE(可串行化)
  SERIALIZABLE是最高的隔离级别, 它通过强制事务串行化执行, 避免了并发事务带来的问题.

| 隔离级别 | 读数据一致性                             | 更新丢失 | 脏读 | 不可重复读 | 幻读 |
| -------- | ---------------------------------------- | -------- | ---- | ---------- | ---- |
| 读未提交 | 最低级别, 只能保证不读取物理上损坏的数据 | ×        | √    | √          | √    |
| 读已提交 | 语句级                                   | ×        | ×    | √          | √    |
| 可重复读 | 事务级                                   | ×        | ×    | ×          | ×    |
| 可串行化 | 最高级别, 事务级                         | ×        | ×    | ×          | ×    |





# 2.Spring事务管理

## 2.1 注解源码

Spring事务管理的四个属性: Propagation, Isolation, timeout, readOnly.

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {
    @AliasFor("transactionManager")
    String value() default "";

    @AliasFor("value")
    String transactionManager() default "";

    Propagation propagation() default Propagation.REQUIRED;

    Isolation isolation() default Isolation.DEFAULT;

    int timeout() default -1;

    boolean readOnly() default false;

    Class<? extends Throwable>[] rollbackFor() default {};

    String[] rollbackForClassName() default {};

    Class<? extends Throwable>[] noRollbackFor() default {};

    String[] noRollbackForClassName() default {};
}

```



## 2.2 事务传播行为

事务传播：指方法A中调用了方法B

```java
public enum Propagation {
    REQUIRED(0),
    SUPPORTS(1),
    MANDATORY(2),
    REQUIRES_NEW(3),
    NOT_SUPPORTED(4),
    NEVER(5),
    NESTED(6);

    private final int value;

    private Propagation(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}

```

- REQUIRED
  如果A有事务就使用当前事务, 如果A没有事务, 就创建一个新事务.
- SUPPORTS
  如果A有事务就使用当前事务, 如果A没有事务, 就以非事务执行.
- MANDATORY
  如果A有事务就使用当前事务, 如果A没有事务, 就抛异常.
- REQUIRES_NEW
  不管A有没有事务都创建一个新事务.
- NOT_SUPPORTED
  不管A有没有事务都以非事务执行.
- NEVER
  如果A有事务就抛异常, 如果A没有事务, 就以非事务执行.
- NESTED
  如果A没有事务就创建一个事务, 如果A有事务, 就在当前事务中嵌套其他事务.



## 2.3 事务隔离级别

```java
public enum Isolation {
    DEFAULT(-1),
    READ_UNCOMMITTED(1),
    READ_COMMITTED(2),
    REPEATABLE_READ(4),
    SERIALIZABLE(8);

    private final int value;

    private Isolation(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
```

其中 DEFAULT 表示使用数据库的隔离级别.



## 2.4 事务超时

为了解决事务执行时间太长, 消耗太多资源的问题, 我们可以给事务设置一个超时时间, 如果事务执行时间超过了超时时间, 就回滚事务.



## 2.5 只读事务

一些不需要事务的方法, 比如读取数据, 就可以设置为只读事务, 这样可以有效地提高一些性能.



# 3.手动事务管理

Spring 使用 TransactionTemplate 事务模板来管理事务

## 3.1 dao层

```java
public interface AccountDao {
	 //汇款
	public void out(String outer , Integer money);
	
	//收款
	public void in(String inner , Integer money);
}

public class AccountDaoImpl extends JdbcDaoSupport implements AccountDao {

	public void out(String outer, Integer money) {
		this.getJdbcTemplate().update("update account set money = money - ? where username = ?", money,outer);
	}

	public void in(String inner, Integer money) {
		this.getJdbcTemplate().update("update account set money = money + ? where username = ?", money,inner);
	}
}
```



## 3.2 Service层

```java
public interface AccountService {
	//转账
	public void transfer(String outer ,String inner ,Integer money);
}

public class AccountServiceImpl implements AccountService {

    private AccountDao accountDao;

	private TransactionTemplate transactionTemplate;

    public void setAccountDao(AccountDao accountDao) {
        this.accountDao = accountDao;
    }	
    
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public void transfer(final String outer, final String inner, final Integer money) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus arg0) {
                accountDao.out(outer, money);
                //模拟故障
                int i = 1/0;
                accountDao.in(inner, money);
            }
        });
    }
}
```



## 3.3 Spring配置

```xml
<beans>
    <!-- 1、datasource -->
    <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
        <property name="driverClass" value="com.mysql.jdbc.Driver"></property>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/test"></property>
        <property name="user" value="root"></property>
        <property name="password" value="123"></property>
    </bean>

    <!-- 2、dao  -->
    <bean id="accountDao" class="org.tx.dao.impl.AccountDaoImpl">
        <property name="dataSource" ref="dataSource"></property>
    </bean>

    <!-- 3、service -->
    <bean id="accountService" class="org.tx.service.impl.AccountServiceImpl">
        <property name="accountDao" ref="accountDao"></property>
        <property name="transactionTemplate" ref="transactionTemplate"></property>
    </bean>

    <!-- 4、创建事务模板 -->
    <bean id="transactionTemplate" class="org.springframework.transaction.support.TransactionTemplate">
        <property name="transactionManager" ref="txManager"></property>
    </bean>

    <!-- 5、配置事务管理器 ,管理器需要事务，事务从Connection获得，连接从连接池DataSource获得 -->
    <bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"></property>
    </bean>
</beans>
```



## 3.4 测试代码

```java
@Test
public void demo(){
	String xmlPath = "applicationContext.xml";
	ApplicationContext applicationContext = new ClassPathXmlApplicationContext(xmlPath);
	AccountService accountService =  (AccountService) applicationContext.getBean("accountService");
	accountService.transfer("jack", "rose", 1000);
}
```



# 4.事务代理

## 4.1 service

```java
public interface AccountService {
	//转账
	public void transfer(String outer ,String inner ,Integer money);
}

public class AccountServiceImpl implements AccountService {

	private AccountDao accountDao;

	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}
	
	public void transfer(String outer, String inner, Integer money) {
		accountDao.out(outer, money);
		//模拟故障
		int i = 1/0;
		accountDao.in(inner, money);
	}
}
```



## 4.2 Spring配置文件

```xml
<beans>
	<!-- 1、datasource -->
	<bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
		<property name="driverClass" value="com.mysql.jdbc.Driver"></property>
		<property name="jdbcUrl" value="jdbc:mysql://localhost:3306/test"></property>
		<property name="user" value="root"></property>
		<property name="password" value="123"></property>
	</bean>
	
	<!-- 2、dao  -->
	<bean id="accountDao" class="org.tx.dao.impl.AccountDaoImpl">
		<property name="dataSource" ref="dataSource"></property>
	</bean>
	
	<!-- 3、service -->
	<bean id="accountService" class="org.tx.service.impl.AccountServiceImpl">
		<property name="accountDao" ref="accountDao"></property>
	</bean>
	
	<!-- 4、service 代理对象 
		4.1 proxyInterfaces 接口 
		4.2 target 目标类
		4.3 transactionManager 事务管理器
		4.4 transactionAttributes 事务属性（事务详情）
			prop.key ：确定哪些方法使用当前事务配置
			prop.text:用于配置事务详情
				格式：PROPAGATION，ISOLATION，readOnly，-Exception，+Exception
					传播行为		隔离级别		是否只读		异常回滚		异常提交
				例如：
					<prop key="transfer">PROPAGATION_REQUIRED,ISOLATION_DEFAULT</prop> 默认传播行为，和隔离级别
					<prop key="transfer">PROPAGATION_REQUIRED,ISOLATION_DEFAULT,readOnly</prop> 只读
					<prop key="transfer">PROPAGATION_REQUIRED,ISOLATION_DEFAULT,+java.lang.ArithmeticException</prop>  有异常扔提交
	-->
	<bean id="proxyAccountService" class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean">
		<property name="proxyInterfaces" value="org.tx.service.AccountService"></property>
		<property name="target" ref="accountService"></property>
		<property name="transactionManager" ref="txManager"></property>
		<property name="transactionAttributes">
			<props>
				<prop key="transfer">PROPAGATION_REQUIRED,ISOLATION_DEFAULT</prop>
			</props>
		</property>
	</bean>
	
	<!-- 5、配置事务管理器  -->
	<bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
		<property name="dataSource" ref="dataSource"></property>
	</bean>
</beans>

```



## 4.3 测试代码

```java
@Test
public void demo(){
	String xmlPath = "applicationContext.xml";
	ApplicationContext applicationContext = new ClassPathXmlApplicationContext(xmlPath);
	AccountService accountService =  (AccountService) applicationContext.getBean("proxyAccountService");
	accountService.transfer("jack", "rose", 1000);
}
```



# 5.Spring + AspectJ

## 5.1 基于XML配置

service层

```java
public interface AccountService {
	//转账
	public void transfer(String outer ,String inner ,Integer money);
}

public class AccountServiceImpl implements AccountService {

	private AccountDao accountDao;
	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}
	
	public void transfer(String outer, String inner, Integer money) {
		accountDao.out(outer, money);
		//模拟故障
		int i = 1/0;
		accountDao.in(inner, money);
	}
}
```

spring配置文件

```xml
<beans>
	<!-- 1、datasource -->
	<bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
		<property name="driverClass" value="com.mysql.jdbc.Driver"></property>
		<property name="jdbcUrl" value="jdbc:mysql://localhost:3306/test"></property>
		<property name="user" value="root"></property>
		<property name="password" value="123"></property>
	</bean>
	
	<!-- 2、dao  -->
	<bean id="accountDao" class="org.tx.dao.impl.AccountDaoImpl">
		<property name="dataSource" ref="dataSource"></property>
	</bean>
	
	<!-- 3、service -->
	<bean id="accountService" class="org.tx.service.impl.AccountServiceImpl">
		<property name="accountDao" ref="accountDao"></property>
	</bean>
	
	<!-- 4、事务管理器 -->
	<bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
		<property name="dataSource" ref="dataSource"></property>
	</bean>
	
	<!-- 5、事务通知 
		<tx:attributes> 用于配置事务详情（事务属性）	
	-->
	<tx:advice id="txAdvice" transaction-manager="txManager">
		<tx:attributes>
			<tx:method name="transfer" propagation="REQUIRED" isolation="DEFAULT"/>
		</tx:attributes>
	</tx:advice>
	
	<!-- 6、AOP编程 -->
	<aop:config>
		<aop:advisor advice-ref="txAdvice" pointcut="execution(* org.tx.service.*.*(..))"/>
	</aop:config>
</beans>

```

测试代码

```java
@Test
public void demo(){
	String xmlPath = "applicationContext.xml";
	ApplicationContext applicationContext = new ClassPathXmlApplicationContext(xmlPath);
	AccountService accountService =  (AccountService) applicationContext.getBean("accountService");
	accountService.transfer("jack", "rose", 1000);
}
```



## 5.2 基于注解配置

service层

```java
@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
public class AccountServiceImpl implements AccountService {

	private AccountDao accountDao;
	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}
	
	//或者 @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public void transfer(String outer, String inner, Integer money) {
		accountDao.out(outer, money);
		//模拟故障
		int i = 1/0;
		accountDao.in(inner, money);
	}
}
```

spring配置文件

```xml
<beans>
		<!-- 1、datasource -->
		<bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
			<property name="driverClass" value="com.mysql.jdbc.Driver"></property>
			<property name="jdbcUrl" value="jdbc:mysql://localhost:3306/test"></property>
			<property name="user" value="root"></property>
			<property name="password" value="123"></property>
		</bean>
		
		<!-- 2、dao  -->
		<bean id="accountDao" class="org.tx.dao.impl.AccountDaoImpl">
			<property name="dataSource" ref="dataSource"></property>
		</bean>
		
		<!-- 3、service -->
		<bean id="accountService" class="org.tx.service.impl.AccountServiceImpl">
			<property name="accountDao" ref="accountDao"></property>
		</bean>
		
		<!-- 4、事务管理器 -->
		<bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
			<property name="dataSource" ref="dataSource"></property>
		</bean>
		
		<!-- 5、将事务管理器交予Spring -->
		<tx:annotation-driven transaction-manager="txManager"/>
</beans>
```

测试代码

```java
@Test
public void demo(){
	String xmlPath = "applicationContext.xml";
	ApplicationContext applicationContext = new ClassPathXmlApplicationContext(xmlPath);
	AccountService accountService =  (AccountService) applicationContext.getBean("accountService");
	accountService.transfer("jack", "rose", 1000);
}
```























