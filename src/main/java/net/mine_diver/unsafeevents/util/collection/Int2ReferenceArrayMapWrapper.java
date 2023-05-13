package net.mine_diver.unsafeevents.util.collection;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Array wrapper which provides map-like interaction
 * with the fastest element retrieval method.
 *
 * <p>
 *     Does not contain an array by itself,
 *     instead, backed by an outside array
 *     using a getter, a setter, and an array constructor,
 *     to allow for better JIT inlining of element retrieval.
 * </p>
 *
 * <p>
 *     Allows to have a custom default return value.
 * </p>
 *
 * <p>
 *     To get an element, ensure array capacity using
 *     {@code if (key >= array.length) wrapper.resizeArray(key + 1);},
 *     and get the element directly from the array.
 * </p>
 *
 * @param <V> the value type.
 * @author mine_diver
 */
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public final class Int2ReferenceArrayMapWrapper<V> {
    /**
     * The default return value of this array.
     *
     * <p>
     *     The unused indices of this array
     *     are filled with this value.
     * </p>
     */
    @NotNull V drv;

    /**
     * The backing array getter.
     */
    @NotNull Supplier<@NotNull V @NotNull []> arrGetter;

    /**
     * The backing array setter.
     */
    @NotNull Consumer<@NotNull V @NotNull []> arrSetter;

    /**
     * The constructor of array with the specified type.
     */
    @NotNull IntFunction<V @NotNull []> arrInst;

    /**
     * The default array wrapper constructor.
     *
     * @param arrGetter the backing array getter.
     * @param arrSetter the backing array setter.
     * @param arrInst the constructor of map's value type array.
     * @param defaultReturnValue the default element of the backing array.
     * @param initialCapacity the initial size of the backing array.
     */
    public Int2ReferenceArrayMapWrapper(
            final @NotNull Supplier<@NotNull V @NotNull []> arrGetter,
            final @NotNull Consumer<@NotNull V @NotNull []> arrSetter,
            final @NotNull IntFunction<V @NotNull []> arrInst,
            final @NotNull V defaultReturnValue,
            final int initialCapacity
    ) {
        this.arrGetter = arrGetter;
        this.arrSetter = arrSetter;
        this.arrInst = arrInst;
        drv = defaultReturnValue;
        arrSetter.accept(newArray(initialCapacity));
    }

    /**
     * Associates an int key with a {@link V} value.
     *
     * @param key the int to associate the value with.
     * @param value the {@link V} to associate the key with.
     */
    public void put(
            final int key,
            final @NotNull V value
    ) {
        ensureArrayCapacity(key);
        arrGetter.get()[key] = value;
    }

    /**
     * Ensures that the backing array length is greater
     * than the specified int key.
     *
     * @param key the key to check.
     */
    private void ensureArrayCapacity(
            final int key
    ) {
        if (key >= arrGetter.get().length) resizeArray(key + 1);
    }

    /**
     * Resizes the backing array with the given new length.
     *
     * <p>
     *     Used for ensuring element retrieval won't throw {@link ArrayIndexOutOfBoundsException}.
     * </p>
     *
     * @param newLength the new backing array length.
     * @throws IllegalStateException if the new array wasn't set properly and/or the getter didn't return the new backing array.
     */
    public void resizeArray(
            final int newLength
    ) {
        val newArr = newArray(newLength);
        System.arraycopy(arrGetter.get(), 0, newArr, 0, arrGetter.get().length);
        arrSetter.accept(newArr);
        if (arrGetter.get() != newArr) throw new IllegalStateException(
                "Array setter function didn't set the array properly, and/or array getter function returned the wrong array!"
        );
    }

    /**
     * Constructs a new array of value's type and fills it with the default return value.
     *
     * @param length the length of the new array.
     * @return the new array of the specified length filled with the default return value.
     */
    private @NotNull V @NotNull [] newArray(final int length) {
        val arr = arrInst.apply(length);
        if (arr.length != length) throw new IllegalStateException(String.format(
                "Array instantiation function returned an array of incorrect size! (Expected: %d, got: %d)",
                length, arr.length
        ));
        Arrays.fill(arr, drv);
        return arr;
    }
}
