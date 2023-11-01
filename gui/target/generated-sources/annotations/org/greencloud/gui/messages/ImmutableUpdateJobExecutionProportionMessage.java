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
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link UpdateJobExecutionProportionMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableUpdateJobExecutionProportionMessage.builder()}.
 */
@Generated(from = "UpdateJobExecutionProportionMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableUpdateJobExecutionProportionMessage
    implements UpdateJobExecutionProportionMessage {
  private final Double data;
  private final String agentName;

  private ImmutableUpdateJobExecutionProportionMessage(Double data, String agentName) {
    this.data = data;
    this.agentName = agentName;
  }

  /**
   * @return The value of the {@code data} attribute
   */
  @JsonProperty("data")
  @Override
  public Double getData() {
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
   * Copy the current immutable object by setting a value for the {@link UpdateJobExecutionProportionMessage#getData() data} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for data
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateJobExecutionProportionMessage withData(Double value) {
    Double newValue = Objects.requireNonNull(value, "data");
    if (this.data.equals(newValue)) return this;
    return new ImmutableUpdateJobExecutionProportionMessage(newValue, this.agentName);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateJobExecutionProportionMessage#getAgentName() agentName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateJobExecutionProportionMessage withAgentName(String value) {
    String newValue = Objects.requireNonNull(value, "agentName");
    if (this.agentName.equals(newValue)) return this;
    return new ImmutableUpdateJobExecutionProportionMessage(this.data, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableUpdateJobExecutionProportionMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableUpdateJobExecutionProportionMessage
        && equalTo(0, (ImmutableUpdateJobExecutionProportionMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableUpdateJobExecutionProportionMessage another) {
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
   * Prints the immutable value {@code UpdateJobExecutionProportionMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("UpdateJobExecutionProportionMessage")
        .omitNullValues()
        .add("data", data)
        .add("agentName", agentName)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "UpdateJobExecutionProportionMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements UpdateJobExecutionProportionMessage {
    @Nullable Double data;
    @Nullable String agentName;
    @JsonProperty("data")
    public void setData(Double data) {
      this.data = data;
    }
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
      this.agentName = agentName;
    }
    @Override
    public Double getData() { throw new UnsupportedOperationException(); }
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
  static ImmutableUpdateJobExecutionProportionMessage fromJson(Json json) {
    ImmutableUpdateJobExecutionProportionMessage.Builder builder = ImmutableUpdateJobExecutionProportionMessage.builder();
    if (json.data != null) {
      builder.data(json.data);
    }
    if (json.agentName != null) {
      builder.agentName(json.agentName);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link UpdateJobExecutionProportionMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable UpdateJobExecutionProportionMessage instance
   */
  public static ImmutableUpdateJobExecutionProportionMessage copyOf(UpdateJobExecutionProportionMessage instance) {
    if (instance instanceof ImmutableUpdateJobExecutionProportionMessage) {
      return (ImmutableUpdateJobExecutionProportionMessage) instance;
    }
    return ImmutableUpdateJobExecutionProportionMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableUpdateJobExecutionProportionMessage ImmutableUpdateJobExecutionProportionMessage}.
   * <pre>
   * ImmutableUpdateJobExecutionProportionMessage.builder()
   *    .data(Double) // required {@link UpdateJobExecutionProportionMessage#getData() data}
   *    .agentName(String) // required {@link UpdateJobExecutionProportionMessage#getAgentName() agentName}
   *    .build();
   * </pre>
   * @return A new ImmutableUpdateJobExecutionProportionMessage builder
   */
  public static ImmutableUpdateJobExecutionProportionMessage.Builder builder() {
    return new ImmutableUpdateJobExecutionProportionMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableUpdateJobExecutionProportionMessage ImmutableUpdateJobExecutionProportionMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "UpdateJobExecutionProportionMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DATA = 0x1L;
    private static final long INIT_BIT_AGENT_NAME = 0x2L;
    private long initBits = 0x3L;

    private @Nullable Double data;
    private @Nullable String agentName;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code UpdateJobExecutionProportionMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(UpdateJobExecutionProportionMessage instance) {
      Objects.requireNonNull(instance, "instance");
      data(instance.getData());
      agentName(instance.getAgentName());
      return this;
    }

    /**
     * Initializes the value for the {@link UpdateJobExecutionProportionMessage#getData() data} attribute.
     * @param data The value for data 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(Double data) {
      this.data = Objects.requireNonNull(data, "data");
      initBits &= ~INIT_BIT_DATA;
      return this;
    }

    /**
     * Initializes the value for the {@link UpdateJobExecutionProportionMessage#getAgentName() agentName} attribute.
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
     * Builds a new {@link ImmutableUpdateJobExecutionProportionMessage ImmutableUpdateJobExecutionProportionMessage}.
     * @return An immutable instance of UpdateJobExecutionProportionMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableUpdateJobExecutionProportionMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableUpdateJobExecutionProportionMessage(data, agentName);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      if ((initBits & INIT_BIT_AGENT_NAME) != 0) attributes.add("agentName");
      return "Cannot build UpdateJobExecutionProportionMessage, some of required attributes are not set " + attributes;
    }
  }
}
