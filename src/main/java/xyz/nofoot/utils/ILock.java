package xyz.nofoot.utils;

public interface ILock {
    boolean tryLock(long timeOutSec);

    void unlock();
}
