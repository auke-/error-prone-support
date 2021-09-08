package tech.picnic.errorprone.bugpatterns;

import com.google.common.collect.ImmutableList;
import io.reactivex.Completable;
import io.reactivex.Observable;

final class RxJavaObservableToReactorTemplatesTest implements RefasterTemplateTestCase {

  Observable<Integer> testObservableAmb() {
    return Observable.amb(ImmutableList.of(Observable.just(1), Observable.just(2)));
  }

  Observable<Integer> testObservableEmpty() {
    return Observable.empty();
  }

  Completable testObservableIgnoreElements() {
    return Observable.just(1, 2).ignoreElements();
  }
}
