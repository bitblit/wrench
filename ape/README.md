* Ape

Ape is a not an acronym.  It isa library for simplifying the creation of REST API's by making it somewhat easier
to comply with the guide to good API design published by Apigee.

# Notes on error handling
You want ALL errors to be caught and transformed into JSON at some point, but since some things are always outside of 
Spring's purview nothing in Spring can handle that - thats why we have an error handling filter that is the
web.xml endpoint for all errors.  Since in their vast wisdom (sarcasm!) the spec writers think we don't need a 
Java-config way of setting up error handlers, you must include the following web.xml as well (everything else can
be java).

Why a filter?  We'll a servlet would have been more logical, but I already had a bunch of filters setup and this
allows me to follow that pattern (and spring has better built-in support for filters than servlets, since it
expects you to use DispatcherServlet/controllers for that kind of thing).

You also need your content scan to scan the package com.erigir.wrench.ape.http to pick up the ErrorHandler.  It will
map to /ErrorHandler, which works as long as you map dispatcherservlet to /.  If you map it to xyz, then location
below would need to be /xyz/ErrorHandler

```xml

<?xml version="1.0" encoding="ISO-8859-1" ?>

<web-app xmlns="http://java.sun.com/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
         version="3.0">

    <error-page>
        <location>/ErrorHandler</location>
    </error-page>

</web-app>


```xml
