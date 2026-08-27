package at.redi2go.photonics.engine.util.lock;

import at.redi2go.photonics.game.Disposable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * A reentrant lock used with try-with-resources statements
 */
public interface SafeLock {
    Key locked();

    Key lockedInterruptibly() throws InterruptedException;

    Key tryLocked();

    Key tryLocked(long time, TimeUnit unit) throws InterruptedException;

    SafeCondition newCondition();

    ConditionKey locked(SafeCondition condition);

    ConditionKey lockedInterruptibly(SafeCondition condition) throws InterruptedException;

    ConditionKey tryLocked(SafeCondition condition);

    ConditionKey tryLocked(SafeCondition condition, long time, TimeUnit unit) throws InterruptedException;

    interface Key extends Disposable {
        boolean isLocked();
    }

    interface ConditionKey extends Key, Condition {

    }

    static SafeLock newFairLock() {
        return new SafeLockImpl(true);
    }

    static SafeLock newUnfairLock() {
        return new SafeLockImpl(false);
    }
}
