# 手写框架MVC

吃水不忘挖井人 感谢[椰子Tyshawn](https://tyshawnlee.blog.csdn.net/)

文章来源：https://blog.csdn.net/litianxiang_kaola/article/details/79121561

（下面在原文基础上 进行了一些修改）



# 1.简介

![](https://github.com/wdkang123/MySpringFramework/blob/main/images/mvc01.png?raw=true)

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

![](https://github.com/wdkang123/MySpringFramework/blob/main/images/mvc02.jpg?raw=true)

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





# 3.MVC实现

## 3.1 映射处理器

### 3.1.1 Request类

请求类中方法和路径对应`@RequestMapping`注解里的方法和路径

```java
public class Request {
    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求路径
     */
    private String requestPath;

    public Request(String requestMethod, String requestPath) {
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + requestMethod.hashCode();
        result = 31 * result + requestPath.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Request)) return false;
        Request request = (Request) obj;
        return request.getRequestPath().equals(this.requestPath) && request.getRequestMethod().equals(this.requestMethod);
    }
}
```



### 3.1.2 Handler类

Handler类为一个处理器 封装了Controller的Class对象和Method方法

```java
public class Handler {

    /**
     * Controller 类
     */
    private Class<?> controllerClass;

    /**
     * Controller 方法
     */
    private Method controllerMethod;

    public Handler(Class<?> controllerClass, Method controllerMethod) {
        this.controllerClass = controllerClass;
        this.controllerMethod = controllerMethod;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getControllerMethod() {
        return controllerMethod;
    }
}
```

### 3.1.3 实现映射处理器

ControllerHelper助手类 定义了一个“请求-处理器”的映射REQUEST_MAP

REQUEST_MAP就相当于SpringMVC的映射处理器 接收到请求后返回对应的处理器

**REQUEST_MAP映射处理器的实现逻辑如下**

（1）首先通过ClassHelper工具类获取到应用下所有的Controller的Class对象

（2）然后遍历Controller及其所有方法 将所有带@RequestMapping注解的方法封装为处理器

（3）将@RequestMapping注解里的请求路径和请求方法封装成请求对象

（4）最后存入到REQUEST_MAP中

```java
public final class ControllerHelper {

    /**
     * REQUEST_MAP为 "请求-处理器" 的映射
     */
    private static final Map<Request, Handler> REQUEST_MAP = new HashMap<Request, Handler>();

    static {
        //遍历所有Controller类
        Set<Class<?>> controllerClassSet = ClassHelper.getControllerClassSet();
        if (CollectionUtils.isNotEmpty(controllerClassSet)) {
            for (Class<?> controllerClass : controllerClassSet) {
                //暴力反射获取所有方法
                Method[] methods = controllerClass.getDeclaredMethods();
                //遍历方法
                if (ArrayUtils.isNotEmpty(methods)) {
                    for (Method method : methods) {
                        //判断是否带RequestMapping注解
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                            //请求路径
                            String requestPath = requestMapping.value();
                            //请求方法
                            String requestMethod = requestMapping.method().name();

                            //封装请求和处理器
                            Request request = new Request(requestMethod, requestPath);
                            Handler handler = new Handler(controllerClass, method);
                            REQUEST_MAP.put(request, handler);p
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取 Handler
     */
    public static Handler getHandler(String requestMethod, String requestPath) {
        Request request = new Request(requestMethod, requestPath);
        return REQUEST_MAP.get(request);
    }
}
```



## 3.2 前端控制器

### 3.2.1 Param类

Param类用于封装Controller的参数

```java
public class Param {

    private Map<String, Object> paramMap;

    public Param() {
    }

    public Param(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public boolean isEmpty(){
        return MapUtils.isEmpty(paramMap);
    }
}
```

### 3.2.2 Data类

Data类用于封装Controller方法返回的JSON结果

```java
public class Data {

    /**
     * 模型数据
     */
    private Object model;

    public Data(Object model) {
        this.model = model;
    }

    public Object getModel() {
        return model;
    }
}
```

### 3.2.3 View类

View类用于封装视图返回的结果

```java
public class View {

    /**
     * 视图路径
     */
    private String path;

    /**
     * 模型数据
     */
    private Map<String, Object> model;

    public View(String path) {
        this.path = path;
        model = new HashMap<String, Object>();
    }

    public View addModel(String key, Object value) {
        model.put(key, value);
        return this;
    }

    public String getPath() {
        return path;
    }

    public Map<String, Object> getModel() {
        return model;
    }
}
```

### 3.2.4 RequestHelper 助手类

前端控制器接收到HTTP请求后 从HTTP中获取请求参数 然后封装到Param对象中

```java
public final class RequestHelper {

    /**
     * 获取请求参数
     */
    public static Param createParam(HttpServletRequest request) throws IOException {
        Map<String, Object> paramMap = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        //没有参数
        if (!paramNames.hasMoreElements()) {
            return null;
        }

        //get和post参数都能获取到
        while (paramNames.hasMoreElements()) {
            String fieldName = paramNames.nextElement();
            String fieldValue = request.getParameter(fieldName);
            paramMap.put(fieldName, fieldValue);
        }

        return new Param(paramMap);
    }
}
```

### 3.2.5 HelperLoader类

到目前为止 我们创建了ClassHelper BeanHelper IocHelper ControllerHelper 这四个类

我们需要一个入口程序来装载他们（实际上加载静态代码块）

当然就算没有这个入口程序 这些类也会被加载 这里只是为了让加载更加的集中

```java
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
```

### 3.2.6 实现前端控制器

前端控制器实际上是一个Servlet 这里配置的拦截是所有请求 在服务器启动时实例化

**当DispatchServlet实例化时**

首先执行init方法 这时会调用HelperLoader.init() 方法来加载相关的Helper类 并注册处理相应资源的Servlet

对于每一次客户端请求会执行service方法 这时会首先将请求方法和请求路径封装为 Request 对象

然后从映射处理器中（REQUEST_MAP）中获取处理器

接着从客户端中获取到Param对象 执行处理器方法

最后判断处理器方法的返回值 若为view类型 则跳转到JSP页面 若为data类型 则返回json数据

```java
@WebServlet(urlPatterns = "/*", loadOnStartup = 0)
public class DispatcherServlet extends HttpServlet {

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        //初始化相关的helper类
        HelperLoader.init();

        //获取ServletContext对象, 用于注册Servlet
        ServletContext servletContext = servletConfig.getServletContext();

        //注册处理jsp和静态资源的servlet
        registerServlet(servletContext);
    }

    /**
     * DefaultServlet和JspServlet都是由Web容器创建
     * org.apache.catalina.servlets.DefaultServlet
     * org.apache.jasper.servlet.JspServlet
     */
    private void registerServlet(ServletContext servletContext) {
        //动态注册处理JSP的Servlet
        ServletRegistration jspServlet = servletContext.getServletRegistration("jsp");
        jspServlet.addMapping(ConfigHelper.getAppJspPath() + "*");

        //动态注册处理静态资源的默认Servlet
        ServletRegistration defaultServlet = servletContext.getServletRegistration("default");
        defaultServlet.addMapping("/favicon.ico"); //网站头像
        defaultServlet.addMapping(ConfigHelper.getAppAssetPath() + "*");
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestMethod = request.getMethod().toUpperCase();
        String requestPath = request.getPathInfo();

        //这里根据Tomcat的配置路径有两种情况, 一种是 "/userList", 另一种是 "/context地址/userList".
        String[] splits = requestPath.split("/");
        if (splits.length > 2) {
            requestPath = "/" + splits[2];
        }

        //根据请求获取处理器(这里类似于SpringMVC中的映射处理器)
        Handler handler = ControllerHelper.getHandler(requestMethod, requestPath);
        if (handler != null) {
            Class<?> controllerClass = handler.getControllerClass();
            Object controllerBean = BeanHelper.getBean(controllerClass);

            //初始化参数
            Param param = RequestHelper.createParam(request);

            //调用与请求对应的方法(这里类似于SpringMVC中的处理器适配器)
            Object result;
            Method actionMethod = handler.getControllerMethod();
            if (param == null || param.isEmpty()) {
                result = ReflectionUtil.invokeMethod(controllerBean, actionMethod);
            } else {
                result = ReflectionUtil.invokeMethod(controllerBean, actionMethod, param);
            }

            //跳转页面或返回json数据(这里类似于SpringMVC中的视图解析器)
            if (result instanceof View) {
                handleViewResult((View) result, request, response);
            } else if (result instanceof Data) {
                handleDataResult((Data) result, response);
            }
        }
    }

    /**
     * 跳转页面
     */
    private void handleViewResult(View view, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String path = view.getPath();
        if (StringUtils.isNotEmpty(path)) {
            if (path.startsWith("/")) { //重定向
                response.sendRedirect(request.getContextPath() + path);
            } else { //请求转发
                Map<String, Object> model = view.getModel();
                for (Map.Entry<String, Object> entry : model.entrySet()) {
                    request.setAttribute(entry.getKey(), entry.getValue());
                }
                request.getRequestDispatcher(ConfigHelper.getAppJspPath() + path).forward(request, response);
            }
        }
    }

    /**
     * 返回JSON数据
     */
    private void handleDataResult(Data data, HttpServletResponse response) throws IOException {
        Object model = data.getModel();
        if (model != null) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();
            String json = JSON.toJSON(model).toString();
            writer.write(json);
            writer.flush();
            writer.close();
        }
    }
}
```





# 4.手写框架的实例

到这里为止 这个手写的框架已经实现了Bean容器 Ioc功能 MVC功能 所以现在我们完全可以用这个框架来写一个实例

## 4.1 业务类

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
        return userList;
    }
}
```

## 4.2 处理类

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

## 4.3 JSP

```java
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
        <th>username</th>
        <th>password</th>
    </tr>
    <c:forEach var="userinfo" items="${userList}">
        <tr>
            <td>${userinfo.username}</td>
            <td>${userinfo.password}</td>
            <td>
                <a href="#">详情</a>
                <a href="#">编辑</a>
            </td>
        </tr>
    </c:forEach>
</table>
</body>
</html>
```

## 4.4 启动后的效果

![](https://github.com/wdkang123/MySpringFramework/blob/main/images/MVC03.png?raw=true)





