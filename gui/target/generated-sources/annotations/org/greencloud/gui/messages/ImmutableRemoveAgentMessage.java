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
 * Immutable implementation of {@link RemoveAgentMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableRemoveAgentMessage.builder()}.
 */
@Generated(from = "RemoveAgentMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableRemoveAgentMessage implements RemoveAgentMessage {
  private final String agentName;

  private ImmutableRemoveAgentMessage(String agentName) {
    this.agentName = agentName;
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
   * Copy the current immutable object by setting a value for the {@link RemoveAgentMessage#getAgentName() agentName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableRemoveAgentMessage withAgentName(String value) {
    String newValue = Objects.requireNonNull(value, "agentName");
    if (this.agentName.equals(newValue)) return this;
    return new ImmutableRemoveAgentMessage(newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableRemoveAgentMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableRemoveAgentMessage
        && equalTo(0, (ImmutableRemoveAgentMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableRemoveAgentMessage another) {
    return agentName.equals(another.agentName);
  }

  /**
   * Computes a hash code from attributes: {@code agentName}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + agentName.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code RemoveAgentMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("RemoveAgentMessage")
        .omitNullValues()
        .add("agentName", agentName)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "RemoveAgentMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements RemoveAgentMessage {
    @Nullable String agentName;
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
      this.agentName = agentName;
    }
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
  static ImmutableRemoveAgentMessage fromJson(Json json) {
    ImmutableRemoveAgentMessage.Builder builder = ImmutableRemoveAgentMessage.builder();
    if (json.agentName != null) {
      builder.agentName(json.agentName);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link RemoveAgentMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable RemoveAgentMessage instance
   */
  public static ImmutableRemoveAgentMessage copyOf(RemoveAgentMessage instance) {
    if (instance instanceof ImmutableRemoveAgentMessage) {
      return (ImmutableRemoveAgentMessage) instance;
    }
    return ImmutableRemoveAgentMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableRemoveAgentMessage ImmutableRemoveAgentMessage}.
   * <pre>
   * ImmutableRemoveAgentMessage.builder()
   *    .agentName(String) // required {@link RemoveAgentMessage#getAgentName() agentName}
   *    .build();
   * </pre>
   * @return A new ImmutableRemoveAgentMessage builder
   */
  public static ImmutableRemoveAgentMessage.Builder builder() {
    return new ImmutableRemoveAgentMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableRemoveAgentMessage ImmutableRemoveAgentMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "RemoveAgentMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_AGENT_NAME = 0x1L;
    private long initBits = 0x1L;

    private @Nullable String agentName;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code RemoveAgentMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(RemoveAgentMessage instance) {
      Objects.requireNonNull(instance, "instance");
      agentName(instance.getAgentName());
      return this;
    }

    /**
     * Initializes the value for the {@link RemoveAgentMessage#getAgentName() agentName} attribute.
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
     * Builds a new {@link ImmutableRemoveAgentMessage ImmutableRemoveAgentMessage}.
     * @return An immutable instance of RemoveAgentMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableRemoveAgentMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableRemoveAgentMessage(agentName);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_AGENT_NAME) != 0) attributes.add("agentName");
      return "Cannot build RemoveAgentMessage, some of required attributes are not set " + attributes;
    }
  }
}
