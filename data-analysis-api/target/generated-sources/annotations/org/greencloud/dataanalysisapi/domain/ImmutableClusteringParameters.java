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
 * Immutable implementation of {@link ClusteringParameters}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableClusteringParameters.builder()}.
 */
@Generated(from = "ClusteringParameters", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableClusteringParameters
    implements ClusteringParameters {
  private final @Nullable ImmutableList<Object> clustering;
  private final @Nullable ImmutableList<Object> validation;

  private ImmutableClusteringParameters(
      @Nullable ImmutableList<Object> clustering,
      @Nullable ImmutableList<Object> validation) {
    this.clustering = clustering;
    this.validation = validation;
  }

  /**
   * @return optional list of clustering parameters
   */
  @JsonProperty("clustering")
  @Override
  public @Nullable ImmutableList<Object> getClustering() {
    return clustering;
  }

  /**
   * @return optional list of dimensionality reduction parameters
   */
  @JsonProperty("dimensionality_reduction")
  @Override
  public @Nullable ImmutableList<Object> getValidation() {
    return validation;
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link ClusteringParameters#getClustering() clustering}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringParameters withClustering(@Nullable Object... elements) {
    if (elements == null) {
      return new ImmutableClusteringParameters(null, this.validation);
    }
    @Nullable ImmutableList<Object> newValue = elements == null ? null : ImmutableList.copyOf(elements);
    return new ImmutableClusteringParameters(newValue, this.validation);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link ClusteringParameters#getClustering() clustering}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of clustering elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringParameters withClustering(@Nullable Iterable<? extends Object> elements) {
    if (this.clustering == elements) return this;
    @Nullable ImmutableList<Object> newValue = elements == null ? null : ImmutableList.copyOf(elements);
    return new ImmutableClusteringParameters(newValue, this.validation);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link ClusteringParameters#getValidation() validation}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringParameters withValidation(@Nullable Object... elements) {
    if (elements == null) {
      return new ImmutableClusteringParameters(this.clustering, null);
    }
    @Nullable ImmutableList<Object> newValue = elements == null ? null : ImmutableList.copyOf(elements);
    return new ImmutableClusteringParameters(this.clustering, newValue);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link ClusteringParameters#getValidation() validation}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of validation elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringParameters withValidation(@Nullable Iterable<? extends Object> elements) {
    if (this.validation == elements) return this;
    @Nullable ImmutableList<Object> newValue = elements == null ? null : ImmutableList.copyOf(elements);
    return new ImmutableClusteringParameters(this.clustering, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableClusteringParameters} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableClusteringParameters
        && equalTo(0, (ImmutableClusteringParameters) another);
  }

  private boolean equalTo(int synthetic, ImmutableClusteringParameters another) {
    return Objects.equals(clustering, another.clustering)
        && Objects.equals(validation, another.validation);
  }

  /**
   * Computes a hash code from attributes: {@code clustering}, {@code validation}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + Objects.hashCode(clustering);
    h += (h << 5) + Objects.hashCode(validation);
    return h;
  }

  /**
   * Prints the immutable value {@code ClusteringParameters} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ClusteringParameters")
        .omitNullValues()
        .add("clustering", clustering)
        .add("validation", validation)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "ClusteringParameters", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements ClusteringParameters {
    @Nullable List<Object> clustering = null;
    @Nullable List<Object> validation = null;
    @JsonProperty("clustering")
    public void setClustering(@Nullable List<Object> clustering) {
      this.clustering = clustering;
    }
    @JsonProperty("dimensionality_reduction")
    public void setValidation(@Nullable List<Object> validation) {
      this.validation = validation;
    }
    @Override
    public List<Object> getClustering() { throw new UnsupportedOperationException(); }
    @Override
    public List<Object> getValidation() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableClusteringParameters fromJson(Json json) {
    ImmutableClusteringParameters.Builder builder = ImmutableClusteringParameters.builder();
    if (json.clustering != null) {
      builder.addAllClustering(json.clustering);
    }
    if (json.validation != null) {
      builder.addAllValidation(json.validation);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link ClusteringParameters} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ClusteringParameters instance
   */
  public static ImmutableClusteringParameters copyOf(ClusteringParameters instance) {
    if (instance instanceof ImmutableClusteringParameters) {
      return (ImmutableClusteringParameters) instance;
    }
    return ImmutableClusteringParameters.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableClusteringParameters ImmutableClusteringParameters}.
   * <pre>
   * ImmutableClusteringParameters.builder()
   *    .clustering(List&amp;lt;Object&amp;gt; | null) // nullable {@link ClusteringParameters#getClustering() clustering}
   *    .validation(List&amp;lt;Object&amp;gt; | null) // nullable {@link ClusteringParameters#getValidation() validation}
   *    .build();
   * </pre>
   * @return A new ImmutableClusteringParameters builder
   */
  public static ImmutableClusteringParameters.Builder builder() {
    return new ImmutableClusteringParameters.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableClusteringParameters ImmutableClusteringParameters}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ClusteringParameters", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private ImmutableList.Builder<Object> clustering = null;
    private ImmutableList.Builder<Object> validation = null;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code ClusteringParameters} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ClusteringParameters instance) {
      Objects.requireNonNull(instance, "instance");
      @Nullable List<Object> clusteringValue = instance.getClustering();
      if (clusteringValue != null) {
        addAllClustering(clusteringValue);
      }
      @Nullable List<Object> validationValue = instance.getValidation();
      if (validationValue != null) {
        addAllValidation(validationValue);
      }
      return this;
    }

    /**
     * Adds one element to {@link ClusteringParameters#getClustering() clustering} list.
     * @param element A clustering element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addClustering(Object element) {
      if (this.clustering == null) {
        this.clustering = ImmutableList.builder();
      }
      this.clustering.add(element);
      return this;
    }

    /**
     * Adds elements to {@link ClusteringParameters#getClustering() clustering} list.
     * @param elements An array of clustering elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addClustering(Object... elements) {
      if (this.clustering == null) {
        this.clustering = ImmutableList.builder();
      }
      this.clustering.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link ClusteringParameters#getClustering() clustering} list.
     * @param elements An iterable of clustering elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("clustering")
    public final Builder clustering(@Nullable Iterable<? extends Object> elements) {
      if (elements == null) {
        this.clustering = null;
        return this;
      }
      this.clustering = ImmutableList.builder();
      return addAllClustering(elements);
    }

    /**
     * Adds elements to {@link ClusteringParameters#getClustering() clustering} list.
     * @param elements An iterable of clustering elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllClustering(Iterable<? extends Object> elements) {
      Objects.requireNonNull(elements, "clustering element");
      if (this.clustering == null) {
        this.clustering = ImmutableList.builder();
      }
      this.clustering.addAll(elements);
      return this;
    }

    /**
     * Adds one element to {@link ClusteringParameters#getValidation() validation} list.
     * @param element A validation element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addValidation(Object element) {
      if (this.validation == null) {
        this.validation = ImmutableList.builder();
      }
      this.validation.add(element);
      return this;
    }

    /**
     * Adds elements to {@link ClusteringParameters#getValidation() validation} list.
     * @param elements An array of validation elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addValidation(Object... elements) {
      if (this.validation == null) {
        this.validation = ImmutableList.builder();
      }
      this.validation.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link ClusteringParameters#getValidation() validation} list.
     * @param elements An iterable of validation elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("dimensionality_reduction")
    public final Builder validation(@Nullable Iterable<? extends Object> elements) {
      if (elements == null) {
        this.validation = null;
        return this;
      }
      this.validation = ImmutableList.builder();
      return addAllValidation(elements);
    }

    /**
     * Adds elements to {@link ClusteringParameters#getValidation() validation} list.
     * @param elements An iterable of validation elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllValidation(Iterable<? extends Object> elements) {
      Objects.requireNonNull(elements, "validation element");
      if (this.validation == null) {
        this.validation = ImmutableList.builder();
      }
      this.validation.addAll(elements);
      return this;
    }

    /**
     * Builds a new {@link ImmutableClusteringParameters ImmutableClusteringParameters}.
     * @return An immutable instance of ClusteringParameters
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableClusteringParameters build() {
      return new ImmutableClusteringParameters(
          clustering == null ? null : clustering.build(),
          validation == null ? null : validation.build());
    }
  }
}
