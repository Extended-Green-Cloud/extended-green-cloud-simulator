package org.greencloud.gui.messages;

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
import org.greencloud.gui.messages.domain.JobTimeFrame;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link SetClientJobTimeFrameMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableSetClientJobTimeFrameMessage.builder()}.
 */
@Generated(from = "SetClientJobTimeFrameMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableSetClientJobTimeFrameMessage
    implements SetClientJobTimeFrameMessage {
  private final JobTimeFrame data;
  private final String agentName;

  private ImmutableSetClientJobTimeFrameMessage(JobTimeFrame data, String agentName) {
    this.data = data;
    this.agentName = agentName;
  }

  /**
   * @return The value of the {@code data} attribute
   */
  @JsonProperty("data")
  @Override
  public JobTimeFrame getData() {
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
   * Copy the current immutable object by setting a value for the {@link SetClientJobTimeFrameMessage#getData() data} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for data
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableSetClientJobTimeFrameMessage withData(JobTimeFrame value) {
    if (this.data == value) return this;
    JobTimeFrame newValue = Objects.requireNonNull(value, "data");
    return new ImmutableSetClientJobTimeFrameMessage(newValue, this.agentName);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link SetClientJobTimeFrameMessage#getAgentName() agentName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableSetClientJobTimeFrameMessage withAgentName(String value) {
    String newValue = Objects.requireNonNull(value, "agentName");
    if (this.agentName.equals(newValue)) return this;
    return new ImmutableSetClientJobTimeFrameMessage(this.data, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableSetClientJobTimeFrameMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableSetClientJobTimeFrameMessage
        && equalTo(0, (ImmutableSetClientJobTimeFrameMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableSetClientJobTimeFrameMessage another) {
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
   * Prints the immutable value {@code SetClientJobTimeFrameMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("SetClientJobTimeFrameMessage")
        .omitNullValues()
        .add("data", data)
        .add("agentName", agentName)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "SetClientJobTimeFrameMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements SetClientJobTimeFrameMessage {
    @Nullable JobTimeFrame data;
    @Nullable String agentName;
    @JsonProperty("data")
    public void setData(JobTimeFrame data) {
      this.data = data;
    }
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
      this.agentName = agentName;
    }
    @Override
    public JobTimeFrame getData() { throw new UnsupportedOperationException(); }
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
  static ImmutableSetClientJobTimeFrameMessage fromJson(Json json) {
    ImmutableSetClientJobTimeFrameMessage.Builder builder = ImmutableSetClientJobTimeFrameMessage.builder();
    if (json.data != null) {
      builder.data(json.data);
    }
    if (json.agentName != null) {
      builder.agentName(json.agentName);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link SetClientJobTimeFrameMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable SetClientJobTimeFrameMessage instance
   */
  public static ImmutableSetClientJobTimeFrameMessage copyOf(SetClientJobTimeFrameMessage instance) {
    if (instance instanceof ImmutableSetClientJobTimeFrameMessage) {
      return (ImmutableSetClientJobTimeFrameMessage) instance;
    }
    return ImmutableSetClientJobTimeFrameMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableSetClientJobTimeFrameMessage ImmutableSetClientJobTimeFrameMessage}.
   * <pre>
   * ImmutableSetClientJobTimeFrameMessage.builder()
   *    .data(org.greencloud.gui.messages.domain.JobTimeFrame) // required {@link SetClientJobTimeFrameMessage#getData() data}
   *    .agentName(String) // required {@link SetClientJobTimeFrameMessage#getAgentName() agentName}
   *    .build();
   * </pre>
   * @return A new ImmutableSetClientJobTimeFrameMessage builder
   */
  public static ImmutableSetClientJobTimeFrameMessage.Builder builder() {
    return new ImmutableSetClientJobTimeFrameMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableSetClientJobTimeFrameMessage ImmutableSetClientJobTimeFrameMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "SetClientJobTimeFrameMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DATA = 0x1L;
    private static final long INIT_BIT_AGENT_NAME = 0x2L;
    private long initBits = 0x3L;

    private @Nullable JobTimeFrame data;
    private @Nullable String agentName;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code SetClientJobTimeFrameMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(SetClientJobTimeFrameMessage instance) {
      Objects.requireNonNull(instance, "instance");
      data(instance.getData());
      agentName(instance.getAgentName());
      return this;
    }

    /**
     * Initializes the value for the {@link SetClientJobTimeFrameMessage#getData() data} attribute.
     * @param data The value for data 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(JobTimeFrame data) {
      this.data = Objects.requireNonNull(data, "data");
      initBits &= ~INIT_BIT_DATA;
      return this;
    }

    /**
     * Initializes the value for the {@link SetClientJobTimeFrameMessage#getAgentName() agentName} attribute.
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
     * Builds a new {@link ImmutableSetClientJobTimeFrameMessage ImmutableSetClientJobTimeFrameMessage}.
     * @return An immutable instance of SetClientJobTimeFrameMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableSetClientJobTimeFrameMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableSetClientJobTimeFrameMessage(data, agentName);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      if ((initBits & INIT_BIT_AGENT_NAME) != 0) attributes.add("agentName");
      return "Cannot build SetClientJobTimeFrameMessage, some of required attributes are not set " + attributes;
    }
  }
}
