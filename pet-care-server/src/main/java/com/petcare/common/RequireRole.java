package com.petcare.common;

import java.lang.annotation.*;

/** RBAC role check annotation. 0=owner, 1=staff, 2=admin */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /** Allowed roles. Empty = any authenticated user */
    int[] value() default {};
}
