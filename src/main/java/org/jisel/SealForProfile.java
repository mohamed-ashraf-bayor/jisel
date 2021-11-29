package org.jisel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
@Repeatable(SealForProfile.SealForProfiless.class)
public @interface SealForProfile {

    String value();

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.METHOD)
    @interface SealForProfiless {
        SealForProfile[] value();
    }
}