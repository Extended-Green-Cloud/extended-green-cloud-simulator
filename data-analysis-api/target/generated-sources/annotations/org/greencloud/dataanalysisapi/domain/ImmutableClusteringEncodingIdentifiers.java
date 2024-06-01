package org.greencloud.dataanalysisapi.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link ClusteringEncodingIdentifiers}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableClusteringEncodingIdentifiers.builder()}.
 */
@Generated(from = "ClusteringEncodingIdentifiers", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableClusteringEncodingIdentifiers
    implements ClusteringEncodingIdentifiers {
  private final ImmutableMap<String, List<Map<String, Object>>> elementsPerCluster;
  private final ImmutableMap<String, String> clusterEncoding;

  private ImmutableClusteringEncodingIdentifiers(
      ImmutableMap<String, List<Map<String, Object>>> elementsPerCluster,
      ImmutableMap<String, String> clusterEncoding) {
    this.elementsPerCluster = elementsPerCluster;
    this.clusterEncoding = clusterEncoding;
  }

  /**
   * @return resources assigned to clusters
   */
  @JsonProperty("elementsPerCluster")
  @Override
  public ImmutableMap<String, List<Map<String, Object>>> getElementsPerCluster() {
    return elementsPerCluster;
  }

  /**
   * @return clusters encoding
   */
  @JsonProperty("clusterEncoding")
  @Override
  public ImmutableMap<String, String> getClusterEncoding() {
    return clusterEncoding;
  }

  /**
   * Copy the current immutable object by replacing the {@link ClusteringEncodingIdentifiers#getElementsPerCluster() elementsPerCluster} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the elementsPerCluster map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringEncodingIdentifiers withElementsPerCluster(Map<String, ? extends List<Map<String, Object>>> entries) {
    if (this.elementsPerCluster == entries) return this;
    ImmutableMap<String, List<Map<String, Object>>> newValue = ImmutableMap.copyOf(entries);
    return new ImmutableClusteringEncodingIdentifiers(newValue, this.clusterEncoding);
  }

  /**
   * Copy the current immutable object by replacing the {@link ClusteringEncodingIdentifiers#getClusterEncoding() clusterEncoding} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the clusterEncoding map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringEncodingIdentifiers withClusterEncoding(Map<String, ? extends String> entries) {
    if (this.clusterEncoding == entries) return this;
    ImmutableMap<String, String> newValue = ImmutableMap.copyOf(entries);
    return new ImmutableClusteringEncodingIdentifiers(this.elementsPerCluster, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableClusteringEncodingIdentifiers} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableClusteringEncodingIdentifiers
        && equalTo(0, (ImmutableClusteringEncodingIdentifiers) another);
  }

  private boolean equalTo(int synthetic, ImmutableClusteringEncodingIdentifiers another) {
    return elementsPerCluster.equals(another.elementsPerCluster)
        && clusterEncoding.equals(another.clusterEncoding);
  }

  /**
   * Computes a hash code from attributes: {@code elementsPerCluster}, {@code clusterEncoding}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + elementsPerCluster.hashCode();
    h += (h << 5) + clusterEncoding.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code ClusteringEncodingIdentifiers} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ClusteringEncodingIdentifiers")
        .omitNullValues()
        .add("elementsPerCluster", elementsPerCluster)
        .add("clusterEncoding", clusterEncoding)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "ClusteringEncodingIdentifiers", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements ClusteringEncodingIdentifiers {
    @Nullable Map<String, List<Map<String, Object>>> elementsPerCluster = ImmutableMap.of();
    @Nullable Map<String, String> clusterEncoding = ImmutableMap.of();
    @JsonProperty("elementsPerCluster")
    public void setElementsPerCluster(Map<String, List<Map<String, Object>>> elementsPerCluster) {
      this.elementsPerCluster = elementsPerCluster;
    }
    @JsonProperty("clusterEncoding")
    public void setClusterEncoding(Map<String, String> clusterEncoding) {
      this.clusterEncoding = clusterEncoding;
    }
    @Override
    public Map<String, List<Map<String, Object>>> getElementsPerCluster() { throw new UnsupportedOperationException(); }
    @Override
    public Map<String, String> getClusterEncoding() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableClusteringEncodingIdentifiers fromJson(Json json) {
    ImmutableClusteringEncodingIdentifiers.Builder builder = ImmutableClusteringEncodingIdentifiers.builder();
    if (json.elementsPerCluster != null) {
      builder.putAllElementsPerCluster(json.elementsPerCluster);
    }
    if (json.clusterEncoding != null) {
      builder.putAllClusterEncoding(json.clusterEncoding);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link ClusteringEncodingIdentifiers} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ClusteringEncodingIdentifiers instance
   */
  public static ImmutableClusteringEncodingIdentifiers copyOf(ClusteringEncodingIdentifiers instance) {
    if (instance instanceof ImmutableClusteringEncodingIdentifiers) {
      return (ImmutableClusteringEncodingIdentifiers) instance;
    }
    return ImmutableClusteringEncodingIdentifiers.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableClusteringEncodingIdentifiers ImmutableClusteringEncodingIdentifiers}.
   * <pre>
   * ImmutableClusteringEncodingIdentifiers.builder()
   *    .putElementsPerCluster|putAllElementsPerCluster(String =&gt; List&amp;lt;Map&amp;lt;String, Object&amp;gt;&amp;gt;) // {@link ClusteringEncodingIdentifiers#getElementsPerCluster() elementsPerCluster} mappings
   *    .putClusterEncoding|putAllClusterEncoding(String =&gt; String) // {@link ClusteringEncodingIdentifiers#getClusterEncoding() clusterEncoding} mappings
   *    .build();
   * </pre>
   * @return A new ImmutableClusteringEncodingIdentifiers builder
   */
  public static ImmutableClusteringEncodingIdentifiers.Builder builder() {
    return new ImmutableClusteringEncodingIdentifiers.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableClusteringEncodingIdentifiers ImmutableClusteringEncodingIdentifiers}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ClusteringEncodingIdentifiers", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private ImmutableMap.Builder<String, List<Map<String, Object>>> elementsPerCluster = ImmutableMap.builder();
    private ImmutableMap.Builder<String, String> clusterEncoding = ImmutableMap.builder();

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code ClusteringEncodingIdentifiers} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ClusteringEncodingIdentifiers instance) {
      Objects.requireNonNull(instance, "instance");
      putAllElementsPerCluster(instance.getElementsPerCluster());
      putAllClusterEncoding(instance.getClusterEncoding());
      return this;
    }

    /**
     * Put one entry to the {@link ClusteringEncodingIdentifiers#getElementsPerCluster() elementsPerCluster} map.
     * @param key The key in the elementsPerCluster map
     * @param value The associated value in the elementsPerCluster map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putElementsPerCluster(String key, List<Map<String, Object>> value) {
      this.elementsPerCluster.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link ClusteringEncodingIdentifiers#getElementsPerCluster() elementsPerCluster} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putElementsPerCluster(Map.Entry<String, ? extends List<Map<String, Object>>> entry) {
      this.elementsPerCluster.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link ClusteringEncodingIdentifiers#getElementsPerCluster() elementsPerCluster} map. Nulls are not permitted
     * @param entries The entries that will be added to the elementsPerCluster map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("elementsPerCluster")
    public final Builder elementsPerCluster(Map<String, ? extends List<Map<String, Object>>> entries) {
      this.elementsPerCluster = ImmutableMap.builder();
      return putAllElementsPerCluster(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link ClusteringEncodingIdentifiers#getElementsPerCluster() elementsPerCluster} map. Nulls are not permitted
     * @param entries The entries that will be added to the elementsPerCluster map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putAllElementsPerCluster(Map<String, ? extends List<Map<String, Object>>> entries) {
      this.elementsPerCluster.putAll(entries);
      return this;
    }

    /**
     * Put one entry to the {@link ClusteringEncodingIdentifiers#getClusterEncoding() clusterEncoding} map.
     * @param key The key in the clusterEncoding map
     * @param value The associated value in the clusterEncoding map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putClusterEncoding(String key, String value) {
      this.clusterEncoding.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link ClusteringEncodingIdentifiers#getClusterEncoding() clusterEncoding} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putClusterEncoding(Map.Entry<String, ? extends String> entry) {
      this.clusterEncoding.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link ClusteringEncodingIdentifiers#getClusterEncoding() clusterEncoding} map. Nulls are not permitted
     * @param entries The entries that will be added to the clusterEncoding map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("clusterEncoding")
    public final Builder clusterEncoding(Map<String, ? extends String> entries) {
      this.clusterEncoding = ImmutableMap.builder();
      return putAllClusterEncoding(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link ClusteringEncodingIdentifiers#getClusterEncoding() clusterEncoding} map. Nulls are not permitted
     * @param entries The entries that will be added to the clusterEncoding map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putAllClusterEncoding(Map<String, ? extends String> entries) {
      this.clusterEncoding.putAll(entries);
      return this;
    }

    /**
     * Builds a new {@link ImmutableClusteringEncodingIdentifiers ImmutableClusteringEncodingIdentifiers}.
     * @return An immutable instance of ClusteringEncodingIdentifiers
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableClusteringEncodingIdentifiers build() {
      return new ImmutableClusteringEncodingIdentifiers(elementsPerCluster.build(), clusterEncoding.build());
    }
  }
}
