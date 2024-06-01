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
 * Immutable implementation of {@link ClusteringEncodingResponse}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableClusteringEncodingResponse.builder()}.
 */
@Generated(from = "ClusteringEncodingResponse", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableClusteringEncodingResponse
    implements ClusteringEncodingResponse {
  private final ImmutableMap<String, List<Map<String, Object>>> clustering;
  private final ImmutableMap<String, String> encoding;

  private ImmutableClusteringEncodingResponse(
      ImmutableMap<String, List<Map<String, Object>>> clustering,
      ImmutableMap<String, String> encoding) {
    this.clustering = clustering;
    this.encoding = encoding;
  }

  /**
   * @return resources assigned to clusters
   */
  @JsonProperty("clustering")
  @Override
  public ImmutableMap<String, List<Map<String, Object>>> getClustering() {
    return clustering;
  }

  /**
   * @return clusters encoding
   */
  @JsonProperty("encoding")
  @Override
  public ImmutableMap<String, String> getEncoding() {
    return encoding;
  }

  /**
   * Copy the current immutable object by replacing the {@link ClusteringEncodingResponse#getClustering() clustering} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the clustering map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringEncodingResponse withClustering(Map<String, ? extends List<Map<String, Object>>> entries) {
    if (this.clustering == entries) return this;
    ImmutableMap<String, List<Map<String, Object>>> newValue = ImmutableMap.copyOf(entries);
    return new ImmutableClusteringEncodingResponse(newValue, this.encoding);
  }

  /**
   * Copy the current immutable object by replacing the {@link ClusteringEncodingResponse#getEncoding() encoding} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the encoding map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableClusteringEncodingResponse withEncoding(Map<String, ? extends String> entries) {
    if (this.encoding == entries) return this;
    ImmutableMap<String, String> newValue = ImmutableMap.copyOf(entries);
    return new ImmutableClusteringEncodingResponse(this.clustering, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableClusteringEncodingResponse} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableClusteringEncodingResponse
        && equalTo(0, (ImmutableClusteringEncodingResponse) another);
  }

  private boolean equalTo(int synthetic, ImmutableClusteringEncodingResponse another) {
    return clustering.equals(another.clustering)
        && encoding.equals(another.encoding);
  }

  /**
   * Computes a hash code from attributes: {@code clustering}, {@code encoding}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + clustering.hashCode();
    h += (h << 5) + encoding.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code ClusteringEncodingResponse} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ClusteringEncodingResponse")
        .omitNullValues()
        .add("clustering", clustering)
        .add("encoding", encoding)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "ClusteringEncodingResponse", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements ClusteringEncodingResponse {
    @Nullable Map<String, List<Map<String, Object>>> clustering = ImmutableMap.of();
    @Nullable Map<String, String> encoding = ImmutableMap.of();
    @JsonProperty("clustering")
    public void setClustering(Map<String, List<Map<String, Object>>> clustering) {
      this.clustering = clustering;
    }
    @JsonProperty("encoding")
    public void setEncoding(Map<String, String> encoding) {
      this.encoding = encoding;
    }
    @Override
    public Map<String, List<Map<String, Object>>> getClustering() { throw new UnsupportedOperationException(); }
    @Override
    public Map<String, String> getEncoding() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableClusteringEncodingResponse fromJson(Json json) {
    ImmutableClusteringEncodingResponse.Builder builder = ImmutableClusteringEncodingResponse.builder();
    if (json.clustering != null) {
      builder.putAllClustering(json.clustering);
    }
    if (json.encoding != null) {
      builder.putAllEncoding(json.encoding);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link ClusteringEncodingResponse} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ClusteringEncodingResponse instance
   */
  public static ImmutableClusteringEncodingResponse copyOf(ClusteringEncodingResponse instance) {
    if (instance instanceof ImmutableClusteringEncodingResponse) {
      return (ImmutableClusteringEncodingResponse) instance;
    }
    return ImmutableClusteringEncodingResponse.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableClusteringEncodingResponse ImmutableClusteringEncodingResponse}.
   * <pre>
   * ImmutableClusteringEncodingResponse.builder()
   *    .putClustering|putAllClustering(String =&gt; List&amp;lt;Map&amp;lt;String, Object&amp;gt;&amp;gt;) // {@link ClusteringEncodingResponse#getClustering() clustering} mappings
   *    .putEncoding|putAllEncoding(String =&gt; String) // {@link ClusteringEncodingResponse#getEncoding() encoding} mappings
   *    .build();
   * </pre>
   * @return A new ImmutableClusteringEncodingResponse builder
   */
  public static ImmutableClusteringEncodingResponse.Builder builder() {
    return new ImmutableClusteringEncodingResponse.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableClusteringEncodingResponse ImmutableClusteringEncodingResponse}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ClusteringEncodingResponse", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private ImmutableMap.Builder<String, List<Map<String, Object>>> clustering = ImmutableMap.builder();
    private ImmutableMap.Builder<String, String> encoding = ImmutableMap.builder();

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code ClusteringEncodingResponse} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ClusteringEncodingResponse instance) {
      Objects.requireNonNull(instance, "instance");
      putAllClustering(instance.getClustering());
      putAllEncoding(instance.getEncoding());
      return this;
    }

    /**
     * Put one entry to the {@link ClusteringEncodingResponse#getClustering() clustering} map.
     * @param key The key in the clustering map
     * @param value The associated value in the clustering map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putClustering(String key, List<Map<String, Object>> value) {
      this.clustering.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link ClusteringEncodingResponse#getClustering() clustering} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putClustering(Map.Entry<String, ? extends List<Map<String, Object>>> entry) {
      this.clustering.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link ClusteringEncodingResponse#getClustering() clustering} map. Nulls are not permitted
     * @param entries The entries that will be added to the clustering map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("clustering")
    public final Builder clustering(Map<String, ? extends List<Map<String, Object>>> entries) {
      this.clustering = ImmutableMap.builder();
      return putAllClustering(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link ClusteringEncodingResponse#getClustering() clustering} map. Nulls are not permitted
     * @param entries The entries that will be added to the clustering map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putAllClustering(Map<String, ? extends List<Map<String, Object>>> entries) {
      this.clustering.putAll(entries);
      return this;
    }

    /**
     * Put one entry to the {@link ClusteringEncodingResponse#getEncoding() encoding} map.
     * @param key The key in the encoding map
     * @param value The associated value in the encoding map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putEncoding(String key, String value) {
      this.encoding.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link ClusteringEncodingResponse#getEncoding() encoding} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putEncoding(Map.Entry<String, ? extends String> entry) {
      this.encoding.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link ClusteringEncodingResponse#getEncoding() encoding} map. Nulls are not permitted
     * @param entries The entries that will be added to the encoding map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("encoding")
    public final Builder encoding(Map<String, ? extends String> entries) {
      this.encoding = ImmutableMap.builder();
      return putAllEncoding(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link ClusteringEncodingResponse#getEncoding() encoding} map. Nulls are not permitted
     * @param entries The entries that will be added to the encoding map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putAllEncoding(Map<String, ? extends String> entries) {
      this.encoding.putAll(entries);
      return this;
    }

    /**
     * Builds a new {@link ImmutableClusteringEncodingResponse ImmutableClusteringEncodingResponse}.
     * @return An immutable instance of ClusteringEncodingResponse
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableClusteringEncodingResponse build() {
      return new ImmutableClusteringEncodingResponse(clustering.build(), encoding.build());
    }
  }
}
