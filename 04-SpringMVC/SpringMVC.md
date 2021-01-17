# SpringMVC框架

吃水不忘挖井人 感谢[椰子Tyshawn](https://tyshawnlee.blog.csdn.net/)

文章来源：https://blog.csdn.net/litianxiang_kaola/article/details/79121561

（下面在原文基础上 进行了一些修改）



# 1.JavaEE体系

包括四层 上到下分别是应用层、Web层、业务层、持久层。

Struts和SpringMVC是Web层的框架，Spring是业务层的框架，Hibernate和MyBatis是持久层的框架。



# 2.为什么要使用SpringMVC

很多应用程序之间 问题在于处理业务的对象和显示业务数据的视图之间存在紧密联系

通常 更新业务对象的命令都是从视图本身发起的 使视图对任何业务更改都有高度敏感性

而且当多个视图依赖于一个业务对象时也是没有灵活性可言的



SpringMVC是一种基于JAVA 实现了Web MCV的设计模式

请求驱动类型的轻量级web框架 使用了MVC架构的思想 将web层进行职责解耦

基于请求驱动就是请求-响应模型 框架的目的就是帮助我们简化开发 SpringMVC也是简化我们日常web开发



# 3.MVC设计模式

MVC设计模式的任务是将包含业务数据的模块与显示模块的视图解耦

在模型和视图之间引入重定向层

此重定向层是控制器 控制器将请求接收 执行更新模型的操作 然后通知视图关于模型更改的消息

## 3.1 传统架构

![](https://github.com/wdkang123/MySpringFramework/blob/main/images/mvc1.png?raw=true)



## 3.2 传统MVC架构

![](https://github.com/wdkang123/MySpringFramework/blob/main/images/mvc2.png?raw=true)



# 4.SpringMVC架构

![](https://github.com/wdkang123/MySpringFramework/blob/main/images/mvc3.png?raw=true)

## 4.1 SpringMVC核心架构

![](https://github.com/wdkang123/MySpringFramework/blob/main/images/mvc4.png?raw=true)



## 4.2 执行的具体流程

### 4.2.1 DispatchServlet 

首先浏览器发送请求 到 DispatchServlet 前端控制器收到请求后不进行自己处理 而是委托给其他解析器进行处理 作为统一访问点 进行全局的流程控制

### 4.2.2 DispatchServlet => HandlerMapping

DispatchServlet => HandlerMapping 处理器映射器将会把请求映射为HandlerExecutionChain对象（包含一个Handler处理器对象 和 多个 HandlerInterceptor拦截器对象）

### 4.2.3 DispatchServlet => HandlerAdapter

DispatchServlet => HandlerAdapter 处理器适配器会将处理器包装为装饰器 从而支持多种类型的处理器 即适配器设计模式的应用 从而很容易支持多类型的处理器

### 4.2.4 HandlerAdapter => 调用处理器

HandlerAdapter 调用处理器相应功能的处理方法 并返回一个ModelAndView对象（包含模型数据 逻辑视图名）

### 4.2.5 ModelAndView

Model部分是业务对象返回的模型数据 View部分为逻辑视图名 => ViewResolver 视图解析器把逻辑视图名解析为具体的View

### 4.2.6 View => 渲染

View会根据传进来的Model模型数据进行渲染 此处的Model实际是一个Map数据结构

### 4.2.7 返回控制权给DispatchServlet

由DispatchServlet返回响应给用户 至此一个流程结束



# 5.SpringMVC入门程序

## 5.1 web.xml

```xml
<web-app>
  <servlet>
      <!-- 加载前端控制器 -->
      <servlet-name>springmvc</servlet-name>
      <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
      <!-- 
           加载配置文件
           默认加载规范：
           * 文件命名：servlet-name-servlet.xml====springmvc-servlet.xml
           * 路径规范：必须在WEB-INF目录下面
           修改加载路径：
       -->
       <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:springmvc.xml</param-value>   
       </init-param>
  </servlet>
  
  <servlet-mapping>
      <servlet-name>springmvc</servlet-name>
      <url-pattern>*.do</url-pattern>
  </servlet-mapping>
</web-app>
```

## 5.2 springmvc.xml

```xml
<beans>
	<!-- 
		配置映射处理器：根据bean(自定义Controller)的name属性的url去寻找handler；springmvc默认的映射处理器是
			BeanNameUrlHandlerMapping
	 -->
	<bean class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping"></bean>
	
    <!-- 
		配置处理器适配器来执行Controlelr ,springmvc默认的是 SimpleControllerHandlerAdapter
	-->
	<bean class="org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter"></bean>
	
	<!-- 配置自定义Controller -->
	<bean id="myController" name="/hello.do" class="org.controller.MyController"></bean>
	
	<!-- 
		配置sprigmvc视图解析器：解析逻辑试图； 
		后台返回逻辑试图：index
		视图解析器解析出真正物理视图：前缀+逻辑试图+后缀====/WEB-INF/jsps/index.jsp
	-->
	<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
		<property name="prefix" value="/WEB-INF/jsps/"></property>
		<property name="suffix" value=".jsp"></property>		
	</bean>
</beans>
```

## 5.3 自定义处理器

```java
public class MyController implements Controller{
	public ModelAndView handleRequest(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
		ModelAndView mv = new ModelAndView();
		//设置页面回显数据
		mv.addObject("hello", "欢迎学习springmvc！");
		
		//返回物理视图
		//mv.setViewName("/WEB-INF/jsps/index.jsp");
		
		//返回逻辑视图
		mv.setViewName("index");
		return mv;
	}
}
```

## 5.4 index页面

```html
<html>
<body>
<h1>${hello}</h1>
</body>
</html>
```

## 5.5 测试地址

```
http://localhost:8080/springmvc/hello.do
```



# 6.HandlerMapping

## 6.1 介绍

处理映射器将会把请求映射成HandlerExecutionChain对象（包含一个Handler处理器对象 和 多个HandlerInterceptor拦截器对象）

通过这种策略模式 很容易添加新的映射策略

## 6.2 处理器映射器

处理器映射器有三种 可以共存 互不影响

### 6.2.1 BeanNameUrlHandlerMapping

默认映射器 即使不配置 也会使用这个请求来映射

```xml
<bean class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping"></bean>
<!-- 映射器把hello.do请求映射到该处理器 -->
<bean id="testController" name="/hello.do" class="org.controller.TestController"></bean>
```



### 6.2.2 SimpleUrlHandlerMapping

该处映射器可以配置多个映射对应一个处理器

```xml
<bean class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping">
	<property name="mappings">
		<props>
			<prop key="/ss.do">testController</prop>
			<prop key="/abc.do">testController</prop>
		</props>
	</property>
</bean>
<!-- 上面的这个映射配置表示多个*.do文件可以访问同一个Controller -->
<bean id="testController" name="/hello.do" class="org.controller.TestController"></bean>

```



### 6.2.3 ControllerClassNameHandlerMapping

该映射器可以不用手动配置映射 通过 类名.do 来访问对应的处理器

```xml
<!-- 这个Mapping一配置, 我们就可以使用Controller的 [类名.do]来访问这个Controller. -->
<bean class="org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping"></bean>
```



# 7.HandlerAdapter

## 7.1 介绍

处理器适配器有两种

SimpleControllerHandlerAdapter 和 HttpRequestHandlerAdapter

## 7.2 处理器适配器

### 7.2.1 SimpleControllerHandlerAdapter 

这是默认的适配器 所有实现了`org.springframework.web.servlet.mvc.Controller` 接口的处理器都是通过此适配器执行的



### 7.2.2 HttpRequestHandlerAdapter

该适配器将http请求封装成 `HttpServletRequest` 和 `HttpServletResponse` 对象

所有实现了 `org.springframework.web.HttpRequestHandler` 接口的处理器都是通过此适配器执行的

**示例如下**

配置`HttpRequestHandlerAdapter`

```xml
<!-- 配置HttpRequestHandlerAdapter适配器 -->
<bean class="org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter"></bean>
```

编写处理器

```java
public class HttpController implements HttpRequestHandler {
	public void handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//给Request设置值，在页面进行回显
		request.setAttribute("hello", "这是HttpRequestHandler！");
		//跳转页面
		request.getRequestDispatcher("/WEB-INF/jsps/index.jsp").forward(request, response);
	}
}
```

index页面

```html
<html>
<body>
<h1>${hello}</h1>
</body>
</html>
```



## 7.3Adapter源码分析

前端控制器（DispatchServlet）  接收到Handler对象后 传递给对应的处理器适配器（HandlerAdapter） 处理器适配器调用相应的Handler方法

### 7.3.1 模拟处理器

```java
// 以下是Controller接口和它的是三种实现 
public interface Controller {
}

public class SimpleController implements Controller{
	public void doSimpleHandler() {
		System.out.println("Simple...");
	}
}

public class HttpController implements Controller{
	public void doHttpHandler() {
		System.out.println("Http...");
	}
}

public class AnnotationController implements Controller{
	public void doAnnotationHandler() {
		System.out.println("Annotation..");
	}
} 
```



### 7.3.2 模拟处理器适配器

```java
// 以下是HandlerAdapter接口和它的三种实现
public interface HandlerAdapter {
	public boolean supports(Object handler);
	public void handle(Object handler);
}

public class SimpleHandlerAdapter implements HandlerAdapter{
	public boolean supports(Object handler) {
		return (handler instanceof SimpleController);
	}

	public void handle(Object handler) {
		((SimpleController)handler).doSimpleHandler();
	}
}

public class HttpHandlerAdapter implements HandlerAdapter{
	public boolean supports(Object handler) {
		return (handler instanceof HttpController);
	}

	public void handle(Object handler) {
		((HttpController)handler).doHttpHandler();
	}
}

public class AnnotationHandlerAdapter implements HandlerAdapter{
	public boolean supports(Object handler) {
		return (handler instanceof AnnotationController);
	}

	public void handle(Object handler) {
		((AnnotationController)handler).doAnnotationHandler();
	}
}
```



### 7.3.3 模拟DispatchServlet

```java
public class Dispatcher {
	public static List<HandlerAdapter> handlerAdapter = new ArrayList<HandlerAdapter>();
	
	public Dispatcher(){
		handlerAdapter.add(new SimpleHandlerAdapter());
		handlerAdapter.add(new HttpHandlerAdapter());
		handlerAdapter.add(new AnnotationHandlerAdapter());
	}
	
	// 核心功能
	public void doDispatch() {
		// 前端控制器（DispatcherServlet）接收到Handler对象后
		// SimpleController handler = new SimpleController();
		// HttpController handler = new HttpController();
		AnnotationController handler = new AnnotationController();
		
		// 传递给对应的处理器适配器（HandlerAdapter）
		HandlerAdapter handlerAdapter = getHandlerAdapter(handler);
		
		// 处理器适配器调用相应的Handler方法
		handlerAdapter.handle(handler);
	}
	
	// 通过Handler找到对应的处理器适配器（HandlerAdapter）
	public HandlerAdapter getHandlerAdapter(Controller handler) {
		for(HandlerAdapter adapter : handlerAdapter){
			if(adapter.supports(handler)){
				return adapter;
			}
		}
		return null;
	}
}
```

### 7.3.4 测试

```java
public class Test {
	public static void main(String[] args) {
		Dispatcher dispather = new Dispatcher();
		dispather.doDispatch();
	}
}
```



# 8.Handler

这里只介绍上文出现的两种处理器 除此之外很多适用于各种应用场景的处理器

尤其是Controller接口还有很多实现类：AbstractController、AbstractUrlViewController、MultiActionController、ParameterizableViewController等等···

## 8.1 Controller

`org.springframework.web.serlvet.mvc.Controller`  该处理器对应的适配器是`SimpleControllerHandlerAdapter`

```java
public interface Controller {
	/**
	 * Process the request and return a ModelAndView object which the DispatcherServlet
	 * will render. A {@code null} return value is not an error: it indicates that
	 * this object completed request processing itself and that there is therefore no
	 * ModelAndView to render.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @return a ModelAndView to render, or {@code null} if handled directly
	 * @throws Exception in case of errors
	 */
	ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
```

该处理器用于处理用户提交的请求 通过调用Service层代码 实现用户请求的计算响应 并最终将计算所得的数据及响应的页面封装为一个ModelAndView对象 返回给前端控制器 DispatchServlet



## 8.2 HttpRequestHandler

`org.springframework.web.HttpRequestHandler` 该处理器对应的适配器是 `HttpRequestHandlerAdapter`

```java
public interface HttpRequestHandler {
    void handleRequest(HttpServletRequest var1, HttpServletResponse var2) throws ServletException, IOException;
}
```

该处理器方法没有返回值 不能像ModelAndView一样 将数据及目标视图封装成一个对象 但可以将数据放入Request、Session等作用域

并由Request、Response完成跳转



# 9.ViewResolver

视图解析器负责处理结果生成View视图 这里介绍两种常用的视图解析器

## 9.1 InternalResourceViewResolver

该视图解析器用于完成对当前web应用内部资源的封装和跳转

而对于内部资源的查找规则是 

将ModelAndView中指定的视图名称与视图解析器配置的前缀与后缀结合 拼接成一个web应用内部资源路径

内部资源路径 = 前缀 + 视图名称 + 后缀

`InternResourceViewResolver` 解析器会把处理器方法返回的模型属性都存到对应的Request中 然后将请求转发到URL

**处理器**

```java
public class MyController implements Controller {
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mv = new ModelAndView();
        mv.addObject("hello", "hello world!");
        mv.setViewName("index");
        return mv;
    }
}
```

**视图解析器**

```xml
<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/jsp/"></property>
        <property name="suffix" value=".jsp"></property>
</bean>
```

当然 若不指定的话 可以在代码中进行控制

```java
mv.setViewName("/WEB-INF/jsp/index.jsp");
```





## 9.2 BeanNameViewResolver

`InternResourceViewResolver` 有一个问题 就是只可以完成内部资源的跳转 无法完成外部资源的跳转

`BeanNameViewResolver` 视图解析器将资源（内部和外部）封装为bean 然后再通过`ModelAndView` 中设置bean实例的id值来指定资源 在配置文件中可以同时配置多个bean

**处理器**

```java
public class MyController implements Controller {
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        return new ModelAndView("myInternalView");
        // return new ModelAndView("baidu.com");
    }
}
```

**视图解析器配置**

```xml
<!--视图解析器-->
<bean class="org.springframework.web.servlet.view.BeanNameViewResolver"/>
<!--内部资源view-->
<bean id="myInternalView" class="org.springframework.web.servlet.view.JstlView">
    <property name="url" value="/jsp/show.jsp"/>
</bean>
<!--外部资源view-->
<bean id="baidu" class="org.springframework.web.servlet.view.RedirectView">
    <property name="url" value="https://www.baidu.com/"/>
</bean>
```













