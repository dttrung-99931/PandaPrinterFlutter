import 'dart:async';

extension AsyncCompleter<T> on Completer<T> {
  void completeIfNot([FutureOr<T>? value]) {
    if (isCompleted) return;
    complete(value);
  }
}

enum CompleteStatus {
  success,
  failed,
  timeout,
  canceled,
}

typedef StatusCompleter = Completer<CompleteStatus>;

extension StatusCompleterExtension on StatusCompleter {
  void willTimeoutIn(Duration duration) {
    Timer(duration, () => completeIfNot(CompleteStatus.timeout));
  }
}
