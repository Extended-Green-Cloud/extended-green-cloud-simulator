package org.greencloud.dataanalysisapi.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link ClusteringFeatures}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableClusteringFeatures.builder()}.
 */
@Generated(from = "ClusteringFeatures", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableClusteringFeatures
    implements ClusteringFeatures {
  private final ImmutableList<String> allFeatures;
  private final @Nullable ImmutableList<String> categoricalFeatures;

  private ImmutableClusteringFeatures(
      ImmutableList<String> allFeatures,
      @Nullable ImmutableList<String> categoricalFeatures) {
    this.allFeatures = allFeatures;
    this.categoricalFeatures = categoricalFeatures;
  }

  /**
   * @return list of all features used in clustering
   */
  @JsonProperty("all_features")
  @Override
  public ImmutableList<String> getAllFeatures() {
    return allFeatures;
  }

  /**
   * @return optional list of categorical features
   */
  @JsonProperty("categorical_features")
  @Override
  public @Nullable ImmutableList<String> getCategoricalFeatures() {
    return categoricalFeatures;
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link ClusteringFeatures#getAllFeatures() allFeatures}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringFeatures withAllFeatures(String... elements) {
    ImmutableList<String> newValue = ImmutableList.copyOf(elements);
    return new ImmutableClusteringFeatures(newValue, this.categoricalFeatures);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link ClusteringFeatures#getAllFeatures() allFeatures}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of allFeatures elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringFeatures withAllFeatures(Iterable<String> elements) {
    if (this.allFeatures == elements) return this;
    ImmutableList<String> newValue = ImmutableList.copyOf(elements);
    return new ImmutableClusteringFeatures(newValue, this.categoricalFeatures);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link ClusteringFeatures#getCategoricalFeatures() categoricalFeatures}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringFeatures withCategoricalFeatures(@Nullable String... elements) {
    if (elements == null) {
      return new ImmutableClusteringFeatures(this.allFeatures, null);
    }
    @Nullable ImmutableList<String> newValue = elements == null ? null : ImmutableList.copyOf(elements);
    return new ImmutableClusteringFeatures(this.allFeatures, newValue);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link ClusteringFeatures#getCategoricalFeatures() categoricalFeatures}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of categoricalFeatures elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringFeatures withCategoricalFeatures(@Nullable Iterable<String> elements) {
    if (this.categoricalFeatures == elements) return this;
    @Nullable ImmutableList<String> newValue = elements == null ? null : ImmutableList.copyOf(elements);
    return new ImmutableClusteringFeatures(this.allFeatures, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableClusteringFeatures} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableClusteringFeatures
        && equalTo(0, (ImmutableClusteringFeatures) another);
  }

  private boolean equalTo(int synthetic, ImmutableClusteringFeatures another) {
    return allFeatures.equals(another.allFeatures)
        && Objects.equals(categoricalFeatures, another.categoricalFeatures);
  }

  /**
   * Computes a hash code from attributes: {@code allFeatures}, {@code categoricalFeatures}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + allFeatures.hashCode();
    h += (h << 5) + Objects.hashCode(categoricalFeatures);
    return h;
  }

  /**
   * Prints the immutable value {@code ClusteringFeatures} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ClusteringFeatures")
        .omitNullValues()
        .add("allFeatures", allFeatures)
        .add("categoricalFeatures", categoricalFeatures)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "ClusteringFeatures", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements ClusteringFeatures {
    @Nullable List<String> allFeatures = ImmutableList.of();
    @Nullable List<String> categoricalFeatures = null;
    @JsonProperty("all_features")
    public void setAllFeatures(List<String> allFeatures) {
      this.allFeatures = allFeatures;
    }
    @JsonProperty("categorical_features")
    public void setCategoricalFeatures(@Nullable List<String> categoricalFeatures) {
      this.categoricalFeatures = categoricalFeatures;
    }
    @Override
    public List<String> getAllFeatures() { throw new UnsupportedOperationException(); }
    @Override
    public List<String> getCategoricalFeatures() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableClusteringFeatures fromJson(Json json) {
    ImmutableClusteringFeatures.Builder builder = ImmutableClusteringFeatures.builder();
    if (json.allFeatures != null) {
      builder.addAllAllFeatures(json.allFeatures);
    }
    if (json.categoricalFeatures != null) {
      builder.addAllCategoricalFeatures(json.categoricalFeatures);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link ClusteringFeatures} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ClusteringFeatures instance
   */
  public static ImmutableClusteringFeatures copyOf(ClusteringFeatures instance) {
    if (instance instanceof ImmutableClusteringFeatures) {
      return (ImmutableClusteringFeatures) instance;
    }
    return ImmutableClusteringFeatures.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableClusteringFeatures ImmutableClusteringFeatures}.
   * <pre>
   * ImmutableClusteringFeatures.builder()
   *    .addAllFeatures|addAllAllFeatures(String) // {@link ClusteringFeatures#getAllFeatures() allFeatures} elements
   *    .categoricalFeatures(List&amp;lt;String&amp;gt; | null) // nullable {@link ClusteringFeatures#getCategoricalFeatures() categoricalFeatures}
   *    .build();
   * </pre>
   * @return A new ImmutableClusteringFeatures builder
   */
  public static ImmutableClusteringFeatures.Builder builder() {
    return new ImmutableClusteringFeatures.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableClusteringFeatures ImmutableClusteringFeatures}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ClusteringFeatures", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private ImmutableList.Builder<String> allFeatures = ImmutableList.builder();
    private ImmutableList.Builder<String> categoricalFeatures = null;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code ClusteringFeatures} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ClusteringFeatures instance) {
      Objects.requireNonNull(instance, "instance");
      addAllAllFeatures(instance.getAllFeatures());
      @Nullable List<String> categoricalFeaturesValue = instance.getCategoricalFeatures();
      if (categoricalFeaturesValue != null) {
        addAllCategoricalFeatures(categoricalFeaturesValue);
      }
      return this;
    }

    /**
     * Adds one element to {@link ClusteringFeatures#getAllFeatures() allFeatures} list.
     * @param element A allFeatures element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllFeatures(String element) {
      this.allFeatures.add(element);
      return this;
    }

    /**
     * Adds elements to {@link ClusteringFeatures#getAllFeatures() allFeatures} list.
     * @param elements An array of allFeatures elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllFeatures(String... elements) {
      this.allFeatures.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link ClusteringFeatures#getAllFeatures() allFeatures} list.
     * @param elements An iterable of allFeatures elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("all_features")
    public final Builder allFeatures(Iterable<String> elements) {
      this.allFeatures = ImmutableList.builder();
      return addAllAllFeatures(elements);
    }

    /**
     * Adds elements to {@link ClusteringFeatures#getAllFeatures() allFeatures} list.
     * @param elements An iterable of allFeatures elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllAllFeatures(Iterable<String> elements) {
      this.allFeatures.addAll(elements);
      return this;
    }

    /**
     * Adds one element to {@link ClusteringFeatures#getCategoricalFeatures() categoricalFeatures} list.
     * @param element A categoricalFeatures element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addCategoricalFeatures(String element) {
      if (this.categoricalFeatures == null) {
        this.categoricalFeatures = ImmutableList.builder();
      }
      this.categoricalFeatures.add(element);
      return this;
    }

    /**
     * Adds elements to {@link ClusteringFeatures#getCategoricalFeatures() categoricalFeatures} list.
     * @param elements An array of categoricalFeatures elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addCategoricalFeatures(String... elements) {
      if (this.categoricalFeatures == null) {
        this.categoricalFeatures = ImmutableList.builder();
      }
      this.categoricalFeatures.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link ClusteringFeatures#getCategoricalFeatures() categoricalFeatures} list.
     * @param elements An iterable of categoricalFeatures elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("categorical_features")
    public final Builder categoricalFeatures(@Nullable Iterable<String> elements) {
      if (elements == null) {
        this.categoricalFeatures = null;
        return this;
      }
      this.categoricalFeatures = ImmutableList.builder();
      return addAllCategoricalFeatures(elements);
    }

    /**
     * Adds elements to {@link ClusteringFeatures#getCategoricalFeatures() categoricalFeatures} list.
     * @param elements An iterable of categoricalFeatures elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllCategoricalFeatures(Iterable<String> elements) {
      Objects.requireNonNull(elements, "categoricalFeatures element");
      if (this.categoricalFeatures == null) {
        this.categoricalFeatures = ImmutableList.builder();
      }
      this.categoricalFeatures.addAll(elements);
      return this;
    }

    /**
     * Builds a new {@link ImmutableClusteringFeatures ImmutableClusteringFeatures}.
     * @return An immutable instance of ClusteringFeatures
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableClusteringFeatures build() {
      return new ImmutableClusteringFeatures(allFeatures.build(), categoricalFeatures == null ? null : categoricalFeatures.build());
    }
  }
}
