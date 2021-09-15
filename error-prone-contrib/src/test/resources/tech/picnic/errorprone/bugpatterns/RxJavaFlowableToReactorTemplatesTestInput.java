package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

final class RxJavaFlowableToReactorTemplatesTest implements RefasterTemplateTestCase {

  Flowable<Integer> testFlowableCombineLatest() {
    return Flowable.combineLatest(Flowable.just(1), Flowable.just(2), Integer::sum);
  }

  Flowable<Integer> testFlowableConcatWithPublisher() {
    return Flowable.just(1).concatWith(Flowable.just(2));
  }

  Flowable<Integer> testFlowableDefer() {
    return Flowable.defer(() -> Flowable.just(1));
  }

  Flowable<Object> testFlowableEmpty() {
    return Flowable.empty();
  }

  Flowable<Object> testFlowableErrorThrowable() {
    return Flowable.error(new IllegalStateException());
  }

  Flowable<Object> testFlowableErrorCallable() {
    return Flowable.error(
        () -> {
          throw new IllegalStateException();
        });
  }

  Flowable<Integer> testFlowableFromArray() {
    return Flowable.fromArray(1, 2, 3);
  }

  Flowable<Integer> testFlowableFromCallable() {
    return Flowable.fromCallable(() -> 1);
  }

  Flowable<Integer> testFlowableFromIterable() {
    return Flowable.fromIterable(ImmutableList.of(1, 2, 3));
  }

  Flowable<Integer> testFlowableFromPublisher() {
    return Flowable.fromPublisher(Flowable.just(1));
  }

  Flowable<Integer> testFlowableFilter() {
    return Flowable.just(1).filter(i -> i > 2);
  }

  Maybe<Integer> testFlowableFirstElement() {
    return Flowable.just(1).firstElement();
  }

  Single<Integer> testFlowableFirstOrError() {
    return Flowable.just(1).firstOrError();
  }

  Completable testFlowableFlatMapCompletable() {
    return Flowable.just(1).flatMapCompletable(integer -> Completable.complete());
  }

  Flowable<Object> testFlowableFlatMap() {
    Flowable.just(1).flatMap(this::exampleMethod2);
    return Flowable.just(1).flatMap(i -> ImmutableSet::of);
  }

  private Maybe<Integer> exampleMethod(Integer x) {
    return null;
  }

  private Flowable<Integer> exampleMethod2(Integer x) {
    return null;
  }

  ImmutableList<Flowable<Integer>> testFlowableJust() {
    return ImmutableList.of(Flowable.just(1), Flowable.just(1, 2));
  }

  Flowable<Integer> testFlowableRange() {
    return Flowable.range(1, 10);
  }

  Flowable<?> testFlowableRangeLong() {
    return Flowable.rangeLong(1, 10);
  }

  Flowable<Integer> testFlowableZip() {
    return Flowable.zip(Flowable.just(1), Flowable.just(2), (i1, i2) -> i1 + i2);
  }

  Flowable<Integer> testFlowableBiFunctionRemoveUtil() {
    return RxJava2Adapter.fluxToFlowable(
        Flux.zip(
            Flowable.just(1),
            Flowable.just(2),
            RxJavaReactorMigrationUtil.toJdkBiFunction((i1, i2) -> i1 + i2)));
  }

  Single<Boolean> testFlowableAll() {
    return Flowable.just(true, true).all(Boolean::booleanValue);
  }

  Single<Boolean> testFlowableAny() {
    return Flowable.just(true, true).any(Boolean::booleanValue);
  }

  Object testFlowableBlockingFirst() {
    return Flowable.just(1).blockingFirst();
  }

  Flowable<Integer> testFlowableMap() {
    return Flowable.just(1).map(i -> i + 1);
  }

  Flowable<Integer> testFlowableMergeWith() {
    return Flowable.just(1).mergeWith(Single.just(1));
  }

  Single<Integer> testFlowableSingleDefault() {
    return Flowable.just(1).single(2);
  }

  Maybe<Integer> testFlowableSingleElement() {
    return Flowable.just(1).singleElement();
  }

  Single<Integer> testFlowableSingleOrError() {
    return Flowable.just(1).singleOrError();
  }

  Flowable<Integer> testFlowableSwitchIfEmptyPublisher() {
    return Flowable.just(1)
        .switchIfEmpty(
            Flowable.error(
                () -> {
                  throw new IllegalStateException();
                }));
  }

  Single<List<Integer>> testFlowableToList() {
    return Flowable.just(1, 2).toList();
  }

  Single<Map<Boolean, Integer>> testFlowableToMap() {
    return Flowable.just(1).toMap(i -> i > 1);
  }
}