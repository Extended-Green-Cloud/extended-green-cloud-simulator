package org.greencloud.gui.messages.domain;

import com.database.knowledge.domain.action.AdaptationActionTypeEnum;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable implementation of {@link AdaptationLog}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableAdaptationLog.builder()}.
 */
@Generated(from = "AdaptationLog", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableAdaptationLog implements AdaptationLog {
  private final AdaptationActionTypeEnum type;
  private final String description;
  private final @Nullable String agentName;
  private final Instant time;

  private ImmutableAdaptationLog(
      AdaptationActionTypeEnum type,
      String description,
      @Nullable String agentName,
      Instant time) {
    this.type = type;
    this.description = description;
    this.agentName = agentName;
    this.time = time;
  }

  /**
   * @return type of the adaptation action
   */
  @JsonProperty("type")
  @Override
  public AdaptationActionTypeEnum getType() {
    return type;
  }

  /**
   * @return description of performed adaptation
   */
  @JsonProperty("description")
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * @return optional name of the agent on which the adaptation was performed
   */
  @JsonProperty("agentName")
  @Override
  public @Nullable String getAgentName() {
    return agentName;
  }

  /**
   * @return time when the adaptation was performed
   */
  @JsonProperty("time")
  @Override
  public Instant getTime() {
    return time;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link AdaptationLog#getType() type} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for type
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableAdaptationLog withType(AdaptationActionTypeEnum value) {
    AdaptationActionTypeEnum newValue = Objects.requireNonNull(value, "type");
    if (this.type == newValue) return this;
    return new ImmutableAdaptationLog(newValue, this.description, this.agentName, this.time);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link AdaptationLog#getDescription() description} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for description
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableAdaptationLog withDescription(String value) {
    String newValue = Objects.requireNonNull(value, "description");
    if (this.description.equals(newValue)) return this;
    return new ImmutableAdaptationLog(this.type, newValue, this.agentName, this.time);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link AdaptationLog#getAgentName() agentName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentName (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableAdaptationLog withAgentName(@Nullable String value) {
    if (Objects.equals(this.agentName, value)) return this;
    return new ImmutableAdaptationLog(this.type, this.description, value, this.time);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link AdaptationLog#getTime() time} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for time
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableAdaptationLog withTime(Instant value) {
    if (this.time == value) return this;
    Instant newValue = Objects.requireNonNull(value, "time");
    return new ImmutableAdaptationLog(this.type, this.description, this.agentName, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableAdaptationLog} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@javax.annotation.Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableAdaptationLog
        && equalTo(0, (ImmutableAdaptationLog) another);
  }

  private boolean equalTo(int synthetic, ImmutableAdaptationLog another) {
    return type.equals(another.type)
        && description.equals(another.description)
        && Objects.equals(agentName, another.agentName)
        && time.equals(another.time);
  }

  /**
   * Computes a hash code from attributes: {@code type}, {@code description}, {@code agentName}, {@code time}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + type.hashCode();
    h += (h << 5) + description.hashCode();
    h += (h << 5) + Objects.hashCode(agentName);
    h += (h << 5) + time.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code AdaptationLog} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("AdaptationLog")
        .omitNullValues()
        .add("type", type)
        .add("description", description)
        .add("agentName", agentName)
        .add("time", time)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "AdaptationLog", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements AdaptationLog {
    @javax.annotation.Nullable AdaptationActionTypeEnum type;
    @javax.annotation.Nullable String description;
    @javax.annotation.Nullable String agentName;
    @javax.annotation.Nullable Instant time;
    @JsonProperty("type")
    public void setType(AdaptationActionTypeEnum type) {
      this.type = type;
    }
    @JsonProperty("description")
    public void setDescription(String description) {
      this.description = description;
    }
    @JsonProperty("agentName")
    public void setAgentName(@Nullable String agentName) {
      this.agentName = agentName;
    }
    @JsonProperty("time")
    public void setTime(Instant time) {
      this.time = time;
    }
    @Override
    public AdaptationActionTypeEnum getType() { throw new UnsupportedOperationException(); }
    @Override
    public String getDescription() { throw new UnsupportedOperationException(); }
    @Override
    public String getAgentName() { throw new UnsupportedOperationException(); }
    @Override
    public Instant getTime() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableAdaptationLog fromJson(Json json) {
    ImmutableAdaptationLog.Builder builder = ImmutableAdaptationLog.builder();
    if (json.type != null) {
      builder.type(json.type);
    }
    if (json.description != null) {
      builder.description(json.description);
    }
    if (json.agentName != null) {
      builder.agentName(json.agentName);
    }
    if (json.time != null) {
      builder.time(json.time);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link AdaptationLog} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable AdaptationLog instance
   */
  public static ImmutableAdaptationLog copyOf(AdaptationLog instance) {
    if (instance instanceof ImmutableAdaptationLog) {
      return (ImmutableAdaptationLog) instance;
    }
    return ImmutableAdaptationLog.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableAdaptationLog ImmutableAdaptationLog}.
   * <pre>
   * ImmutableAdaptationLog.builder()
   *    .type(com.database.knowledge.domain.action.AdaptationActionTypeEnum) // required {@link AdaptationLog#getType() type}
   *    .description(String) // required {@link AdaptationLog#getDescription() description}
   *    .agentName(String | null) // nullable {@link AdaptationLog#getAgentName() agentName}
   *    .time(java.time.Instant) // required {@link AdaptationLog#getTime() time}
   *    .build();
   * </pre>
   * @return A new ImmutableAdaptationLog builder
   */
  public static ImmutableAdaptationLog.Builder builder() {
    return new ImmutableAdaptationLog.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableAdaptationLog ImmutableAdaptationLog}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "AdaptationLog", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_TYPE = 0x1L;
    private static final long INIT_BIT_DESCRIPTION = 0x2L;
    private static final long INIT_BIT_TIME = 0x4L;
    private long initBits = 0x7L;

    private @javax.annotation.Nullable AdaptationActionTypeEnum type;
    private @javax.annotation.Nullable String description;
    private @javax.annotation.Nullable String agentName;
    private @javax.annotation.Nullable Instant time;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code AdaptationLog} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(AdaptationLog instance) {
      Objects.requireNonNull(instance, "instance");
      type(instance.getType());
      description(instance.getDescription());
      @Nullable String agentNameValue = instance.getAgentName();
      if (agentNameValue != null) {
        agentName(agentNameValue);
      }
      time(instance.getTime());
      return this;
    }

    /**
     * Initializes the value for the {@link AdaptationLog#getType() type} attribute.
     * @param type The value for type 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("type")
    public final Builder type(AdaptationActionTypeEnum type) {
      this.type = Objects.requireNonNull(type, "type");
      initBits &= ~INIT_BIT_TYPE;
      return this;
    }

    /**
     * Initializes the value for the {@link AdaptationLog#getDescription() description} attribute.
     * @param description The value for description 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("description")
    public final Builder description(String description) {
      this.description = Objects.requireNonNull(description, "description");
      initBits &= ~INIT_BIT_DESCRIPTION;
      return this;
    }

    /**
     * Initializes the value for the {@link AdaptationLog#getAgentName() agentName} attribute.
     * @param agentName The value for agentName (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("agentName")
    public final Builder agentName(@Nullable String agentName) {
      this.agentName = agentName;
      return this;
    }

    /**
     * Initializes the value for the {@link AdaptationLog#getTime() time} attribute.
     * @param time The value for time 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("time")
    public final Builder time(Instant time) {
      this.time = Objects.requireNonNull(time, "time");
      initBits &= ~INIT_BIT_TIME;
      return this;
    }

    /**
     * Builds a new {@link ImmutableAdaptationLog ImmutableAdaptationLog}.
     * @return An immutable instance of AdaptationLog
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableAdaptationLog build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableAdaptationLog(type, description, agentName, time);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_TYPE) != 0) attributes.add("type");
      if ((initBits & INIT_BIT_DESCRIPTION) != 0) attributes.add("description");
      if ((initBits & INIT_BIT_TIME) != 0) attributes.add("time");
      return "Cannot build AdaptationLog, some of required attributes are not set " + attributes;
    }
  }
}
