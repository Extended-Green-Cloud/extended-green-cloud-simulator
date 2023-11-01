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
 * Immutable implementation of {@link UpdateServerMaintenanceMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableUpdateServerMaintenanceMessage.builder()}.
 */
@Generated(from = "UpdateServerMaintenanceMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableUpdateServerMaintenanceMessage
    implements UpdateServerMaintenanceMessage {
  private final String agentName;
  private final String state;
  private final Boolean result;
  private final @Nullable Boolean error;

  private ImmutableUpdateServerMaintenanceMessage(
      String agentName,
      String state,
      Boolean result,
      @Nullable Boolean error) {
    this.agentName = agentName;
    this.state = state;
    this.result = result;
    this.error = error;
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
   * @return The value of the {@code state} attribute
   */
  @JsonProperty("state")
  @Override
  public String getState() {
    return state;
  }

  /**
   * @return The value of the {@code result} attribute
   */
  @JsonProperty("result")
  @Override
  public Boolean getResult() {
    return result;
  }

  /**
   * @return The value of the {@code error} attribute
   */
  @JsonProperty("error")
  @Override
  public @Nullable Boolean getError() {
    return error;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateServerMaintenanceMessage#getAgentName() agentName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateServerMaintenanceMessage withAgentName(String value) {
    String newValue = Objects.requireNonNull(value, "agentName");
    if (this.agentName.equals(newValue)) return this;
    return new ImmutableUpdateServerMaintenanceMessage(newValue, this.state, this.result, this.error);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateServerMaintenanceMessage#getState() state} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for state
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateServerMaintenanceMessage withState(String value) {
    String newValue = Objects.requireNonNull(value, "state");
    if (this.state.equals(newValue)) return this;
    return new ImmutableUpdateServerMaintenanceMessage(this.agentName, newValue, this.result, this.error);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateServerMaintenanceMessage#getResult() result} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for result
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateServerMaintenanceMessage withResult(Boolean value) {
    Boolean newValue = Objects.requireNonNull(value, "result");
    if (this.result.equals(newValue)) return this;
    return new ImmutableUpdateServerMaintenanceMessage(this.agentName, this.state, newValue, this.error);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateServerMaintenanceMessage#getError() error} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for error (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateServerMaintenanceMessage withError(@Nullable Boolean value) {
    if (Objects.equals(this.error, value)) return this;
    return new ImmutableUpdateServerMaintenanceMessage(this.agentName, this.state, this.result, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableUpdateServerMaintenanceMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableUpdateServerMaintenanceMessage
        && equalTo(0, (ImmutableUpdateServerMaintenanceMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableUpdateServerMaintenanceMessage another) {
    return agentName.equals(another.agentName)
        && state.equals(another.state)
        && result.equals(another.result)
        && Objects.equals(error, another.error);
  }

  /**
   * Computes a hash code from attributes: {@code agentName}, {@code state}, {@code result}, {@code error}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + agentName.hashCode();
    h += (h << 5) + state.hashCode();
    h += (h << 5) + result.hashCode();
    h += (h << 5) + Objects.hashCode(error);
    return h;
  }

  /**
   * Prints the immutable value {@code UpdateServerMaintenanceMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("UpdateServerMaintenanceMessage")
        .omitNullValues()
        .add("agentName", agentName)
        .add("state", state)
        .add("result", result)
        .add("error", error)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "UpdateServerMaintenanceMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements UpdateServerMaintenanceMessage {
    @Nullable String agentName;
    @Nullable String state;
    @Nullable Boolean result;
    @Nullable Boolean error;
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
      this.agentName = agentName;
    }
    @JsonProperty("state")
    public void setState(String state) {
      this.state = state;
    }
    @JsonProperty("result")
    public void setResult(Boolean result) {
      this.result = result;
    }
    @JsonProperty("error")
    public void setError(@Nullable Boolean error) {
      this.error = error;
    }
    @Override
    public String getAgentName() { throw new UnsupportedOperationException(); }
    @Override
    public String getState() { throw new UnsupportedOperationException(); }
    @Override
    public Boolean getResult() { throw new UnsupportedOperationException(); }
    @Override
    public Boolean getError() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableUpdateServerMaintenanceMessage fromJson(Json json) {
    ImmutableUpdateServerMaintenanceMessage.Builder builder = ImmutableUpdateServerMaintenanceMessage.builder();
    if (json.agentName != null) {
      builder.agentName(json.agentName);
    }
    if (json.state != null) {
      builder.state(json.state);
    }
    if (json.result != null) {
      builder.result(json.result);
    }
    if (json.error != null) {
      builder.error(json.error);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link UpdateServerMaintenanceMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable UpdateServerMaintenanceMessage instance
   */
  public static ImmutableUpdateServerMaintenanceMessage copyOf(UpdateServerMaintenanceMessage instance) {
    if (instance instanceof ImmutableUpdateServerMaintenanceMessage) {
      return (ImmutableUpdateServerMaintenanceMessage) instance;
    }
    return ImmutableUpdateServerMaintenanceMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableUpdateServerMaintenanceMessage ImmutableUpdateServerMaintenanceMessage}.
   * <pre>
   * ImmutableUpdateServerMaintenanceMessage.builder()
   *    .agentName(String) // required {@link UpdateServerMaintenanceMessage#getAgentName() agentName}
   *    .state(String) // required {@link UpdateServerMaintenanceMessage#getState() state}
   *    .result(Boolean) // required {@link UpdateServerMaintenanceMessage#getResult() result}
   *    .error(Boolean | null) // nullable {@link UpdateServerMaintenanceMessage#getError() error}
   *    .build();
   * </pre>
   * @return A new ImmutableUpdateServerMaintenanceMessage builder
   */
  public static ImmutableUpdateServerMaintenanceMessage.Builder builder() {
    return new ImmutableUpdateServerMaintenanceMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableUpdateServerMaintenanceMessage ImmutableUpdateServerMaintenanceMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "UpdateServerMaintenanceMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_AGENT_NAME = 0x1L;
    private static final long INIT_BIT_STATE = 0x2L;
    private static final long INIT_BIT_RESULT = 0x4L;
    private long initBits = 0x7L;

    private @Nullable String agentName;
    private @Nullable String state;
    private @Nullable Boolean result;
    private @Nullable Boolean error;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code UpdateServerMaintenanceMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(UpdateServerMaintenanceMessage instance) {
      Objects.requireNonNull(instance, "instance");
      agentName(instance.getAgentName());
      state(instance.getState());
      result(instance.getResult());
      @Nullable Boolean errorValue = instance.getError();
      if (errorValue != null) {
        error(errorValue);
      }
      return this;
    }

    /**
     * Initializes the value for the {@link UpdateServerMaintenanceMessage#getAgentName() agentName} attribute.
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
     * Initializes the value for the {@link UpdateServerMaintenanceMessage#getState() state} attribute.
     * @param state The value for state 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("state")
    public final Builder state(String state) {
      this.state = Objects.requireNonNull(state, "state");
      initBits &= ~INIT_BIT_STATE;
      return this;
    }

    /**
     * Initializes the value for the {@link UpdateServerMaintenanceMessage#getResult() result} attribute.
     * @param result The value for result 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("result")
    public final Builder result(Boolean result) {
      this.result = Objects.requireNonNull(result, "result");
      initBits &= ~INIT_BIT_RESULT;
      return this;
    }

    /**
     * Initializes the value for the {@link UpdateServerMaintenanceMessage#getError() error} attribute.
     * @param error The value for error (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("error")
    public final Builder error(@Nullable Boolean error) {
      this.error = error;
      return this;
    }

    /**
     * Builds a new {@link ImmutableUpdateServerMaintenanceMessage ImmutableUpdateServerMaintenanceMessage}.
     * @return An immutable instance of UpdateServerMaintenanceMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableUpdateServerMaintenanceMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableUpdateServerMaintenanceMessage(agentName, state, result, error);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_AGENT_NAME) != 0) attributes.add("agentName");
      if ((initBits & INIT_BIT_STATE) != 0) attributes.add("state");
      if ((initBits & INIT_BIT_RESULT) != 0) attributes.add("result");
      return "Cannot build UpdateServerMaintenanceMessage, some of required attributes are not set " + attributes;
    }
  }
}
