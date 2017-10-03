package br.com.creativesource.jfast.consumer;

import rx.Observable;

/**
 * An interface that will return an {@link rx.Observable} stream of strings.
 * 
 */
public interface TextObservable {
    public Observable<String> stream();
}
