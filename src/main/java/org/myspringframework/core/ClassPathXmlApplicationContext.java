package org.myspringframework.core;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassPathXmlApplicationContext implements ApplicationContext {

    private static final Logger logger = LoggerFactory.getLogger(ClassPathXmlApplicationContext.class);
    private Map<String, Object> singletonObjects = new HashMap<>();


    public ClassPathXmlApplicationContext(String configLocation) {
        try {
            SAXReader reader = new SAXReader();
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(configLocation);
            Document document = reader.read(in);
            List<Node> nodes = document.selectNodes("//bean");
            nodes.forEach(node -> {
                try {
                    Element beanElt = (Element) node;
                    String id = beanElt.attributeValue("id");
                    String className = beanElt.attributeValue("class");
                    logger.info("beanName = " + id);
                    logger.info("beanClassName = " + className);
                    Class<?> clazz = Class.forName(className);
                    Constructor<?> defaultCon = clazz.getDeclaredConstructor();
                    Object bean = defaultCon.newInstance();
                    singletonObjects.put(id, bean);
                    logger.info(singletonObjects.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            nodes.forEach(node -> {
                try {
                    Element beanElt = (Element) node;
                    String id = beanElt.attributeValue("id");
                    String className = beanElt.attributeValue("class");
                    Class<?> clazz = Class.forName(className);
                    List<Element> properties = beanElt.elements("property");
                    properties.forEach(property -> {
                        try {
                            String propertyName = property.attributeValue("name");
                            Field field = clazz.getDeclaredField(propertyName);
                            logger.info("propertyName = " + propertyName);
                            String setMethodName = "set" + propertyName.toUpperCase().charAt(0) + propertyName.substring(1);
                            Method setMethod = clazz.getDeclaredMethod(setMethodName, field.getType());
                            String value = property.attributeValue("value");
                            String ref = property.attributeValue("ref");
                            if(value != null) {
                                String propertyTypeSimpleName = field.getType().getSimpleName();
                                Object propertyVal = null;
                                switch (propertyTypeSimpleName) {
                                    case "byte": case "Byte":
                                        propertyVal = Byte.valueOf(value);
                                        break;
                                    case "short": case "Short":
                                        propertyVal = Short.valueOf(value);
                                        break;
                                    case "int": case "Integer":
                                        propertyVal = Integer.valueOf(value);
                                        break;
                                    case "long": case "Long":
                                        propertyVal = Long.valueOf(value);
                                        break;
                                    case "float": case "Float":
                                        propertyVal = Float.valueOf(value);
                                        break;
                                    case "double": case "Double":
                                        propertyVal = Double.valueOf(value);
                                        break;
                                    case "boolean": case "Boolean":
                                        propertyVal = Boolean.valueOf(value);
                                        break;
                                    case "char": case "Character":
                                        propertyVal = value.charAt(0);
                                        break;
                                    case "String":
                                        propertyVal = value;
                                        break;
                                }
                                setMethod.invoke(singletonObjects.get(id), propertyVal);
                            }
                            if(ref != null) {
                                setMethod.invoke(singletonObjects.get(id), singletonObjects.get(ref));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getBean(String beanName) {
        return singletonObjects.get(beanName);
    }
}
