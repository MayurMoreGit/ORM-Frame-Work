package com.thinking.machines.ormframework.annotations;
import java.lang.annotation.*;
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Table
{
String name();
}