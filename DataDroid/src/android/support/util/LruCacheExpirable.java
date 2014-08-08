package android.support.util;

import java.util.HashMap;

import android.os.SystemClock;

public class LruCacheExpirable<K, V> extends LruCache<K, V> {

    private final HashMap<K, Long>    validityTime;
    private final int                 timeOut;

    /**
     * @param maxSize
     *            for caches that do not override {@link #sizeOf}, this is the maximum number of entries in the cache. For all other caches, this is
     *            the maximum sum of the sizes of the entries in this cache.
     * @param timeOut
     *            timeout in milliseconds, after that period {@link LruCacheExpirable#get(Object)} will return null, and remove entry
     */
    public LruCacheExpirable(final int maxSize, final int timeOut) {
        super(maxSize);
        this.timeOut = timeOut;
        this.validityTime = new HashMap<>(0, 0.75f);
    }

    /**
     * Returns the value for {@code key} if it exists in the cache or can be created by {@code #create}. If a value was returned, it is moved to the
     * head of the queue. This returns null if a value is not cached and cannot be created.
     */
    public final V get(final K key) {
        int nbPreviousCreated = createCount();
        V mapValue = super.get(key);
        synchronized (this) {
            if (mapValue != null) {
                if (createCount() > nbPreviousCreated) {
                    validityTime.put(key, SystemClock.uptimeMillis() + timeOut);
                }

                final boolean isValid = validityTime.get(key) > SystemClock.uptimeMillis();
                if (!isValid) {
                    hitCount--;
                    remove(key);
                    mapValue = null;
                }
            }
        }

        return mapValue;
    }

    /**
     * Caches {@code value} for {@code key}. The value is moved to the head of the queue.
     * 
     * @return the previous value mapped by {@code key}.
     */
    public V put(final K key, final V value) {

        V previous = super.put(key, value);
        synchronized (this) {
            validityTime.put(key, SystemClock.uptimeMillis() + timeOut);
        }
        return previous;
    }

    /**
     * Removes the entry for {@code key} if it exists.
     * 
     * @return the previous value mapped by {@code key}.
     */
    public V remove(final K key) {
        V previous = super.remove(key);

        synchronized (this) {
            if (previous != null) {
                validityTime.remove(key);
            }
        }
        return previous;
    }
}
