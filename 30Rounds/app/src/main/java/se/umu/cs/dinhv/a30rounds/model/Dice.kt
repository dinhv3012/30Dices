package se.umu.cs.dinhv.a30rounds.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Dice represents a single dice with a value from 1 to 6.
 */
class Dice() : Parcelable {
    var value: Int = (1..6).random()

    /**
     * Rolls the dice to get a new random value.
     */
    fun roll() {
        value = (1..6).random()
    }

    constructor(parcel: Parcel) : this() {
        value = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(value)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Dice> {
        override fun createFromParcel(parcel: Parcel): Dice = Dice(parcel)
        override fun newArray(size: Int): Array<Dice?> = arrayOfNulls(size)
    }
}
