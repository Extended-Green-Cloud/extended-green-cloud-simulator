package org.greencloud.gui.messages.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
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
import org.greencloud.commons.domain.jobstep.JobStep;
import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link JobCreator}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableJobCreator.builder()}.
 */
@Generated(from = "JobCreator", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableJobCreator implements JobCreator {
  private final Instant occurrenceTime;
  private final @Nullable Boolean isFinished;
  private final Long deadline;
  private final Long duration;
  private final ImmutableMap<String, Resource> resources;
  private final String processorName;
  private final @Nullable String selectionPreference;
  private final ImmutableList<JobStep> steps;

  private ImmutableJobCreator(
      Instant occurrenceTime,
      @Nullable Boolean isFinished,
      Long deadline,
      Long duration,
      ImmutableMap<String, Resource> resources,
      String processorName,
      @Nullable String selectionPreference,
      ImmutableList<JobStep> steps) {
    this.occurrenceTime = occurrenceTime;
    this.isFinished = isFinished;
    this.deadline = deadline;
    this.duration = duration;
    this.resources = resources;
    this.processorName = processorName;
    this.selectionPreference = selectionPreference;
    this.steps = steps;
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
   * @return The value of the {@code deadline} attribute
   */
  @JsonProperty("deadline")
  @Override
  public Long getDeadline() {
    return deadline;
  }

  /**
   * @return The value of the {@code duration} attribute
   */
  @JsonProperty("duration")
  @Override
  public Long getDuration() {
    return duration;
  }

  /**
   * @return The value of the {@code resources} attribute
   */
  @JsonProperty("resources")
  @Override
  public ImmutableMap<String, Resource> getResources() {
    return resources;
  }

  /**
   * @return The value of the {@code processorName} attribute
   */
  @JsonProperty("processorName")
  @Override
  public String getProcessorName() {
    return processorName;
  }

  /**
   * @return The value of the {@code selectionPreference} attribute
   */
  @JsonProperty("selectionPreference")
  @Override
  public @Nullable String getSelectionPreference() {
    return selectionPreference;
  }

  /**
   * @return The value of the {@code steps} attribute
   */
  @JsonProperty("steps")
  @Override
  public ImmutableList<JobStep> getSteps() {
    return steps;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link JobCreator#getOccurrenceTime() occurrenceTime} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for occurrenceTime
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableJobCreator withOccurrenceTime(Instant value) {
    if (this.occurrenceTime == value) return this;
    Instant newValue = Objects.requireNonNull(value, "occurrenceTime");
    return new ImmutableJobCreator(
        newValue,
        this.isFinished,
        this.deadline,
        this.duration,
        this.resources,
        this.processorName,
        this.selectionPreference,
        this.steps);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link JobCreator#isFinished() isFinished} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for isFinished (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableJobCreator withIsFinished(@Nullable Boolean value) {
    if (Objects.equals(this.isFinished, value)) return this;
    return new ImmutableJobCreator(
        this.occurrenceTime,
        value,
        this.deadline,
        this.duration,
        this.resources,
        this.processorName,
        this.selectionPreference,
        this.steps);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link JobCreator#getDeadline() deadline} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for deadline
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableJobCreator withDeadline(Long value) {
    Long newValue = Objects.requireNonNull(value, "deadline");
    if (this.deadline.equals(newValue)) return this;
    return new ImmutableJobCreator(
        this.occurrenceTime,
        this.isFinished,
        newValue,
        this.duration,
        this.resources,
        this.processorName,
        this.selectionPreference,
        this.steps);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link JobCreator#getDuration() duration} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for duration
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableJobCreator withDuration(Long value) {
    Long newValue = Objects.requireNonNull(value, "duration");
    if (this.duration.equals(newValue)) return this;
    return new ImmutableJobCreator(
        this.occurrenceTime,
        this.isFinished,
        this.deadline,
        newValue,
        this.resources,
        this.processorName,
        this.selectionPreference,
        this.steps);
  }

  /**
   * Copy the current immutable object by replacing the {@link JobCreator#getResources() resources} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the resources map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableJobCreator withResources(Map<String, ? extends Resource> entries) {
    if (this.resources == entries) return this;
    ImmutableMap<String, Resource> newValue = ImmutableMap.copyOf(entries);
    return new ImmutableJobCreator(
        this.occurrenceTime,
        this.isFinished,
        this.deadline,
        this.duration,
        newValue,
        this.processorName,
        this.selectionPreference,
        this.steps);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link JobCreator#getProcessorName() processorName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for processorName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableJobCreator withProcessorName(String value) {
    String newValue = Objects.requireNonNull(value, "processorName");
    if (this.processorName.equals(newValue)) return this;
    return new ImmutableJobCreator(
        this.occurrenceTime,
        this.isFinished,
        this.deadline,
        this.duration,
        this.resources,
        newValue,
        this.selectionPreference,
        this.steps);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link JobCreator#getSelectionPreference() selectionPreference} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for selectionPreference (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableJobCreator withSelectionPreference(@Nullable String value) {
    if (Objects.equals(this.selectionPreference, value)) return this;
    return new ImmutableJobCreator(
        this.occurrenceTime,
        this.isFinished,
        this.deadline,
        this.duration,
        this.resources,
        this.processorName,
        value,
        this.steps);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link JobCreator#getSteps() steps}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableJobCreator withSteps(JobStep... elements) {
    ImmutableList<JobStep> newValue = ImmutableList.copyOf(elements);
    return new ImmutableJobCreator(
        this.occurrenceTime,
        this.isFinished,
        this.deadline,
        this.duration,
        this.resources,
        this.processorName,
        this.selectionPreference,
        newValue);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link JobCreator#getSteps() steps}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of steps elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableJobCreator withSteps(Iterable<? extends JobStep> elements) {
    if (this.steps == elements) return this;
    ImmutableList<JobStep> newValue = ImmutableList.copyOf(elements);
    return new ImmutableJobCreator(
        this.occurrenceTime,
        this.isFinished,
        this.deadline,
        this.duration,
        this.resources,
        this.processorName,
        this.selectionPreference,
        newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableJobCreator} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableJobCreator
        && equalTo(0, (ImmutableJobCreator) another);
  }

  private boolean equalTo(int synthetic, ImmutableJobCreator another) {
    return occurrenceTime.equals(another.occurrenceTime)
        && Objects.equals(isFinished, another.isFinished)
        && deadline.equals(another.deadline)
        && duration.equals(another.duration)
        && resources.equals(another.resources)
        && processorName.equals(another.processorName)
        && Objects.equals(selectionPreference, another.selectionPreference)
        && steps.equals(another.steps);
  }

  /**
   * Computes a hash code from attributes: {@code occurrenceTime}, {@code isFinished}, {@code deadline}, {@code duration}, {@code resources}, {@code processorName}, {@code selectionPreference}, {@code steps}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + occurrenceTime.hashCode();
    h += (h << 5) + Objects.hashCode(isFinished);
    h += (h << 5) + deadline.hashCode();
    h += (h << 5) + duration.hashCode();
    h += (h << 5) + resources.hashCode();
    h += (h << 5) + processorName.hashCode();
    h += (h << 5) + Objects.hashCode(selectionPreference);
    h += (h << 5) + steps.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code JobCreator} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("JobCreator")
        .omitNullValues()
        .add("occurrenceTime", occurrenceTime)
        .add("isFinished", isFinished)
        .add("deadline", deadline)
        .add("duration", duration)
        .add("resources", resources)
        .add("processorName", processorName)
        .add("selectionPreference", selectionPreference)
        .add("steps", steps)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "JobCreator", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements JobCreator {
    @Nullable Instant occurrenceTime;
    @Nullable Boolean isFinished;
    @Nullable Long deadline;
    @Nullable Long duration;
    @Nullable Map<String, Resource> resources = ImmutableMap.of();
    @Nullable String processorName;
    @Nullable String selectionPreference;
    @Nullable List<JobStep> steps = ImmutableList.of();
    @JsonProperty("occurrenceTime")
    public void setOccurrenceTime(Instant occurrenceTime) {
      this.occurrenceTime = occurrenceTime;
    }
    @JsonProperty("isFinished")
    public void setIsFinished(@Nullable Boolean isFinished) {
      this.isFinished = isFinished;
    }
    @JsonProperty("deadline")
    public void setDeadline(Long deadline) {
      this.deadline = deadline;
    }
    @JsonProperty("duration")
    public void setDuration(Long duration) {
      this.duration = duration;
    }
    @JsonProperty("resources")
    public void setResources(Map<String, Resource> resources) {
      this.resources = resources;
    }
    @JsonProperty("processorName")
    public void setProcessorName(String processorName) {
      this.processorName = processorName;
    }
    @JsonProperty("selectionPreference")
    public void setSelectionPreference(@Nullable String selectionPreference) {
      this.selectionPreference = selectionPreference;
    }
    @JsonProperty("steps")
    public void setSteps(List<JobStep> steps) {
      this.steps = steps;
    }
    @Override
    public Instant getOccurrenceTime() { throw new UnsupportedOperationException(); }
    @Override
    public Boolean isFinished() { throw new UnsupportedOperationException(); }
    @Override
    public Long getDeadline() { throw new UnsupportedOperationException(); }
    @Override
    public Long getDuration() { throw new UnsupportedOperationException(); }
    @Override
    public Map<String, Resource> getResources() { throw new UnsupportedOperationException(); }
    @Override
    public String getProcessorName() { throw new UnsupportedOperationException(); }
    @Override
    public String getSelectionPreference() { throw new UnsupportedOperationException(); }
    @Override
    public List<JobStep> getSteps() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableJobCreator fromJson(Json json) {
    ImmutableJobCreator.Builder builder = ImmutableJobCreator.builder();
    if (json.occurrenceTime != null) {
      builder.occurrenceTime(json.occurrenceTime);
    }
    if (json.isFinished != null) {
      builder.isFinished(json.isFinished);
    }
    if (json.deadline != null) {
      builder.deadline(json.deadline);
    }
    if (json.duration != null) {
      builder.duration(json.duration);
    }
    if (json.resources != null) {
      builder.putAllResources(json.resources);
    }
    if (json.processorName != null) {
      builder.processorName(json.processorName);
    }
    if (json.selectionPreference != null) {
      builder.selectionPreference(json.selectionPreference);
    }
    if (json.steps != null) {
      builder.addAllSteps(json.steps);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link JobCreator} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable JobCreator instance
   */
  public static ImmutableJobCreator copyOf(JobCreator instance) {
    if (instance instanceof ImmutableJobCreator) {
      return (ImmutableJobCreator) instance;
    }
    return ImmutableJobCreator.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableJobCreator ImmutableJobCreator}.
   * <pre>
   * ImmutableJobCreator.builder()
   *    .occurrenceTime(java.time.Instant) // required {@link JobCreator#getOccurrenceTime() occurrenceTime}
   *    .isFinished(Boolean | null) // nullable {@link JobCreator#isFinished() isFinished}
   *    .deadline(Long) // required {@link JobCreator#getDeadline() deadline}
   *    .duration(Long) // required {@link JobCreator#getDuration() duration}
   *    .putResources|putAllResources(String =&gt; org.greencloud.commons.domain.resources.Resource) // {@link JobCreator#getResources() resources} mappings
   *    .processorName(String) // required {@link JobCreator#getProcessorName() processorName}
   *    .selectionPreference(String | null) // nullable {@link JobCreator#getSelectionPreference() selectionPreference}
   *    .addSteps|addAllSteps(org.greencloud.commons.domain.jobstep.JobStep) // {@link JobCreator#getSteps() steps} elements
   *    .build();
   * </pre>
   * @return A new ImmutableJobCreator builder
   */
  public static ImmutableJobCreator.Builder builder() {
    return new ImmutableJobCreator.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableJobCreator ImmutableJobCreator}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "JobCreator", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_OCCURRENCE_TIME = 0x1L;
    private static final long INIT_BIT_DEADLINE = 0x2L;
    private static final long INIT_BIT_DURATION = 0x4L;
    private static final long INIT_BIT_PROCESSOR_NAME = 0x8L;
    private long initBits = 0xfL;

    private @Nullable Instant occurrenceTime;
    private @Nullable Boolean isFinished;
    private @Nullable Long deadline;
    private @Nullable Long duration;
    private ImmutableMap.Builder<String, Resource> resources = ImmutableMap.builder();
    private @Nullable String processorName;
    private @Nullable String selectionPreference;
    private ImmutableList.Builder<JobStep> steps = ImmutableList.builder();

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.greencloud.gui.messages.domain.JobCreator} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(JobCreator instance) {
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
      if (object instanceof JobCreator) {
        JobCreator instance = (JobCreator) object;
        duration(instance.getDuration());
        @Nullable String selectionPreferenceValue = instance.getSelectionPreference();
        if (selectionPreferenceValue != null) {
          selectionPreference(selectionPreferenceValue);
        }
        if ((bits & 0x1L) == 0) {
          occurrenceTime(instance.getOccurrenceTime());
          bits |= 0x1L;
        }
        putAllResources(instance.getResources());
        processorName(instance.getProcessorName());
        if ((bits & 0x2L) == 0) {
          @Nullable Boolean isFinishedValue = instance.isFinished();
          if (isFinishedValue != null) {
            isFinished(isFinishedValue);
          }
          bits |= 0x2L;
        }
        deadline(instance.getDeadline());
        addAllSteps(instance.getSteps());
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
     * Initializes the value for the {@link JobCreator#getOccurrenceTime() occurrenceTime} attribute.
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
     * Initializes the value for the {@link JobCreator#isFinished() isFinished} attribute.
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
     * Initializes the value for the {@link JobCreator#getDeadline() deadline} attribute.
     * @param deadline The value for deadline 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("deadline")
    public final Builder deadline(Long deadline) {
      this.deadline = Objects.requireNonNull(deadline, "deadline");
      initBits &= ~INIT_BIT_DEADLINE;
      return this;
    }

    /**
     * Initializes the value for the {@link JobCreator#getDuration() duration} attribute.
     * @param duration The value for duration 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("duration")
    public final Builder duration(Long duration) {
      this.duration = Objects.requireNonNull(duration, "duration");
      initBits &= ~INIT_BIT_DURATION;
      return this;
    }

    /**
     * Put one entry to the {@link JobCreator#getResources() resources} map.
     * @param key The key in the resources map
     * @param value The associated value in the resources map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putResources(String key, Resource value) {
      this.resources.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link JobCreator#getResources() resources} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putResources(Map.Entry<String, ? extends Resource> entry) {
      this.resources.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link JobCreator#getResources() resources} map. Nulls are not permitted
     * @param entries The entries that will be added to the resources map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("resources")
    public final Builder resources(Map<String, ? extends Resource> entries) {
      this.resources = ImmutableMap.builder();
      return putAllResources(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link JobCreator#getResources() resources} map. Nulls are not permitted
     * @param entries The entries that will be added to the resources map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putAllResources(Map<String, ? extends Resource> entries) {
      this.resources.putAll(entries);
      return this;
    }

    /**
     * Initializes the value for the {@link JobCreator#getProcessorName() processorName} attribute.
     * @param processorName The value for processorName 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("processorName")
    public final Builder processorName(String processorName) {
      this.processorName = Objects.requireNonNull(processorName, "processorName");
      initBits &= ~INIT_BIT_PROCESSOR_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link JobCreator#getSelectionPreference() selectionPreference} attribute.
     * @param selectionPreference The value for selectionPreference (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("selectionPreference")
    public final Builder selectionPreference(@Nullable String selectionPreference) {
      this.selectionPreference = selectionPreference;
      return this;
    }

    /**
     * Adds one element to {@link JobCreator#getSteps() steps} list.
     * @param element A steps element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addSteps(JobStep element) {
      this.steps.add(element);
      return this;
    }

    /**
     * Adds elements to {@link JobCreator#getSteps() steps} list.
     * @param elements An array of steps elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addSteps(JobStep... elements) {
      this.steps.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link JobCreator#getSteps() steps} list.
     * @param elements An iterable of steps elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("steps")
    public final Builder steps(Iterable<? extends JobStep> elements) {
      this.steps = ImmutableList.builder();
      return addAllSteps(elements);
    }

    /**
     * Adds elements to {@link JobCreator#getSteps() steps} list.
     * @param elements An iterable of steps elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllSteps(Iterable<? extends JobStep> elements) {
      this.steps.addAll(elements);
      return this;
    }

    /**
     * Builds a new {@link ImmutableJobCreator ImmutableJobCreator}.
     * @return An immutable instance of JobCreator
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableJobCreator build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableJobCreator(
          occurrenceTime,
          isFinished,
          deadline,
          duration,
          resources.build(),
          processorName,
          selectionPreference,
          steps.build());
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_OCCURRENCE_TIME) != 0) attributes.add("occurrenceTime");
      if ((initBits & INIT_BIT_DEADLINE) != 0) attributes.add("deadline");
      if ((initBits & INIT_BIT_DURATION) != 0) attributes.add("duration");
      if ((initBits & INIT_BIT_PROCESSOR_NAME) != 0) attributes.add("processorName");
      return "Cannot build JobCreator, some of required attributes are not set " + attributes;
    }
  }
}
