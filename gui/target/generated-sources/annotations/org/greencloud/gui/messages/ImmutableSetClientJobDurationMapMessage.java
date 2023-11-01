package org.greencloud.gui.messages;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.greencloud.commons.enums.job.JobClientStatusEnum;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link SetClientJobDurationMapMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableSetClientJobDurationMapMessage.builder()}.
 */
@Generated(from = "SetClientJobDurationMapMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableSetClientJobDurationMapMessage
    implements SetClientJobDurationMapMessage {
  private final ImmutableMap<JobClientStatusEnum, Long> data;
  private final String agentName;

  private ImmutableSetClientJobDurationMapMessage(
      ImmutableMap<JobClientStatusEnum, Long> data,
      String agentName) {
    this.data = data;
    this.agentName = agentName;
  }

  /**
   * @return The value of the {@code data} attribute
   */
  @JsonProperty("data")
  @Override
  public ImmutableMap<JobClientStatusEnum, Long> getData() {
    return data;
  }

  /**
   * @return The value of the {@code agentName} attribute
   */
  @JsonProperty("agentName")
  @Override
  public String getAgentName() {
    return agentName;
  }

  /**
   * Copy the current immutable object by replacing the {@link SetClientJobDurationMapMessage#getData() data} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the data map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableSetClientJobDurationMapMessage withData(Map<JobClientStatusEnum, ? extends Long> entries) {
    if (this.data == entries) return this;
    ImmutableMap<JobClientStatusEnum, Long> newValue = Maps.immutableEnumMap(entries);
    return new ImmutableSetClientJobDurationMapMessage(newValue, this.agentName);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link SetClientJobDurationMapMessage#getAgentName() agentName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableSetClientJobDurationMapMessage withAgentName(String value) {
    String newValue = Objects.requireNonNull(value, "agentName");
    if (this.agentName.equals(newValue)) return this;
    return new ImmutableSetClientJobDurationMapMessage(this.data, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableSetClientJobDurationMapMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableSetClientJobDurationMapMessage
        && equalTo(0, (ImmutableSetClientJobDurationMapMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableSetClientJobDurationMapMessage another) {
    return data.equals(another.data)
        && agentName.equals(another.agentName);
  }

  /**
   * Computes a hash code from attributes: {@code data}, {@code agentName}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + data.hashCode();
    h += (h << 5) + agentName.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code SetClientJobDurationMapMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("SetClientJobDurationMapMessage")
        .omitNullValues()
        .add("data", data)
        .add("agentName", agentName)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "SetClientJobDurationMapMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements SetClientJobDurationMapMessage {
    @Nullable Map<JobClientStatusEnum, Long> data = ImmutableMap.of();
    @Nullable String agentName;
    @JsonProperty("data")
    public void setData(Map<JobClientStatusEnum, Long> data) {
      this.data = data;
    }
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
      this.agentName = agentName;
    }
    @Override
    public Map<JobClientStatusEnum, Long> getData() { throw new UnsupportedOperationException(); }
    @Override
    public String getAgentName() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableSetClientJobDurationMapMessage fromJson(Json json) {
    ImmutableSetClientJobDurationMapMessage.Builder builder = ImmutableSetClientJobDurationMapMessage.builder();
    if (json.data != null) {
      builder.putAllData(json.data);
    }
    if (json.agentName != null) {
      builder.agentName(json.agentName);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link SetClientJobDurationMapMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable SetClientJobDurationMapMessage instance
   */
  public static ImmutableSetClientJobDurationMapMessage copyOf(SetClientJobDurationMapMessage instance) {
    if (instance instanceof ImmutableSetClientJobDurationMapMessage) {
      return (ImmutableSetClientJobDurationMapMessage) instance;
    }
    return ImmutableSetClientJobDurationMapMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableSetClientJobDurationMapMessage ImmutableSetClientJobDurationMapMessage}.
   * <pre>
   * ImmutableSetClientJobDurationMapMessage.builder()
   *    .putData|putAllData(org.greencloud.commons.enums.job.JobClientStatusEnum =&gt; long) // {@link SetClientJobDurationMapMessage#getData() data} mappings
   *    .agentName(String) // required {@link SetClientJobDurationMapMessage#getAgentName() agentName}
   *    .build();
   * </pre>
   * @return A new ImmutableSetClientJobDurationMapMessage builder
   */
  public static ImmutableSetClientJobDurationMapMessage.Builder builder() {
    return new ImmutableSetClientJobDurationMapMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableSetClientJobDurationMapMessage ImmutableSetClientJobDurationMapMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "SetClientJobDurationMapMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_AGENT_NAME = 0x1L;
    private long initBits = 0x1L;

    private ImmutableMap.Builder<JobClientStatusEnum, Long> data = ImmutableMap.builder();
    private @Nullable String agentName;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code SetClientJobDurationMapMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(SetClientJobDurationMapMessage instance) {
      Objects.requireNonNull(instance, "instance");
      putAllData(instance.getData());
      agentName(instance.getAgentName());
      return this;
    }

    /**
     * Put one entry to the {@link SetClientJobDurationMapMessage#getData() data} map.
     * @param key The key in the data map
     * @param value The associated value in the data map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putData(JobClientStatusEnum key, long value) {
      this.data.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link SetClientJobDurationMapMessage#getData() data} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putData(Map.Entry<JobClientStatusEnum, ? extends Long> entry) {
      this.data.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link SetClientJobDurationMapMessage#getData() data} map. Nulls are not permitted
     * @param entries The entries that will be added to the data map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(Map<JobClientStatusEnum, ? extends Long> entries) {
      this.data = ImmutableMap.builder();
      return putAllData(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link SetClientJobDurationMapMessage#getData() data} map. Nulls are not permitted
     * @param entries The entries that will be added to the data map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putAllData(Map<JobClientStatusEnum, ? extends Long> entries) {
      this.data.putAll(entries);
      return this;
    }

    /**
     * Initializes the value for the {@link SetClientJobDurationMapMessage#getAgentName() agentName} attribute.
     * @param agentName The value for agentName 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("agentName")
    public final Builder agentName(String agentName) {
      this.agentName = Objects.requireNonNull(agentName, "agentName");
      initBits &= ~INIT_BIT_AGENT_NAME;
      return this;
    }

    /**
     * Builds a new {@link ImmutableSetClientJobDurationMapMessage ImmutableSetClientJobDurationMapMessage}.
     * @return An immutable instance of SetClientJobDurationMapMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableSetClientJobDurationMapMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableSetClientJobDurationMapMessage(Maps.immutableEnumMap(data.build()), agentName);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_AGENT_NAME) != 0) attributes.add("agentName");
      return "Cannot build SetClientJobDurationMapMessage, some of required attributes are not set " + attributes;
    }
  }
}
