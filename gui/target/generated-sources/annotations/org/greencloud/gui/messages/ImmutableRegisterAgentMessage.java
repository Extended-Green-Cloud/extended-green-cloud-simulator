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
import org.greencloud.commons.args.agent.AgentArgs;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link RegisterAgentMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableRegisterAgentMessage.builder()}.
 */
@Generated(from = "RegisterAgentMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableRegisterAgentMessage implements RegisterAgentMessage {
  private final String agentType;
  private final AgentArgs data;

  private ImmutableRegisterAgentMessage(String agentType, AgentArgs data) {
    this.agentType = agentType;
    this.data = data;
  }

  /**
   * @return The value of the {@code agentType} attribute
   */
  @JsonProperty("agentType")
  @Override
  public String getAgentType() {
    return agentType;
  }

  /**
   * @return The value of the {@code data} attribute
   */
  @JsonProperty("data")
  @Override
  public AgentArgs getData() {
    return data;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link RegisterAgentMessage#getAgentType() agentType} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentType
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableRegisterAgentMessage withAgentType(String value) {
    String newValue = Objects.requireNonNull(value, "agentType");
    if (this.agentType.equals(newValue)) return this;
    return new ImmutableRegisterAgentMessage(newValue, this.data);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link RegisterAgentMessage#getData() data} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for data
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableRegisterAgentMessage withData(AgentArgs value) {
    if (this.data == value) return this;
    AgentArgs newValue = Objects.requireNonNull(value, "data");
    return new ImmutableRegisterAgentMessage(this.agentType, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableRegisterAgentMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableRegisterAgentMessage
        && equalTo(0, (ImmutableRegisterAgentMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableRegisterAgentMessage another) {
    return agentType.equals(another.agentType)
        && data.equals(another.data);
  }

  /**
   * Computes a hash code from attributes: {@code agentType}, {@code data}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + agentType.hashCode();
    h += (h << 5) + data.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code RegisterAgentMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("RegisterAgentMessage")
        .omitNullValues()
        .add("agentType", agentType)
        .add("data", data)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "RegisterAgentMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements RegisterAgentMessage {
    @Nullable String agentType;
    @Nullable AgentArgs data;
    @JsonProperty("agentType")
    public void setAgentType(String agentType) {
      this.agentType = agentType;
    }
    @JsonProperty("data")
    public void setData(AgentArgs data) {
      this.data = data;
    }
    @Override
    public String getAgentType() { throw new UnsupportedOperationException(); }
    @Override
    public AgentArgs getData() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableRegisterAgentMessage fromJson(Json json) {
    ImmutableRegisterAgentMessage.Builder builder = ImmutableRegisterAgentMessage.builder();
    if (json.agentType != null) {
      builder.agentType(json.agentType);
    }
    if (json.data != null) {
      builder.data(json.data);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link RegisterAgentMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable RegisterAgentMessage instance
   */
  public static ImmutableRegisterAgentMessage copyOf(RegisterAgentMessage instance) {
    if (instance instanceof ImmutableRegisterAgentMessage) {
      return (ImmutableRegisterAgentMessage) instance;
    }
    return ImmutableRegisterAgentMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableRegisterAgentMessage ImmutableRegisterAgentMessage}.
   * <pre>
   * ImmutableRegisterAgentMessage.builder()
   *    .agentType(String) // required {@link RegisterAgentMessage#getAgentType() agentType}
   *    .data(org.greencloud.commons.args.agent.AgentArgs) // required {@link RegisterAgentMessage#getData() data}
   *    .build();
   * </pre>
   * @return A new ImmutableRegisterAgentMessage builder
   */
  public static ImmutableRegisterAgentMessage.Builder builder() {
    return new ImmutableRegisterAgentMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableRegisterAgentMessage ImmutableRegisterAgentMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "RegisterAgentMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_AGENT_TYPE = 0x1L;
    private static final long INIT_BIT_DATA = 0x2L;
    private long initBits = 0x3L;

    private @Nullable String agentType;
    private @Nullable AgentArgs data;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code RegisterAgentMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(RegisterAgentMessage instance) {
      Objects.requireNonNull(instance, "instance");
      agentType(instance.getAgentType());
      data(instance.getData());
      return this;
    }

    /**
     * Initializes the value for the {@link RegisterAgentMessage#getAgentType() agentType} attribute.
     * @param agentType The value for agentType 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("agentType")
    public final Builder agentType(String agentType) {
      this.agentType = Objects.requireNonNull(agentType, "agentType");
      initBits &= ~INIT_BIT_AGENT_TYPE;
      return this;
    }

    /**
     * Initializes the value for the {@link RegisterAgentMessage#getData() data} attribute.
     * @param data The value for data 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(AgentArgs data) {
      this.data = Objects.requireNonNull(data, "data");
      initBits &= ~INIT_BIT_DATA;
      return this;
    }

    /**
     * Builds a new {@link ImmutableRegisterAgentMessage ImmutableRegisterAgentMessage}.
     * @return An immutable instance of RegisterAgentMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableRegisterAgentMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableRegisterAgentMessage(agentType, data);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_AGENT_TYPE) != 0) attributes.add("agentType");
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      return "Cannot build RegisterAgentMessage, some of required attributes are not set " + attributes;
    }
  }
}
