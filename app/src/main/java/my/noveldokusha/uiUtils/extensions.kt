package my.noveldokusha.uiUtils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import my.noveldokusha.App

fun RecyclerView.ViewHolder.addBottomMargin(margin: Int = 1000, condition: () -> Boolean): Unit
{
	itemView.layoutParams = (itemView.layoutParams as ViewGroup.MarginLayoutParams).also {
		it.bottomMargin = if (condition()) margin else 0
	}
}

fun RecyclerView.ViewHolder.addRightMargin(margin: Int = 400, condition: () -> Boolean): Unit
{
	itemView.layoutParams = (itemView.layoutParams as ViewGroup.MarginLayoutParams).also {
		it.rightMargin = if (condition()) margin else 0
	}
}

fun toast(text: String, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(App.instance, text, duration).show()

val View.inflater: LayoutInflater get() = LayoutInflater.from(context)

fun <T, A> Observer<T>.switchLiveData(old: A?, new: A?, owner: LifecycleOwner, liveData: A.() -> LiveData<T>)
{
	old?.let { liveData(it).removeObserver(this) }
	new?.let { liveData(it).observe(owner, this) }
}