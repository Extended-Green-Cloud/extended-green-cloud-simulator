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
import org.greencloud.dataanalysisapi.enums.ClusteringMethodEnum;
import org.greencloud.dataanalysisapi.enums.DimensionalityReductionEnum;
import org.greencloud.dataanalysisapi.enums.ValidationMetricsEnum;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link ClusteringMethod}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableClusteringMethod.builder()}.
 */
@Generated(from = "ClusteringMethod", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableClusteringMethod implements ClusteringMethod {
  private final @Nullable ClusteringMethodEnum clustering;
  private final @Nullable ImmutableList<ValidationMetricsEnum> validation;
  private final @Nullable DimensionalityReductionEnum dimensionalityReduction;

  private ImmutableClusteringMethod(
      @Nullable ClusteringMethodEnum clustering,
      @Nullable ImmutableList<ValidationMetricsEnum> validation,
      @Nullable DimensionalityReductionEnum dimensionalityReduction) {
    this.clustering = clustering;
    this.validation = validation;
    this.dimensionalityReduction = dimensionalityReduction;
  }

  /**
   * @return name of clustering method
   */
  @JsonProperty("clustering")
  @Override
  public @Nullable ClusteringMethodEnum getClustering() {
    return clustering;
  }

  /**
   * @return optional list of validation metrics used
   */
  @JsonProperty("validation")
  @Override
  public @Nullable ImmutableList<ValidationMetricsEnum> getValidation() {
    return validation;
  }

  /**
   * @return name of dimensionality reduction method
   */
  @JsonProperty("dimensionality_reduction")
  @Override
  public @Nullable DimensionalityReductionEnum getDimensionalityReduction() {
    return dimensionalityReduction;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ClusteringMethod#getClustering() clustering} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for clustering (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableClusteringMethod withClustering(@Nullable ClusteringMethodEnum value) {
    if (this.clustering == value) return this;
    return new ImmutableClusteringMethod(value, this.validation, this.dimensionalityReduction);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link ClusteringMethod#getValidation() validation}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringMethod withValidation(@Nullable ValidationMetricsEnum... elements) {
    if (elements == null) {
      return new ImmutableClusteringMethod(this.clustering, null, this.dimensionalityReduction);
    }
    @Nullable ImmutableList<ValidationMetricsEnum> newValue = elements == null ? null : ImmutableList.copyOf(elements);
    return new ImmutableClusteringMethod(this.clustering, newValue, this.dimensionalityReduction);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link ClusteringMethod#getValidation() validation}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of validation elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringMethod withValidation(@Nullable Iterable<? extends ValidationMetricsEnum> elements) {
    if (this.validation == elements) return this;
    @Nullable ImmutableList<ValidationMetricsEnum> newValue = elements == null ? null : ImmutableList.copyOf(elements);
    return new ImmutableClusteringMethod(this.clustering, newValue, this.dimensionalityReduction);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ClusteringMethod#getDimensionalityReduction() dimensionalityReduction} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for dimensionalityReduction (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableClusteringMethod withDimensionalityReduction(@Nullable DimensionalityReductionEnum value) {
    if (this.dimensionalityReduction == value) return this;
    return new ImmutableClusteringMethod(this.clustering, this.validation, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableClusteringMethod} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableClusteringMethod
        && equalTo(0, (ImmutableClusteringMethod) another);
  }

  private boolean equalTo(int synthetic, ImmutableClusteringMethod another) {
    return Objects.equals(clustering, another.clustering)
        && Objects.equals(validation, another.validation)
        && Objects.equals(dimensionalityReduction, another.dimensionalityReduction);
  }

  /**
   * Computes a hash code from attributes: {@code clustering}, {@code validation}, {@code dimensionalityReduction}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + Objects.hashCode(clustering);
    h += (h << 5) + Objects.hashCode(validation);
    h += (h << 5) + Objects.hashCode(dimensionalityReduction);
    return h;
  }

  /**
   * Prints the immutable value {@code ClusteringMethod} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ClusteringMethod")
        .omitNullValues()
        .add("clustering", clustering)
        .add("validation", validation)
        .add("dimensionalityReduction", dimensionalityReduction)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "ClusteringMethod", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements ClusteringMethod {
    @Nullable ClusteringMethodEnum clustering;
    @Nullable List<ValidationMetricsEnum> validation = null;
    @Nullable DimensionalityReductionEnum dimensionalityReduction;
    @JsonProperty("clustering")
    public void setClustering(@Nullable ClusteringMethodEnum clustering) {
      this.clustering = clustering;
    }
    @JsonProperty("validation")
    public void setValidation(@Nullable List<ValidationMetricsEnum> validation) {
      this.validation = validation;
    }
    @JsonProperty("dimensionality_reduction")
    public void setDimensionalityReduction(@Nullable DimensionalityReductionEnum dimensionalityReduction) {
      this.dimensionalityReduction = dimensionalityReduction;
    }
    @Override
    public ClusteringMethodEnum getClustering() { throw new UnsupportedOperationException(); }
    @Override
    public List<ValidationMetricsEnum> getValidation() { throw new UnsupportedOperationException(); }
    @Override
    public DimensionalityReductionEnum getDimensionalityReduction() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableClusteringMethod fromJson(Json json) {
    ImmutableClusteringMethod.Builder builder = ImmutableClusteringMethod.builder();
    if (json.clustering != null) {
      builder.clustering(json.clustering);
    }
    if (json.validation != null) {
      builder.addAllValidation(json.validation);
    }
    if (json.dimensionalityReduction != null) {
      builder.dimensionalityReduction(json.dimensionalityReduction);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link ClusteringMethod} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ClusteringMethod instance
   */
  public static ImmutableClusteringMethod copyOf(ClusteringMethod instance) {
    if (instance instanceof ImmutableClusteringMethod) {
      return (ImmutableClusteringMethod) instance;
    }
    return ImmutableClusteringMethod.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableClusteringMethod ImmutableClusteringMethod}.
   * <pre>
   * ImmutableClusteringMethod.builder()
   *    .clustering(org.greencloud.dataanalysisapi.enums.ClusteringMethodEnum | null) // nullable {@link ClusteringMethod#getClustering() clustering}
   *    .validation(List&amp;lt;org.greencloud.dataanalysisapi.enums.ValidationMetricsEnum&amp;gt; | null) // nullable {@link ClusteringMethod#getValidation() validation}
   *    .dimensionalityReduction(org.greencloud.dataanalysisapi.enums.DimensionalityReductionEnum | null) // nullable {@link ClusteringMethod#getDimensionalityReduction() dimensionalityReduction}
   *    .build();
   * </pre>
   * @return A new ImmutableClusteringMethod builder
   */
  public static ImmutableClusteringMethod.Builder builder() {
    return new ImmutableClusteringMethod.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableClusteringMethod ImmutableClusteringMethod}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ClusteringMethod", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private @Nullable ClusteringMethodEnum clustering;
    private ImmutableList.Builder<ValidationMetricsEnum> validation = null;
    private @Nullable DimensionalityReductionEnum dimensionalityReduction;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code ClusteringMethod} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ClusteringMethod instance) {
      Objects.requireNonNull(instance, "instance");
      @Nullable ClusteringMethodEnum clusteringValue = instance.getClustering();
      if (clusteringValue != null) {
        clustering(clusteringValue);
      }
      @Nullable List<ValidationMetricsEnum> validationValue = instance.getValidation();
      if (validationValue != null) {
        addAllValidation(validationValue);
      }
      @Nullable DimensionalityReductionEnum dimensionalityReductionValue = instance.getDimensionalityReduction();
      if (dimensionalityReductionValue != null) {
        dimensionalityReduction(dimensionalityReductionValue);
      }
      return this;
    }

    /**
     * Initializes the value for the {@link ClusteringMethod#getClustering() clustering} attribute.
     * @param clustering The value for clustering (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("clustering")
    public final Builder clustering(@Nullable ClusteringMethodEnum clustering) {
      this.clustering = clustering;
      return this;
    }

    /**
     * Adds one element to {@link ClusteringMethod#getValidation() validation} list.
     * @param element A validation element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addValidation(ValidationMetricsEnum element) {
      if (this.validation == null) {
        this.validation = ImmutableList.builder();
      }
      this.validation.add(element);
      return this;
    }

    /**
     * Adds elements to {@link ClusteringMethod#getValidation() validation} list.
     * @param elements An array of validation elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addValidation(ValidationMetricsEnum... elements) {
      if (this.validation == null) {
        this.validation = ImmutableList.builder();
      }
      this.validation.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link ClusteringMethod#getValidation() validation} list.
     * @param elements An iterable of validation elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("validation")
    public final Builder validation(@Nullable Iterable<? extends ValidationMetricsEnum> elements) {
      if (elements == null) {
        this.validation = null;
        return this;
      }
      this.validation = ImmutableList.builder();
      return addAllValidation(elements);
    }

    /**
     * Adds elements to {@link ClusteringMethod#getValidation() validation} list.
     * @param elements An iterable of validation elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllValidation(Iterable<? extends ValidationMetricsEnum> elements) {
      Objects.requireNonNull(elements, "validation element");
      if (this.validation == null) {
        this.validation = ImmutableList.builder();
      }
      this.validation.addAll(elements);
      return this;
    }

    /**
     * Initializes the value for the {@link ClusteringMethod#getDimensionalityReduction() dimensionalityReduction} attribute.
     * @param dimensionalityReduction The value for dimensionalityReduction (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("dimensionality_reduction")
    public final Builder dimensionalityReduction(@Nullable DimensionalityReductionEnum dimensionalityReduction) {
      this.dimensionalityReduction = dimensionalityReduction;
      return this;
    }

    /**
     * Builds a new {@link ImmutableClusteringMethod ImmutableClusteringMethod}.
     * @return An immutable instance of ClusteringMethod
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableClusteringMethod build() {
      return new ImmutableClusteringMethod(clustering, validation == null ? null : validation.build(), dimensionalityReduction);
    }
  }
}
