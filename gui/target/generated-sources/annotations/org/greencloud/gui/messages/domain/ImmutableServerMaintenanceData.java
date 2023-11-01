package org.greencloud.gui.messages.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link ServerMaintenanceData}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableServerMaintenanceData.builder()}.
 */
@Generated(from = "ServerMaintenanceData", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableServerMaintenanceData
    implements ServerMaintenanceData {
  private final Instant occurrenceTime;
  private final @Nullable Boolean isFinished;
  private final ImmutableMap<String, Resource> newResources;

  private ImmutableServerMaintenanceData(
      Instant occurrenceTime,
      @Nullable Boolean isFinished,
      ImmutableMap<String, Resource> newResources) {
    this.occurrenceTime = occurrenceTime;
    this.isFinished = isFinished;
    this.newResources = newResources;
  }

  /**
   * @return The value of the {@code occurrenceTime} attribute
   */
  @JsonProperty("occurrenceTime")
  @Override
  public Instant getOccurrenceTime() {
    return occurrenceTime;
  }

  /**
   * @return The value of the {@code isFinished} attribute
   */
  @JsonProperty("isFinished")
  @Override
  public @Nullable Boolean isFinished() {
    return isFinished;
  }

  /**
   * @return The value of the {@code newResources} attribute
   */
  @JsonProperty("newResources")
  @Override
  public ImmutableMap<String, Resource> getNewResources() {
    return newResources;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ServerMaintenanceData#getOccurrenceTime() occurrenceTime} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for occurrenceTime
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableServerMaintenanceData withOccurrenceTime(Instant value) {
    if (this.occurrenceTime == value) return this;
    Instant newValue = Objects.requireNonNull(value, "occurrenceTime");
    return new ImmutableServerMaintenanceData(newValue, this.isFinished, this.newResources);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ServerMaintenanceData#isFinished() isFinished} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for isFinished (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableServerMaintenanceData withIsFinished(@Nullable Boolean value) {
    if (Objects.equals(this.isFinished, value)) return this;
    return new ImmutableServerMaintenanceData(this.occurrenceTime, value, this.newResources);
  }

  /**
   * Copy the current immutable object by replacing the {@link ServerMaintenanceData#getNewResources() newResources} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the newResources map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableServerMaintenanceData withNewResources(Map<String, ? extends Resource> entries) {
    if (this.newResources == entries) return this;
    ImmutableMap<String, Resource> newValue = ImmutableMap.copyOf(entries);
    return new ImmutableServerMaintenanceData(this.occurrenceTime, this.isFinished, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableServerMaintenanceData} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableServerMaintenanceData
        && equalTo(0, (ImmutableServerMaintenanceData) another);
  }

  private boolean equalTo(int synthetic, ImmutableServerMaintenanceData another) {
    return occurrenceTime.equals(another.occurrenceTime)
        && Objects.equals(isFinished, another.isFinished)
        && newResources.equals(another.newResources);
  }

  /**
   * Computes a hash code from attributes: {@code occurrenceTime}, {@code isFinished}, {@code newResources}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + occurrenceTime.hashCode();
    h += (h << 5) + Objects.hashCode(isFinished);
    h += (h << 5) + newResources.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code ServerMaintenanceData} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ServerMaintenanceData")
        .omitNullValues()
        .add("occurrenceTime", occurrenceTime)
        .add("isFinished", isFinished)
        .add("newResources", newResources)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "ServerMaintenanceData", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements ServerMaintenanceData {
    @Nullable Instant occurrenceTime;
    @Nullable Boolean isFinished;
    @Nullable Map<String, Resource> newResources = ImmutableMap.of();
    @JsonProperty("occurrenceTime")
    public void setOccurrenceTime(Instant occurrenceTime) {
      this.occurrenceTime = occurrenceTime;
    }
    @JsonProperty("isFinished")
    public void setIsFinished(@Nullable Boolean isFinished) {
      this.isFinished = isFinished;
    }
    @JsonProperty("newResources")
    public void setNewResources(Map<String, Resource> newResources) {
      this.newResources = newResources;
    }
    @Override
    public Instant getOccurrenceTime() { throw new UnsupportedOperationException(); }
    @Override
    public Boolean isFinished() { throw new UnsupportedOperationException(); }
    @Override
    public Map<String, Resource> getNewResources() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableServerMaintenanceData fromJson(Json json) {
    ImmutableServerMaintenanceData.Builder builder = ImmutableServerMaintenanceData.builder();
    if (json.occurrenceTime != null) {
      builder.occurrenceTime(json.occurrenceTime);
    }
    if (json.isFinished != null) {
      builder.isFinished(json.isFinished);
    }
    if (json.newResources != null) {
      builder.putAllNewResources(json.newResources);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link ServerMaintenanceData} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ServerMaintenanceData instance
   */
  public static ImmutableServerMaintenanceData copyOf(ServerMaintenanceData instance) {
    if (instance instanceof ImmutableServerMaintenanceData) {
      return (ImmutableServerMaintenanceData) instance;
    }
    return ImmutableServerMaintenanceData.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableServerMaintenanceData ImmutableServerMaintenanceData}.
   * <pre>
   * ImmutableServerMaintenanceData.builder()
   *    .occurrenceTime(java.time.Instant) // required {@link ServerMaintenanceData#getOccurrenceTime() occurrenceTime}
   *    .isFinished(Boolean | null) // nullable {@link ServerMaintenanceData#isFinished() isFinished}
   *    .putNewResources|putAllNewResources(String =&gt; org.greencloud.commons.domain.resources.Resource) // {@link ServerMaintenanceData#getNewResources() newResources} mappings
   *    .build();
   * </pre>
   * @return A new ImmutableServerMaintenanceData builder
   */
  public static ImmutableServerMaintenanceData.Builder builder() {
    return new ImmutableServerMaintenanceData.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableServerMaintenanceData ImmutableServerMaintenanceData}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ServerMaintenanceData", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_OCCURRENCE_TIME = 0x1L;
    private long initBits = 0x1L;

    private @Nullable Instant occurrenceTime;
    private @Nullable Boolean isFinished;
    private ImmutableMap.Builder<String, Resource> newResources = ImmutableMap.builder();

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.greencloud.gui.messages.domain.ServerMaintenanceData} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ServerMaintenanceData instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.greencloud.gui.messages.domain.EventData} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(EventData instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    private void from(Object object) {
      @Var long bits = 0;
      if (object instanceof ServerMaintenanceData) {
        ServerMaintenanceData instance = (ServerMaintenanceData) object;
        if ((bits & 0x2L) == 0) {
          @Nullable Boolean isFinishedValue = instance.isFinished();
          if (isFinishedValue != null) {
            isFinished(isFinishedValue);
          }
          bits |= 0x2L;
        }
        if ((bits & 0x1L) == 0) {
          occurrenceTime(instance.getOccurrenceTime());
          bits |= 0x1L;
        }
        putAllNewResources(instance.getNewResources());
      }
      if (object instanceof EventData) {
        EventData instance = (EventData) object;
        if ((bits & 0x2L) == 0) {
          @Nullable Boolean isFinishedValue = instance.isFinished();
          if (isFinishedValue != null) {
            isFinished(isFinishedValue);
          }
          bits |= 0x2L;
        }
        if ((bits & 0x1L) == 0) {
          occurrenceTime(instance.getOccurrenceTime());
          bits |= 0x1L;
        }
      }
    }

    /**
     * Initializes the value for the {@link ServerMaintenanceData#getOccurrenceTime() occurrenceTime} attribute.
     * @param occurrenceTime The value for occurrenceTime 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("occurrenceTime")
    public final Builder occurrenceTime(Instant occurrenceTime) {
      this.occurrenceTime = Objects.requireNonNull(occurrenceTime, "occurrenceTime");
      initBits &= ~INIT_BIT_OCCURRENCE_TIME;
      return this;
    }

    /**
     * Initializes the value for the {@link ServerMaintenanceData#isFinished() isFinished} attribute.
     * @param isFinished The value for isFinished (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("isFinished")
    public final Builder isFinished(@Nullable Boolean isFinished) {
      this.isFinished = isFinished;
      return this;
    }

    /**
     * Put one entry to the {@link ServerMaintenanceData#getNewResources() newResources} map.
     * @param key The key in the newResources map
     * @param value The associated value in the newResources map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putNewResources(String key, Resource value) {
      this.newResources.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link ServerMaintenanceData#getNewResources() newResources} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putNewResources(Map.Entry<String, ? extends Resource> entry) {
      this.newResources.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link ServerMaintenanceData#getNewResources() newResources} map. Nulls are not permitted
     * @param entries The entries that will be added to the newResources map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("newResources")
    public final Builder newResources(Map<String, ? extends Resource> entries) {
      this.newResources = ImmutableMap.builder();
      return putAllNewResources(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link ServerMaintenanceData#getNewResources() newResources} map. Nulls are not permitted
     * @param entries The entries that will be added to the newResources map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putAllNewResources(Map<String, ? extends Resource> entries) {
      this.newResources.putAll(entries);
      return this;
    }

    /**
     * Builds a new {@link ImmutableServerMaintenanceData ImmutableServerMaintenanceData}.
     * @return An immutable instance of ServerMaintenanceData
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableServerMaintenanceData build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableServerMaintenanceData(occurrenceTime, isFinished, newResources.build());
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_OCCURRENCE_TIME) != 0) attributes.add("occurrenceTime");
      return "Cannot build ServerMaintenanceData, some of required attributes are not set " + attributes;
    }
  }
}
