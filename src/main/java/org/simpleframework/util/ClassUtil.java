package org.simpleframework.util;


import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;


@Slf4j
public class ClassUtil {

    public static final String FILE_PROTOCOL = "file";

    /**
     * 获取包下类集合
     * <p>
     * 获取到类的加载器
     * 通过类加载器获取到加载的资源信息
     * 依据不同的资源类型，采用不同方式获取资源集合
     *
     * @param packageName 包名
     * @return 类集合
     */
    public static Set<Class<?>> extractPackageClass(String packageName) {

        // 获取到类加载器
        ClassLoader classLoader = getClassLoader();
        URL url = classLoader.getResource(packageName.replace(".", "/"));
        if (url == null) {
            log.warn("从 {} 获取到URL", packageName);
            return null;
        }
        // 依据不同的资源类型，采用不同的方式获取资源集合
        Set<Class<?>> classSet = null;
        // 过滤出文件类型的资源
        if (url.getProtocol().equalsIgnoreCase(FILE_PROTOCOL)) {
            classSet = new HashSet<>();
            File packageDirectory = new File(url.getPath());
            extractClassFile(classSet, packageDirectory, packageName);
        }

        return classSet;
    }

    /**
     * 递归获取目标 package里面的所有class文件(包括子 package 里的 class文件)
     *
     * @param emptyClassSet 装载目标类的集合
     * @param fileSource    文件或目录
     * @param packageName   包名
     */
    private static void extractClassFile(Set<Class<?>> emptyClassSet, File fileSource, String packageName) {
        // 不是目录则跳出递归
        if (!fileSource.isDirectory()) {
            return;
        }
        // 如果是文件夹，则调用listFiles方法获取文件夹下的文件或文件夹
        File[] files = fileSource.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) return true;
                else {
                    // 获取文件的绝对值路径
                    String absoluteFilePath = file.getAbsolutePath();
                    if (absoluteFilePath.endsWith(".class")) {
                        // 若是class文件则直接加载
                        addToClassSet(absoluteFilePath);
                    }
                }
                return false;
            }

            // 根据文件的绝对值路径，获取并生成class对象，并放入classSet中
            private void addToClassSet(String absoluteFilePath) {
                absoluteFilePath = absoluteFilePath.replace(File.separator, ".");
                String className = absoluteFilePath.substring(absoluteFilePath.indexOf(packageName));
                className = className.substring(0, className.lastIndexOf("."));
                // 通过反射机制获取到对应的Class对象并加入到classSet中
                Class targetClass = loadClass(className);
                emptyClassSet.add(targetClass);
            }
        });
        if (files != null) {
            // 递归调用
            for (File f : files) {
                extractClassFile(emptyClassSet, f, packageName);
            }
        }
    }

    /**
     * 获取class
     *
     * @param className
     * @return
     */
    public static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("获取class异常", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 实例化class
     *
     * @param clazz      class
     * @param <T>        class的类型
     * @param accessible 是否支持创建出class对象的实例
     * @return
     */
    public static <T> T newInstance(Class<?> clazz, boolean accessible) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(accessible);
            return (T) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("创建类实例异常", e);
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取ClassLoader
     *
     * @return 当前 ClassLoader
     */
    public static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 设置类的属性值
     *
     * @param field      成员变量
     * @param target     类实例
     * @param value      成员变量的值
     * @param accessible 是否允许设置私有属性
     */
    public static void setField(Field field, Object target, Object value, boolean accessible) {
        field.setAccessible(accessible);
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            log.error("设置属性错误", e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        extractPackageClass("com.imooc");
    }
}
