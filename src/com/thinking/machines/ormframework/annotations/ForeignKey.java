package com.thinking.machines.ormframework.annotations;
import java.lang.annotation.*;
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ForeignKey
{
String parent();
String column();
}