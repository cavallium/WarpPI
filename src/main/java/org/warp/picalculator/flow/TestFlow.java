package org.warp.picalculator.flow;

import org.apache.commons.lang3.tuple.Pair;

public class TestFlow {
	public static void main(String[] args) {
		try {

			BehaviorSubject<Float> subject0 = BehaviorSubject.create(0f);
			
			Disposable s00 = subject0.subscribe((val) -> {
				System.out.println(val);
			});
			Thread.sleep(100);
			subject0.onNext(1f);
			subject0.onNext(2f);
			subject0.onNext(3f);
			subject0.onNext(4f);
			subject0.onNext(5f);
			subject0.onNext(60f);
			s00.dispose();
			subject0.onNext(60f);
			subject0.onNext(7f);
			
			
			subject0.onComplete();
			System.out.println("items sent.");

			Subject<Float> subject1 = BehaviorSubject.create(0f);

			Disposable s01 = subject1.map((val) -> val + 1).subscribe((val) -> {
				System.out.println(val);
			});
			Thread.sleep(100);
			subject1.onNext(1f);
			subject1.onNext(2f);
			subject1.onNext(3f);
			subject1.onNext(4f);
			subject1.onNext(5f);
			subject1.onNext(60f);
			s01.dispose();
			subject1.onNext(60f);
			subject1.onNext(7f);
			
			subject1.onComplete();
			System.out.println("items sent.");

			BehaviorSubject<Float> subjectA = BehaviorSubject.create();
			BehaviorSubject<Float> subjectB = BehaviorSubject.create();
			Observable<Float> observable = Observable.merge(subjectA, subjectB);

			Disposable s1 = observable.subscribe((val) -> {
				System.out.println(val);
			});
			Thread.sleep(100);
			subjectA.onNext(1f);
			subjectA.onNext(2f);
			subjectA.onNext(3f);
			subjectA.onNext(4f);
			subjectA.onNext(5f);
			subjectB.onNext(60f);
			s1.dispose();
			subjectB.onNext(60f);
			subjectA.onNext(7f);
			
			subjectB.onComplete();
			subjectA.onComplete();
			Thread.sleep(100);
			System.out.println("no more news subscribers left, closing publisher..");

			BehaviorSubject<Float> subjectC = BehaviorSubject.create();
			BehaviorSubject<Float> subjectD = BehaviorSubject.create();
			Observable<Pair<Float, Float>> observableCombined = Observable.combineLatest(subjectC, subjectD);
			System.out.println("Combined observable: " + observableCombined.toString());
			Disposable s2 = observableCombined.subscribe((val) -> {
				System.out.println(val);
			});
			Thread.sleep(100);
			subjectC.onNext(1f);
			subjectC.onNext(2f);
			subjectC.onNext(3f);
			subjectC.onNext(4f);
			subjectC.onNext(5f);
			subjectD.onNext(60f);
			subjectD.onNext(60f);
			subjectC.onNext(7f);
			s2.dispose();
			
			subjectD.onComplete();
			subjectC.onComplete();
			System.out.println("items sent.");

			ObservableInterval timA = ObservableInterval.create(100L);
			Disposable d = timA.subscribe((t) -> {
				System.out.println(t);
			});
			
			Thread.sleep(500);
			d.dispose();
			System.out.println("items sent.");

			ObservableInterval subjectE = ObservableInterval.create(100L);
			BehaviorSubject<Float> subjectF = BehaviorSubject.create();
			Observable<Pair<Long, Float>> observableZipped = Observable.zip(subjectE, subjectF);
			System.out.println("Zipped observable: " + observableZipped.toString());
			Disposable s3 = observableZipped.subscribe((val) -> {
				System.out.println(val);
			});
			Thread.sleep(100);
			subjectF.onNext(1f);
			Thread.sleep(100);
			subjectF.onNext(2f);
			Thread.sleep(100);
			subjectF.onNext(3f);
			Thread.sleep(100);
			subjectF.onNext(4f);
			Thread.sleep(100);
			subjectF.onNext(5f);
			Thread.sleep(100);
			subjectF.onNext(60f);
			Thread.sleep(100);
			subjectF.onNext(60f);
			Thread.sleep(100);
			subjectF.onNext(7f);
			Thread.sleep(500);
			s3.dispose();
			
			subjectF.onComplete();
			System.out.println("items sent.");
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
