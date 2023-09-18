package org.greencloud.commons.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

@JacksonAnnotationsInside
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Value.Style(
		of = "new",
		privateNoargConstructor = true,
		allParameters = true,
		visibility = Value.Style.ImplementationVisibility.PUBLIC
)
public @interface ImmutableConfig {
}
