# 简单 IOC 容器的实现

## 需要实现的点

1. 创建注解

   ```java
   // 表示作用在类上
   @Target(ElementType.TYPE)
   // 可通过反射获取
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Service {
   }
   ```

2. 提取标记对象

   * 指定范围，获取范围内的所有类
   * 遍历所有类，获取被注解标记的类，并加载入容器中

3. 实现容器

   * 保存 Class 对象及其载体

     可采用 `ConcurrentHashMap`

   * 容器加载

     * 配置的管理与获取
     * 获取指定范围内的 Class 对象
     * 依据配置提取 Class 对象，连同实例一并存入容器

   * 对外提供容器的操作方式

     * 增加、删除操作
     * 通过注解来获取被注解标注的 Class
     * 根据 Class 获取对应实例
     * 通过超类获取对应的子类 Class
     * 获取所有 Class 和实例
     * 获取容器载体保存的 Class 数量

4. 依赖注入

   * 定义相关的注解标签
   * 创建被注解标记的成员变量实例，并将其注入到成员变量中

# 手写 AOP

## 1.0实现

1. 基于 CGLib实现动态代理

   > JDK 动态代理需要被代理的类必须继承某一个接口
   >
   > 基于ASM的CGLib，可以为被代理的类动态生成子类，以解决 JDK动态代理对接口的束缚

2. 实现 @Aspect、@Order 注解

   ```Java
   @Target(ElementType.TYPE)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Aspect {
       // 表示当前被 Aspect 标记的横切逻辑，会织入到被属性逻辑标记的那些类中
       Class<? extends Annotation> value();
   }
   @Target(ElementType.TYPE)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Order {
       // 规定value越小优先级越高
       int value();
   }
   ```

3. 实现 DefaultAspect 所有切面必须继承 DefaultAspect

   ```java
   public abstract class DefaultAspect {
       /**
        * 前置拦截
        * @param targetClass 被代理的目标类
        * @param method 被代理的目标方法
        * @param args 被代理的目标方法对应的参数列表
        * @throws Throwable
        */
       public void before(Class<?> targetClass, Method method,Object[] args) throws Throwable{
   
       }
   
       /**
        *
        * @param targetClass 被代理的目标类
        * @param method 被代理的目标方法
        * @param args 被代理的目标方法对应的参数列表
        * @param returnValue 被代理的目标方法执行后的返回值
        * @return
        * @throws Throwable
        */
       public Object afterReturning(Class<?> targetClass, Method method,Object[] args,Object returnValue)throws Throwable{
           return returnValue;
       }
   
       /**
        *
        * @param targetClass 被代理的目标类
        * @param method 被代理的目标方法
        * @param args 被代理的目标方法对应的参数列表
        * @param e 被代理的目标方法抛出异常
        * @throws Throwable
        */
       public void afterThrowing(Class<?> targetClass, Method method,Object[] args,Throwable e) throws Throwable{
   
       }
   }
   
   ```

4. Aspect的排序以及Advice的定序执行

   ```
   public class AspectInfo {
       private int orderIndex;
       private DefaultAspect aspectObject;
   }
   ```

   将 @Order 的值与 Aspect 的实现放入 `List<AspectInfo>`中，并将其按照 @Order 的值进行排序，以实现 Advice 的定序执行

4. 横切逻辑的织入

   通过 CGLib 实现横切逻辑的织入

   ```java
       @Override
       public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
           Object returnValue = null;
           // 1. 按照order的顺序升序执行完所有Aspect的before方法
           invokeBeforeAdvices(method, args);
           try {
               // 2. 执行被代理类的方法
               returnValue = methodProxy.invokeSuper(proxy, args);
               // 3. 如果被代理方法正常返回，则按照order的顺序降序执行完所有的 Aspect 的afterReturning方法
               returnValue = invokeAfterReturningAdvices(method, args, returnValue);
           } catch (Exception e) {
               // 4. 如果被代理方法抛出异常，则按照order的顺序降序执行完所有 Aspect 的afterThrowing方法
               invokeAfterThrowingAdives(method, args, e);
           }
           return returnValue;
       }
   ```

### 1.0的短板

Pointcut 粒度只能支持到注解级别，即只能筛选出被诸如 @Controller @Service 等注解标记的类的集合进行横切逻辑的织入

## 2.0 引入 AspectJ 的表达式解析功能

```Java
    /**
     * 表达式解析器
     */
    private PointcutExpression pointcutExpression;

    public PointcutLocator(String expression) {
        this.pointcutExpression = pointcutParser.parsePointcutExpression(expression);
    }

    /**
     * 判断传入的Class对象是否是Aspect的目标代理类，即匹配的Pointcut表达式(初筛)
     *
     * @param targetClass 表达式
     * @return 是否匹配
     */
    public boolean roughMatches(Class<?> targetClass) {
        // 只能校验within
        // 不能校验（execution，call，get，set），面对无法校验的表达式，直接返回true
        return pointcutExpression.couldMatchJoinPointsInType(targetClass);
    }

    /**
     * 判断传入的 Method 对象是否是Aspect的目标代理方法，即匹配 Pointcut表达式（精筛）
     * @param method
     * @return
     */
    public boolean accurateMatches(Method method) {
        ShadowMatch shadowMatch = pointcutExpression.matchesMethodExecution(method);
        return shadowMatch.alwaysMatches();
    }
```

### 相应的对1.0 进行修改

1. 修改 @Aspect 注解

   ```Java
   @Target(ElementType.TYPE)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Aspect {
       // 表示当前被 Aspect 标记的横切逻辑，被织入类的位置
       String pointcut();
   }
   ```

2. 修改对应的 AspectInfo

   ```
   public class AspectInfo {
   	// 优先级
       private int orderIndex;
       // Aspect实例
       private DefaultAspect aspectObject;
       // 依据 @Aspect 的值。为其注入对应的 PointcutExpression
       private PointcutLocator pointcutLocator;
   }
   ```