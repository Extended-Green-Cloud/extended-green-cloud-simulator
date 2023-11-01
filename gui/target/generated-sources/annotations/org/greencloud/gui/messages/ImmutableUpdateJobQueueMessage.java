package org.greencloud.gui.messages;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.greencloud.commons.domain.job.instance.JobInstanceScheduler;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link UpdateJobQueueMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableUpdateJobQueueMessage.builder()}.
 */
@Generated(from = "UpdateJobQueueMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableUpdateJobQueueMessage implements UpdateJobQueueMessage {
  private final LinkedList<JobInstanceScheduler> data;

  private ImmutableUpdateJobQueueMessage(LinkedList<JobInstanceScheduler> data) {
    this.data = data;
  }

  /**
   * @return The value of the {@code data} attribute
   */
  @JsonProperty("data")
  @Override
  public LinkedList<JobInstanceScheduler> getData() {
    return data;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateJobQueueMessage#getData() data} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for data
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateJobQueueMessage withData(LinkedList<JobInstanceScheduler> value) {
    if (this.data == value) return this;
    LinkedList<JobInstanceScheduler> newValue = Objects.requireNonNull(value, "data");
    return new ImmutableUpdateJobQueueMessage(newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableUpdateJobQueueMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableUpdateJobQueueMessage
        && equalTo(0, (ImmutableUpdateJobQueueMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableUpdateJobQueueMessage another) {
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
   * Prints the immutable value {@code UpdateJobQueueMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("UpdateJobQueueMessage")
        .omitNullValues()
        .add("data", data)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "UpdateJobQueueMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements UpdateJobQueueMessage {
    @Nullable LinkedList<JobInstanceScheduler> data;
    @JsonProperty("data")
    public void setData(LinkedList<JobInstanceScheduler> data) {
      this.data = data;
    }
    @Override
    public LinkedList<JobInstanceScheduler> getData() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableUpdateJobQueueMessage fromJson(Json json) {
    ImmutableUpdateJobQueueMessage.Builder builder = ImmutableUpdateJobQueueMessage.builder();
    if (json.data != null) {
      builder.data(json.data);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link UpdateJobQueueMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable UpdateJobQueueMessage instance
   */
  public static ImmutableUpdateJobQueueMessage copyOf(UpdateJobQueueMessage instance) {
    if (instance instanceof ImmutableUpdateJobQueueMessage) {
      return (ImmutableUpdateJobQueueMessage) instance;
    }
    return ImmutableUpdateJobQueueMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableUpdateJobQueueMessage ImmutableUpdateJobQueueMessage}.
   * <pre>
   * ImmutableUpdateJobQueueMessage.builder()
   *    .data(LinkedList&amp;lt;org.greencloud.commons.domain.job.instance.JobInstanceScheduler&amp;gt;) // required {@link UpdateJobQueueMessage#getData() data}
   *    .build();
   * </pre>
   * @return A new ImmutableUpdateJobQueueMessage builder
   */
  public static ImmutableUpdateJobQueueMessage.Builder builder() {
    return new ImmutableUpdateJobQueueMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableUpdateJobQueueMessage ImmutableUpdateJobQueueMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "UpdateJobQueueMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DATA = 0x1L;
    private long initBits = 0x1L;

    private @Nullable LinkedList<JobInstanceScheduler> data;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code UpdateJobQueueMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(UpdateJobQueueMessage instance) {
      Objects.requireNonNull(instance, "instance");
      data(instance.getData());
      return this;
    }

    /**
     * Initializes the value for the {@link UpdateJobQueueMessage#getData() data} attribute.
     * @param data The value for data 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(LinkedList<JobInstanceScheduler> data) {
      this.data = Objects.requireNonNull(data, "data");
      initBits &= ~INIT_BIT_DATA;
      return this;
    }

    /**
     * Builds a new {@link ImmutableUpdateJobQueueMessage ImmutableUpdateJobQueueMessage}.
     * @return An immutable instance of UpdateJobQueueMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableUpdateJobQueueMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableUpdateJobQueueMessage(data);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      return "Cannot build UpdateJobQueueMessage, some of required attributes are not set " + attributes;
    }
  }
}
