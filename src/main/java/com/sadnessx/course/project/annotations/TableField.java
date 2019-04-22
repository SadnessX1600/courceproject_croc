package com.sadnessx.course.project.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface TableField {
    String type() default "";

    boolean isPrimaryKey() default false;

    boolean isUnique() default false;

}
