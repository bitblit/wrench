# Wrench-Web

## Using SimpleIncludeFilter

In your static web directory, add a WEB-INF/web.xml that looks like this:

```xml
<?xml version="1.0" encoding="utf-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd" version="2.5">

    <display-name>SimpleFilteredWebsite</display-name>

    <filter>
        <filter-name>simpleIncludesFilter</filter-name>
        <filter-class>com.erigir.wrench.web.simpleincludes.SimpleIncludesFilter</filter-class>
        <init-param>
            <param-name>defaultFileIncludePath</param-name>
            <param-value>prop:simpleIncludesSource</param-value>
        </init-param>
        <init-param>
            <param-name>defaultIncludeMode</param-name>
            <param-value>html</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>simpleIncludesFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

</web-app>
```xml

Then you'll need to set the simpleIncludesSource property in your pom.xml, like so:

```xml
            <plugin>
                <groupId>org.apache.tomcat.maven</groupId>
                <artifactId>tomcat7-maven-plugin</artifactId>
                <version>2.2</version>
                <configuration>
                    <systemProperties>
                        <simpleIncludesSource>${project.basedir}/src/main/webapp</simpleIncludesSource>
                    </systemProperties>
                    <path>/</path>
                    <httpPort>${tomcat.port.number}</httpPort>
                    <httpsPort>${tomcat.ssl.port.number}</httpsPort>
                    <keystoreFile>${basedir}/src/main/config/tomcat-ssl.keystore</keystoreFile>
                    <keystorePass>jetty8</keystorePass>
                </configuration>
            </plugin>
```xml
