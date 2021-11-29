package org.jisel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@Repeatable(AddToProfile.AddToProfiless.class)
public @interface AddToProfile {

    String value();

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    @interface AddToProfiless {
        AddToProfile[] value();
    }
}