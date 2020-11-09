package com.chw;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @author ChaiHongwei
 * @date 2020-11-09 15:33
 */
public class Test {
    public static void main(String[] args) {
        new Observable<Integer>() {
            @Override
            protected void subscribeActual(@NonNull Observer<? super Integer> observer) {

            }
        };

        //事件源,用于发送事件
        ObservableOnSubscribe<Integer> source = new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> emitter) throws Throwable {
                //注意:这里的emitter其实是ObservableCreate.CreateEmitter

                if (emitter.isDisposed()) {
                    return;
                }
                try {
                    emitter.onNext(1);
                    emitter.onComplete();
                } catch (Exception ex) {
                    emitter.onError(ex);
                }
            }
        };

        //观察者,用于订阅Observable,接收事件源发送过来的事件
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Integer integer) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };

        //创建一个Observable,Observable继承自ObservableSource
        Observable.create(source)//如果内部没有hook,返回ObservableCreate(source)对象

//                .map(new Function<Integer, Integer>() {
//                    @Override
//                    public Integer apply(Integer integer) throws Throwable {
//                        return integer;
//                    }
//                })//ObservableMap(ObservableCreate(source))

                //观察者回调在新线程中执行
                .observeOn(Schedulers.newThread())//返回ObservableObserveOn(ObservableCreate(source))
                //订阅者方法在io线程中执行
                .subscribeOn(Schedulers.io())//返回ObservableSubscribeOn(ObservableObserveOn(ObservableCreate(source)))


//                .map(new Function<Integer, Integer>() {
//                    @Override
//                    public Integer apply(Integer integer) throws Throwable {
//                        return integer;
//                    }
//                })
//                .subscribeOn(Schedulers.io())


                //订阅事件,执行订阅者方法
                //也就是ObservableSubscribeOn(ObservableObserveOn(ObservableCreate(source))).subscribeActual(observer)
                .subscribe(observer);

        //在io线程中,重新订阅,也就是下面的代码:
        //ObservableObserveOn(ObservableCreate(source)).subscribeActual(ObservableSubscribeOn.SubscribeOnObserver)

        //ObservableCreate(source).subscribe(ObservableObserveOn.ObserveOnObserver)

        //ObservableCreate(source).subscribeActual(ObservableObserveOn.ObserveOnObserver)

        //ObservableObserveOn.ObserveOnObserver.onSubscribe(CreateEmitter(ObservableObserveOn.ObserveOnObserver))

        //source.subscribe(CreateEmitter(ObservableObserveOn.ObserveOnObserver))


        //这个还是在io线程中执行的
        //CreateEmitter.onNext(1)

        //ObservableObserveOn.ObserveOnObserver.onNext(1)

        //切换主线程调用,之后都在主线程中了
        //ObservableObserveOn.ObserveOnObserver.schedule().onNext(1)

        //ObservableSubscribeOn.SubscribeOnObserver.onNext(1)

        //一直回调到observer.onNext(1)


        /**
         * 1.调用链最下面的subscribeOn用于控制这行代码之上的所有代码的执行线程,除非上面遇到observeOn(参考3).
         * subscribeOn(Schedulers.io())和subscribeOn(AndroidSchedulers.mainThread())组合后还是io线程
         *
         * 2.调用链observeOn用于控制这行代码之下的代码的执行线程,
         * 直到遇到下面新的observeOn重新切换,否则一直都在observeOn执行
         *
         * 3.observeOn的优先级要比subscribeOn的优先级高,
         * 比如observeOn(AndroidSchedulers.mainThread()).map.subscribeOn(Schedulers.io())中的map在main线程中执行
         *
         * 4.方便记忆:
         * 整个是个装饰模式,下面的包装上面的,回调的时候是包装里面的回调包装外面的
         * 执行到subscribeOn的时候,就开始对包装的source进行切换线程了,而source肯定是上面的代码包装后的结果,
         * 所以subscribeOn控制的是上面的代码的线程切换
         *
         * 执行到observeOn的时候,只是在指定的线程等着包装里面的调用onNext,然后在里面切换线程,
         * 去通知观察者,所以控制的是下面的代码的线程
         *
         * 5.
         * onSubscribe在subscribe方法调用的线程执行
         * subscribe在subscribeOn指定的线程执行(当main和io线程同时指定时,在io线程中执行)
         * onNext在最后一个observeOn指定的线程执行
         */
    }
}
