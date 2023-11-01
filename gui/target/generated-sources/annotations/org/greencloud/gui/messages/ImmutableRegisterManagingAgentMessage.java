package org.greencloud.gui.messages;

import com.database.knowledge.domain.goal.AdaptationGoal;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link RegisterManagingAgentMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableRegisterManagingAgentMessage.builder()}.
 */
@Generated(from = "RegisterManagingAgentMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableRegisterManagingAgentMessage
    implements RegisterManagingAgentMessage {
  private final ImmutableList<AdaptationGoal> data;

  private ImmutableRegisterManagingAgentMessage(ImmutableList<AdaptationGoal> data) {
    this.data = data;
  }

  /**
   * @return The value of the {@code data} attribute
   */
  @JsonProperty("data")
  @Override
  public ImmutableList<AdaptationGoal> getData() {
    return data;
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link RegisterManagingAgentMessage#getData() data}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableRegisterManagingAgentMessage withData(AdaptationGoal... elements) {
    ImmutableList<AdaptationGoal> newValue = ImmutableList.copyOf(elements);
    return new ImmutableRegisterManagingAgentMessage(newValue);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link RegisterManagingAgentMessage#getData() data}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of data elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableRegisterManagingAgentMessage withData(Iterable<? extends AdaptationGoal> elements) {
    if (this.data == elements) return this;
    ImmutableList<AdaptationGoal> newValue = ImmutableList.copyOf(elements);
    return new ImmutableRegisterManagingAgentMessage(newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableRegisterManagingAgentMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableRegisterManagingAgentMessage
        && equalTo(0, (ImmutableRegisterManagingAgentMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableRegisterManagingAgentMessage another) {
    return data.equals(another.data);
  }

  /**
   * Computes a hash code from attributes: {@code data}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + data.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code RegisterManagingAgentMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("RegisterManagingAgentMessage")
        .omitNullValues()
        .add("data", data)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "RegisterManagingAgentMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements RegisterManagingAgentMessage {
    @Nullable List<AdaptationGoal> data = ImmutableList.of();
    @JsonProperty("data")
    public void setData(List<AdaptationGoal> data) {
      this.data = data;
    }
    @Override
    public List<AdaptationGoal> getData() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableRegisterManagingAgentMessage fromJson(Json json) {
    ImmutableRegisterManagingAgentMessage.Builder builder = ImmutableRegisterManagingAgentMessage.builder();
    if (json.data != null) {
      builder.addAllData(json.data);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link RegisterManagingAgentMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable RegisterManagingAgentMessage instance
   */
  public static ImmutableRegisterManagingAgentMessage copyOf(RegisterManagingAgentMessage instance) {
    if (instance instanceof ImmutableRegisterManagingAgentMessage) {
      return (ImmutableRegisterManagingAgentMessage) instance;
    }
    return ImmutableRegisterManagingAgentMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableRegisterManagingAgentMessage ImmutableRegisterManagingAgentMessage}.
   * <pre>
   * ImmutableRegisterManagingAgentMessage.builder()
   *    .addData|addAllData(com.database.knowledge.domain.goal.AdaptationGoal) // {@link RegisterManagingAgentMessage#getData() data} elements
   *    .build();
   * </pre>
   * @return A new ImmutableRegisterManagingAgentMessage builder
   */
  public static ImmutableRegisterManagingAgentMessage.Builder builder() {
    return new ImmutableRegisterManagingAgentMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableRegisterManagingAgentMessage ImmutableRegisterManagingAgentMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "RegisterManagingAgentMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private ImmutableList.Builder<AdaptationGoal> data = ImmutableList.builder();

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code RegisterManagingAgentMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(RegisterManagingAgentMessage instance) {
      Objects.requireNonNull(instance, "instance");
      addAllData(instance.getData());
      return this;
    }

    /**
     * Adds one element to {@link RegisterManagingAgentMessage#getData() data} list.
     * @param element A data element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addData(AdaptationGoal element) {
      this.data.add(element);
      return this;
    }

    /**
     * Adds elements to {@link RegisterManagingAgentMessage#getData() data} list.
     * @param elements An array of data elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addData(AdaptationGoal... elements) {
      this.data.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link RegisterManagingAgentMessage#getData() data} list.
     * @param elements An iterable of data elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(Iterable<? extends AdaptationGoal> elements) {
      this.data = ImmutableList.builder();
      return addAllData(elements);
    }

    /**
     * Adds elements to {@link RegisterManagingAgentMessage#getData() data} list.
     * @param elements An iterable of data elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllData(Iterable<? extends AdaptationGoal> elements) {
      this.data.addAll(elements);
      return this;
    }

    /**
     * Builds a new {@link ImmutableRegisterManagingAgentMessage ImmutableRegisterManagingAgentMessage}.
     * @return An immutable instance of RegisterManagingAgentMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableRegisterManagingAgentMessage build() {
      return new ImmutableRegisterManagingAgentMessage(data.build());
    }
  }
}
