package org.greencloud.dataanalysisapi.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link ClusteringConfiguration}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableClusteringConfiguration.builder()}.
 */
@Generated(from = "ClusteringConfiguration", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableClusteringConfiguration
    implements ClusteringConfiguration {
  private final @Nullable String name;
  private final ClusteringFeatures features;
  private final ClusteringMethod method;
  private final ClusteringParameters parameters;

  private ImmutableClusteringConfiguration(
      @Nullable String name,
      ClusteringFeatures features,
      ClusteringMethod method,
      ClusteringParameters parameters) {
    this.name = name;
    this.features = features;
    this.method = method;
    this.parameters = parameters;
  }

  /**
   * @return optional name of clustering
   */
  @JsonProperty("name")
  @Override
  public @Nullable String getName() {
    return name;
  }

  /**
   * @return features used in clustering
   */
  @JsonProperty("features")
  @Override
  public ClusteringFeatures getFeatures() {
    return features;
  }

  /**
   * @return names of methods used in clustering
   */
  @JsonProperty("method")
  @Override
  public ClusteringMethod getMethod() {
    return method;
  }

  /**
   * @return parameters applied in clustering methods
   */
  @JsonProperty("parameters")
  @Override
  public ClusteringParameters getParameters() {
    return parameters;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ClusteringConfiguration#getName() name} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for name (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableClusteringConfiguration withName(@Nullable String value) {
    if (Objects.equals(this.name, value)) return this;
    return new ImmutableClusteringConfiguration(value, this.features, this.method, this.parameters);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ClusteringConfiguration#getFeatures() features} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for features
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableClusteringConfiguration withFeatures(ClusteringFeatures value) {
    if (this.features == value) return this;
    ClusteringFeatures newValue = Objects.requireNonNull(value, "features");
    return new ImmutableClusteringConfiguration(this.name, newValue, this.method, this.parameters);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ClusteringConfiguration#getMethod() method} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for method
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableClusteringConfiguration withMethod(ClusteringMethod value) {
    if (this.method == value) return this;
    ClusteringMethod newValue = Objects.requireNonNull(value, "method");
    return new ImmutableClusteringConfiguration(this.name, this.features, newValue, this.parameters);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ClusteringConfiguration#getParameters() parameters} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for parameters
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableClusteringConfiguration withParameters(ClusteringParameters value) {
    if (this.parameters == value) return this;
    ClusteringParameters newValue = Objects.requireNonNull(value, "parameters");
    return new ImmutableClusteringConfiguration(this.name, this.features, this.method, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableClusteringConfiguration} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableClusteringConfiguration
        && equalTo(0, (ImmutableClusteringConfiguration) another);
  }

  private boolean equalTo(int synthetic, ImmutableClusteringConfiguration another) {
    return Objects.equals(name, another.name)
        && features.equals(another.features)
        && method.equals(another.method)
        && parameters.equals(another.parameters);
  }

  /**
   * Computes a hash code from attributes: {@code name}, {@code features}, {@code method}, {@code parameters}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + Objects.hashCode(name);
    h += (h << 5) + features.hashCode();
    h += (h << 5) + method.hashCode();
    h += (h << 5) + parameters.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code ClusteringConfiguration} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ClusteringConfiguration")
        .omitNullValues()
        .add("name", name)
        .add("features", features)
        .add("method", method)
        .add("parameters", parameters)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "ClusteringConfiguration", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements ClusteringConfiguration {
    @Nullable String name;
    @Nullable ClusteringFeatures features;
    @Nullable ClusteringMethod method;
    @Nullable ClusteringParameters parameters;
    @JsonProperty("name")
    public void setName(@Nullable String name) {
      this.name = name;
    }
    @JsonProperty("features")
    public void setFeatures(ClusteringFeatures features) {
      this.features = features;
    }
    @JsonProperty("method")
    public void setMethod(ClusteringMethod method) {
      this.method = method;
    }
    @JsonProperty("parameters")
    public void setParameters(ClusteringParameters parameters) {
      this.parameters = parameters;
    }
    @Override
    public String getName() { throw new UnsupportedOperationException(); }
    @Override
    public ClusteringFeatures getFeatures() { throw new UnsupportedOperationException(); }
    @Override
    public ClusteringMethod getMethod() { throw new UnsupportedOperationException(); }
    @Override
    public ClusteringParameters getParameters() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableClusteringConfiguration fromJson(Json json) {
    ImmutableClusteringConfiguration.Builder builder = ImmutableClusteringConfiguration.builder();
    if (json.name != null) {
      builder.name(json.name);
    }
    if (json.features != null) {
      builder.features(json.features);
    }
    if (json.method != null) {
      builder.method(json.method);
    }
    if (json.parameters != null) {
      builder.parameters(json.parameters);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link ClusteringConfiguration} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ClusteringConfiguration instance
   */
  public static ImmutableClusteringConfiguration copyOf(ClusteringConfiguration instance) {
    if (instance instanceof ImmutableClusteringConfiguration) {
      return (ImmutableClusteringConfiguration) instance;
    }
    return ImmutableClusteringConfiguration.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableClusteringConfiguration ImmutableClusteringConfiguration}.
   * <pre>
   * ImmutableClusteringConfiguration.builder()
   *    .name(String | null) // nullable {@link ClusteringConfiguration#getName() name}
   *    .features(org.greencloud.dataanalysisapi.domain.ClusteringFeatures) // required {@link ClusteringConfiguration#getFeatures() features}
   *    .method(org.greencloud.dataanalysisapi.domain.ClusteringMethod) // required {@link ClusteringConfiguration#getMethod() method}
   *    .parameters(org.greencloud.dataanalysisapi.domain.ClusteringParameters) // required {@link ClusteringConfiguration#getParameters() parameters}
   *    .build();
   * </pre>
   * @return A new ImmutableClusteringConfiguration builder
   */
  public static ImmutableClusteringConfiguration.Builder builder() {
    return new ImmutableClusteringConfiguration.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableClusteringConfiguration ImmutableClusteringConfiguration}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ClusteringConfiguration", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_FEATURES = 0x1L;
    private static final long INIT_BIT_METHOD = 0x2L;
    private static final long INIT_BIT_PARAMETERS = 0x4L;
    private long initBits = 0x7L;

    private @Nullable String name;
    private @Nullable ClusteringFeatures features;
    private @Nullable ClusteringMethod method;
    private @Nullable ClusteringParameters parameters;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code ClusteringConfiguration} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ClusteringConfiguration instance) {
      Objects.requireNonNull(instance, "instance");
      @Nullable String nameValue = instance.getName();
      if (nameValue != null) {
        name(nameValue);
      }
      features(instance.getFeatures());
      method(instance.getMethod());
      parameters(instance.getParameters());
      return this;
    }

    /**
     * Initializes the value for the {@link ClusteringConfiguration#getName() name} attribute.
     * @param name The value for name (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("name")
    public final Builder name(@Nullable String name) {
      this.name = name;
      return this;
    }

    /**
     * Initializes the value for the {@link ClusteringConfiguration#getFeatures() features} attribute.
     * @param features The value for features 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("features")
    public final Builder features(ClusteringFeatures features) {
      this.features = Objects.requireNonNull(features, "features");
      initBits &= ~INIT_BIT_FEATURES;
      return this;
    }

    /**
     * Initializes the value for the {@link ClusteringConfiguration#getMethod() method} attribute.
     * @param method The value for method 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("method")
    public final Builder method(ClusteringMethod method) {
      this.method = Objects.requireNonNull(method, "method");
      initBits &= ~INIT_BIT_METHOD;
      return this;
    }

    /**
     * Initializes the value for the {@link ClusteringConfiguration#getParameters() parameters} attribute.
     * @param parameters The value for parameters 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("parameters")
    public final Builder parameters(ClusteringParameters parameters) {
      this.parameters = Objects.requireNonNull(parameters, "parameters");
      initBits &= ~INIT_BIT_PARAMETERS;
      return this;
    }

    /**
     * Builds a new {@link ImmutableClusteringConfiguration ImmutableClusteringConfiguration}.
     * @return An immutable instance of ClusteringConfiguration
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableClusteringConfiguration build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableClusteringConfiguration(name, features, method, parameters);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_FEATURES) != 0) attributes.add("features");
      if ((initBits & INIT_BIT_METHOD) != 0) attributes.add("method");
      if ((initBits & INIT_BIT_PARAMETERS) != 0) attributes.add("parameters");
      return "Cannot build ClusteringConfiguration, some of required attributes are not set " + attributes;
    }
  }
}
