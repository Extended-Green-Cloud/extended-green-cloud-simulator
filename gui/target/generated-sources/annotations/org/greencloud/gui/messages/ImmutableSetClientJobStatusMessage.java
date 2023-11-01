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
 * Immutable implementation of {@link SetClientJobStatusMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableSetClientJobStatusMessage.builder()}.
 */
@Generated(from = "SetClientJobStatusMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableSetClientJobStatusMessage
    implements SetClientJobStatusMessage {
  private final String status;
  private final String agentName;

  private ImmutableSetClientJobStatusMessage(String status, String agentName) {
    this.status = status;
    this.agentName = agentName;
  }

  /**
   * @return The value of the {@code status} attribute
   */
  @JsonProperty("status")
  @Override
  public String getStatus() {
    return status;
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
   * Copy the current immutable object by setting a value for the {@link SetClientJobStatusMessage#getStatus() status} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for status
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableSetClientJobStatusMessage withStatus(String value) {
    String newValue = Objects.requireNonNull(value, "status");
    if (this.status.equals(newValue)) return this;
    return new ImmutableSetClientJobStatusMessage(newValue, this.agentName);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link SetClientJobStatusMessage#getAgentName() agentName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableSetClientJobStatusMessage withAgentName(String value) {
    String newValue = Objects.requireNonNull(value, "agentName");
    if (this.agentName.equals(newValue)) return this;
    return new ImmutableSetClientJobStatusMessage(this.status, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableSetClientJobStatusMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableSetClientJobStatusMessage
        && equalTo(0, (ImmutableSetClientJobStatusMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableSetClientJobStatusMessage another) {
    return status.equals(another.status)
        && agentName.equals(another.agentName);
  }

  /**
   * Computes a hash code from attributes: {@code status}, {@code agentName}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + status.hashCode();
    h += (h << 5) + agentName.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code SetClientJobStatusMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("SetClientJobStatusMessage")
        .omitNullValues()
        .add("status", status)
        .add("agentName", agentName)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "SetClientJobStatusMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements SetClientJobStatusMessage {
    @Nullable String status;
    @Nullable String agentName;
    @JsonProperty("status")
    public void setStatus(String status) {
      this.status = status;
    }
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
      this.agentName = agentName;
    }
    @Override
    public String getStatus() { throw new UnsupportedOperationException(); }
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
  static ImmutableSetClientJobStatusMessage fromJson(Json json) {
    ImmutableSetClientJobStatusMessage.Builder builder = ImmutableSetClientJobStatusMessage.builder();
    if (json.status != null) {
      builder.status(json.status);
    }
    if (json.agentName != null) {
      builder.agentName(json.agentName);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link SetClientJobStatusMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable SetClientJobStatusMessage instance
   */
  public static ImmutableSetClientJobStatusMessage copyOf(SetClientJobStatusMessage instance) {
    if (instance instanceof ImmutableSetClientJobStatusMessage) {
      return (ImmutableSetClientJobStatusMessage) instance;
    }
    return ImmutableSetClientJobStatusMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableSetClientJobStatusMessage ImmutableSetClientJobStatusMessage}.
   * <pre>
   * ImmutableSetClientJobStatusMessage.builder()
   *    .status(String) // required {@link SetClientJobStatusMessage#getStatus() status}
   *    .agentName(String) // required {@link SetClientJobStatusMessage#getAgentName() agentName}
   *    .build();
   * </pre>
   * @return A new ImmutableSetClientJobStatusMessage builder
   */
  public static ImmutableSetClientJobStatusMessage.Builder builder() {
    return new ImmutableSetClientJobStatusMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableSetClientJobStatusMessage ImmutableSetClientJobStatusMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "SetClientJobStatusMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_STATUS = 0x1L;
    private static final long INIT_BIT_AGENT_NAME = 0x2L;
    private long initBits = 0x3L;

    private @Nullable String status;
    private @Nullable String agentName;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code SetClientJobStatusMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(SetClientJobStatusMessage instance) {
      Objects.requireNonNull(instance, "instance");
      status(instance.getStatus());
      agentName(instance.getAgentName());
      return this;
    }

    /**
     * Initializes the value for the {@link SetClientJobStatusMessage#getStatus() status} attribute.
     * @param status The value for status 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("status")
    public final Builder status(String status) {
      this.status = Objects.requireNonNull(status, "status");
      initBits &= ~INIT_BIT_STATUS;
      return this;
    }

    /**
     * Initializes the value for the {@link SetClientJobStatusMessage#getAgentName() agentName} attribute.
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
     * Builds a new {@link ImmutableSetClientJobStatusMessage ImmutableSetClientJobStatusMessage}.
     * @return An immutable instance of SetClientJobStatusMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableSetClientJobStatusMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableSetClientJobStatusMessage(status, agentName);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_STATUS) != 0) attributes.add("status");
      if ((initBits & INIT_BIT_AGENT_NAME) != 0) attributes.add("agentName");
      return "Cannot build SetClientJobStatusMessage, some of required attributes are not set " + attributes;
    }
  }
}
