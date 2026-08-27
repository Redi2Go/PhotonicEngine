package at.redi2go.photonics.engine.util.lock;

import org.jspecify.annotations.NonNull;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class SafeLockImpl extends ReentrantLock implements SafeLock, SafeLock.Key {
    SafeLockImpl(boolean fair) {
        super(fair);
    }

    @Override
    public Key locked() {
        lock();
        return this;
    }

    @Override
    public Key lockedInterruptibly() throws InterruptedException {
        lockInterruptibly();
        return this;
    }

    @Override
    public Key tryLocked() {
        return tryLock() ? this : NotLockedKey.INSTANCE;
    }

    @Override
    public Key tryLocked(long time, TimeUnit unit) throws InterruptedException {
        return tryLock(time, unit) ? this : NotLockedKey.INSTANCE;
    }


    @Override
    public @NonNull ConditionImpl newCondition() {
        return new ConditionImpl(super.newCondition());
    }

    @Override
    public ConditionKey locked(SafeCondition condition) {
        lock();
        return (ConditionKey) condition;
    }

    @Override
    public ConditionKey lockedInterruptibly(SafeCondition condition) throws InterruptedException {
        lock();
        return (ConditionKey) condition;
    }

    @Override
    public ConditionKey tryLocked(SafeCondition condition) {
        return tryLock() ? (ConditionKey) condition : NotLockedKey.INSTANCE;
    }

    @Override
    public ConditionKey tryLocked(SafeCondition condition, long time, TimeUnit unit) throws InterruptedException {
        return tryLock(time, unit) ? (ConditionKey) condition : NotLockedKey.INSTANCE;
    }

    @Override
    public boolean isLocked() {
        return true;
    }

    @Override
    public void close() {
        unlock();
    }

    class ConditionImpl implements SafeCondition, ConditionKey {
        private final Condition backing;

        private ConditionImpl(Condition backing) {
            this.backing = backing;
        }

        @Override
        public boolean isLocked() {
            return true;
        }

        @Override
        public void await() throws InterruptedException {
            backing.await();
        }

        @Override
        public void awaitUninterruptibly() {
            backing.awaitUninterruptibly();
        }

        @Override
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            return backing.awaitNanos(nanosTimeout);
        }

        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            return backing.await(time, unit);
        }

        @Override
        public boolean awaitUntil(@NonNull Date deadline) throws InterruptedException {
            return backing.awaitUntil(deadline);
        }

        @Override
        public void signal() {
            backing.signal();
        }

        @Override
        public void signalAll() {
            backing.signalAll();
        }


        @Override
        public void close() {
            unlock();
        }
    }

    private static class NotLockedKey implements ConditionKey {
        private static final NotLockedKey INSTANCE = new NotLockedKey();

        @Override
        public boolean isLocked() {
            return false;
        }

        @Override
        public void await() throws InterruptedException {
            throw new IllegalMonitorStateException();
        }

        @Override
        public void awaitUninterruptibly() {
            throw new IllegalMonitorStateException();
        }

        @Override
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            throw new IllegalMonitorStateException();
        }

        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            throw new IllegalMonitorStateException();
        }

        @Override
        public boolean awaitUntil(@NonNull Date deadline) throws InterruptedException {
            throw new IllegalMonitorStateException();
        }

        @Override
        public void signal() {
            throw new IllegalMonitorStateException();
        }

        @Override
        public void signalAll() {
            throw new IllegalMonitorStateException();
        }

        @Override
        public void close() {

        }
    }
}
