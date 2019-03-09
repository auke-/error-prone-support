package tech.picnic.errorprone.refastertemplates;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Queue;
import java.util.SortedSet;
import java.util.stream.Stream;

/** Refaster templates related to expressions dealing with (arbitrary) collections. */
final class CollectionTemplates {
  private CollectionTemplates() {}

  /** Prefer {@link Collection#isEmpty()} over alternatives that consult the collection's size. */
  static final class CollectionIsEmpty<T> {
    @BeforeTemplate
    boolean before(Collection<T> collection) {
      return Refaster.anyOf(collection.size() == 0, collection.size() <= 0, collection.size() < 1);
    }

    @AfterTemplate
    @AlsoNegation
    boolean after(Collection<T> collection) {
      return collection.isEmpty();
    }
  }

  /**
   * Don't call {@link Iterables#addAll(Collection, Iterable)} when the elements to be added are
   * already part of a {@link Collection}.
   */
  static final class CollectionAddAllFromCollection<T, S extends T> {
    @BeforeTemplate
    boolean before(Collection<T> addTo, Collection<S> elementsToAdd) {
      return Iterables.addAll(addTo, elementsToAdd);
    }

    @AfterTemplate
    boolean after(Collection<T> addTo, Collection<S> elementsToAdd) {
      return addTo.addAll(elementsToAdd);
    }
  }

  /** Prefer {@link ArrayList#ArrayList(Collection)} over the Guava alternative. */
  static final class NewArrayListFromCollection<T> {
    @BeforeTemplate
    ArrayList<T> before(Collection<T> collection) {
      return Lists.newArrayList(collection);
    }

    @AfterTemplate
    ArrayList<T> after(Collection<T> collection) {
      return new ArrayList<>(collection);
    }
  }

  /** Prefer {@link ImmutableCollection#asList()} over the more verbose alternative. */
  static final class ImmutableCollectionAsList<T> {
    @BeforeTemplate
    ImmutableList<T> before(ImmutableCollection<T> collection) {
      return ImmutableList.copyOf(collection);
    }

    @AfterTemplate
    ImmutableList<T> after(ImmutableCollection<T> collection) {
      return collection.asList();
    }
  }

  /**
   * Don't call {@link ImmutableCollection#asList()} if the result is going to be streamed; stream
   * directly.
   */
  static final class ImmutableCollectionAsListToStream<T> {
    @BeforeTemplate
    Stream<T> before(ImmutableCollection<T> collection) {
      return collection.asList().stream();
    }

    @AfterTemplate
    Stream<T> after(ImmutableCollection<T> collection) {
      return collection.stream();
    }
  }

  /**
   * Don't use the ternary operator to extract the first element of a possibly-empty {@link
   * Collection} as an {@link Optional}.
   */
  static final class OptionalFirstCollectionElement<T> {
    @BeforeTemplate
    Optional<T> before(Collection<T> collection) {
      return Refaster.anyOf(
          collection.stream().findAny(),
          collection.isEmpty() ? Optional.empty() : Optional.of(collection.iterator().next()));
    }

    @BeforeTemplate
    Optional<T> before(List<T> collection) {
      return collection.isEmpty() ? Optional.empty() : Optional.of(collection.get(0));
    }

    @BeforeTemplate
    Optional<T> before(SortedSet<T> collection) {
      return collection.isEmpty() ? Optional.empty() : Optional.of(collection.first());
    }

    @AfterTemplate
    Optional<T> after(Collection<T> collection) {
      return collection.stream().findFirst();
    }
  }

  static final class OptionalFirstQueueElement<T> {
    @BeforeTemplate
    Optional<T> before(Queue<T> queue) {
      return Refaster.anyOf(
          queue.stream().findFirst(),
          queue.isEmpty()
              ? Optional.empty()
              : Refaster.anyOf(Optional.of(queue.peek()), Optional.ofNullable(queue.peek())));
    }

    @AfterTemplate
    Optional<T> after(Queue<T> queue) {
      return Optional.ofNullable(queue.peek());
    }
  }

  static final class RemoveOptionalFirstNavigableSetElement<T> {
    @BeforeTemplate
    Optional<T> before(NavigableSet<T> set) {
      return set.isEmpty()
          ? Optional.empty()
          : Refaster.anyOf(Optional.of(set.pollFirst()), Optional.ofNullable(set.pollFirst()));
    }

    @AfterTemplate
    Optional<T> after(NavigableSet<T> set) {
      return Optional.ofNullable(set.pollFirst());
    }
  }

  static final class RemoveOptionalFirstQueueElement<T> {
    @BeforeTemplate
    Optional<T> before(Queue<T> queue) {
      return queue.isEmpty()
          ? Optional.empty()
          : Refaster.anyOf(
              Optional.of(Refaster.anyOf(queue.poll(), queue.remove())),
              Optional.ofNullable(Refaster.anyOf(queue.poll(), queue.remove())));
    }

    @AfterTemplate
    Optional<T> after(Queue<T> queue) {
      return Optional.ofNullable(queue.poll());
    }
  }
}
