# 手写框架MVC

吃水不忘挖井人 感谢[椰子Tyshawn](https://tyshawnlee.blog.csdn.net/)

文章来源：https://blog.csdn.net/litianxiang_kaola/article/details/79121561

（下面在原文基础上 进行了一些修改）



# 1.简介

图片【】



上篇博客实现了Bean容器和Ioc功能 本篇博客实现简化版的SpringMVC 在下面的内容前

SpringMVC最核心的部分就是前端控制器 `DispatchServlet`

而`DispatchServlet`其实就是一个Servlet 

所以我们有必要了解下Servlet的知识点



# 2.Servlet基础

这部分很基础 就很快带过了



## 2.1 编写servlet

```java
public class ServletDemo implements Servlet{

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		res.getWriter().println("Hello ServletDemo!");
	}
	
	//destroy、init、getServletConfig、getServletInfo
}
```

## 2.2 修改web.xml

```xml
<!-- 创建一个Servlet实例 -->
<servlet>
	<servlet-name>ServletDemo</servlet-name>
	<servlet-class>org.servlet.ServletDemo</servlet-class>
</servlet>

<!-- 给Servlet映射一个可访问的URI地址-->
<servlet-mapping>
	<servlet-name>ServletDemo</servlet-name>
	<url-pattern>/ServletDemo</url-pattern>
</servlet-mapping>
```

## 2.3 部署到应用到tomcat



## 2.4 测试 http://localhost:8080/Demo/ServletDemo



## 2.5 执行流程

图片【】

- 客户端发出请求 http://localhost:8080/Demo/ServletDemo
- 根据 web.xml文件的配置，找到< url-pattern>的值为 /ServletDemo 的< servlet-mapping>元素
- 读取< servlet-mapping>元素的< servlet-name>子元素的值，由此确定Servlet的名字为 ServletDemo
- 找到< servlet-name>值为 ServletDemo的< servlet>元素
- 读取< servlet>元素的< servlet-class>子元素的值，由此确定Servlet的类名为org.servlet.ServletDemo
- 到Tomcat安装目录/webapps/Demo/WEB-INF/classes/org/servlet目录下查找到 ServletDemo.class文件



## 2.6 Servlet 生命周期

Servlet程序是由WEB服务器（如Tomcat）调用

**WEB服务器收到客户端的Servlet访问请求后**

- ①Web服务器首先检查是否已经装载并创建了该Servlet的实例对象。如果是，则直接执行第④步，否则，执行第②步。
- ② 装载并创建该Servlet的一个实例对象。
- ③ 调用Servlet实例对象的init()方法。
- ④ 创建一个用于封装HTTP请求消息的HttpServletRequest对象和一个代表HTTP响应消息的HttpServletResponse对象，然后调用Servlet的service()方法并将请求和响应对象作为参数传递进去。
- ⑤ WEB应用程序被停止或重新启动之前，Servlet引擎将卸载Servlet，并在卸载之前调用Servlet的destroy()方法。



Tomcat在加载Web应用时，就会把相应的web.xml文件中的数据读入到内存中。

因此当Tomcat需要参考web.xml文件时，实际上只需要从内存中读取相关数据就可以了，无需再到文件系统中读取web.xml。



## 2.7 Servlet特征

(1)Servlet是单例多线程的.

(2)一个Servlet实例只会执行一次无参构造器与init()方法, 并且是在第一次访问时执行.

(3)用户每提交一次对当前Servlet的请求, 就会执行一次service()方法.

(4)一个Servlet实例只会执行一次destroy()方法, 在应用停止时执行.

(5)由于Servlet是单例多线程的, 所以为了保证其线程安全性, 一般情况下不定义可以修改的成员变量. 因为每个线程均可修改这个成员变量, 会出现线程安全问题.

(6)默认情况下, Servlet在Web容器启动时是不会被实例化的, 在第一次调用时实例化. 但可以设置成Web容器启动时实例化.

```xml
//web.xml
<servlet>
	<servlet-name>ServletDemo</servlet-name>
	<servlet-class>org.servlet.ServletDemo</servlet-class>
</servlet>

<!-- 如果load-on-startup配置项大于或等于0，当前Servlet会在服务器启动时创建, 大于0时越小优先级越高-->
<load-on-startup>2</load-on-startup>
```



## 2.8 Servlet线程线程安全

当多个客户端并发访问同一个Servlet时，web服务器会为每一个客户端的访问请求创建一个线程，并在这个线程上调用Servlet的service方法，

因此service方法内如果访问了同一个资源的话，就有可能引发线程安全问题。

**解决并发出现的问题，可以采用以下方式：**

- 使用Java同步机制(synchronize)对多线程同步：运行效率低
- 合理决定在Servlet中定义的变量的作用域（解决线程安全问题的最佳办法，不要写全局变量，而写局部变量。）



## 2.9 ServletConfig对象

在Servlet的配置文件中，可以使用一个或多个< init-param>标签为servlet配置一些初始化参数。

当servlet配置了初始化参数后，web容器在创建Servlet实例对象时，会自动将这些初始化参数封装到ServletConfig对象中，并在调用servlet的init方法时，

将ServletConfig对象传递给servlet。

进而，程序员通过ServletConfig对象就可以得到当前Servlet的初始化参数信息。

```java
<web-app>
	<servlet>
		<servlet-name>ServletDemo</servlet-name>
		<servlet-class>org.servlet.ServletDemo</servlet-class>
		
	 <init-param>
		<param-name>encoding</param-name>
		<param-value>UTF-8</param-value>
	</init-param>
	</servlet>
	
	<servlet-mapping>
		<servlet-name>ServletDemo</servlet-name>
		<url-pattern>/ServletDemo</url-pattern>
	</servlet-mapping>
</web-app>

方法一：
public class ServletDemo extends HttpServlet{
	private ServletConfig config;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		this.config = config;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String value = config.getInitParameter("encoding");
		System.out.println(value);
	}
}

方法二：
public class ServletDemo extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String value = this.getServletConfig().getInitParameter("encoding");
		System.out.println(value);
	}
}

方法三：
public class ServletDemo extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String value = this.getInitParameter("encoding");
		System.out.println(value);
	}
}
```



## 2.10 ServletContext对象

WEB容器在启动时，它会为**每个WEB应用程序**都创建一个对应的ServletContext对象，它代表当前web应用。

ServletConfig对象中维护了ServletContext对象的引用，开发人员在编写servlet时，可以通过`ServletConfig.getServletContext`方法获得`ServletContext`对象。

由于一个WEB应用中的所有Servlet共享同一个ServletContext对象，因此Servlet对象之间可以通过ServletContext对象来实现通讯。

**ServletContext对象通常也被称之为context域对象。**



### ServletContext应用

**多个Servlet通过ServletContext对象实现数据共享**

- void setAttribute(String name,object value); //向ServletContext对象的map中添加
- Object getAttribute(String name); //从ServletContext对象的map中取数据
- void removeAttribute(String name); //根据name去移除数据

**获取WEB应用的初始化参数**

- String getInitParameter(String name); //根据配置文件中的key得到value

**实现Servlet的转发**

```java
protected void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
	ServletContext sc = this.getServletConfig().getServletContext();
	RequestDispatcher rd = sc.getRequestDispatcher("/ServletDemo2");
	rd.forward(req, res);
}
```

**利用ServletContext对象读取资源文件**

```java
private void test() throws IOException {
	ServletContext sc = this.getServletContext();
	String path = sc.getRealPath("/WEB-INF/classes/org/servlet/a.properties");
	Properties prop = new Properties();
	prop.load(new FileInputStream(path));
	System.out.println(prop.get("ltx"));
}
```

































