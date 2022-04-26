# 如何实现简单的 IOC 容器

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

