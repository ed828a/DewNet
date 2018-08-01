package com.example.edward.dewnet.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.edward.dewnet.R
import com.example.edward.dewnet.model.NetworkState
import com.example.edward.dewnet.model.Status
import kotlinx.android.synthetic.main.cell_network_state.view.*

/**
 * Created by Edward on 7/31/2018.
 */


/**
 * A View Holder that can display a loading or have click action.
 * It is used to show the network state of paging.
 */
class NetworkStateItemViewHolder(view: View,
                                 private val retryCallback: () -> Unit)
    : RecyclerView.ViewHolder(view) {
    private val progressBar = view.progress_bar
    private val retry = view.retry_button
    private val errorMsg = view.error_msg
    init {
        retry.setOnClickListener {
            retryCallback()
        }
    }
    fun bindTo(networkState: NetworkState?) {
        progressBar.visibility = toVisbility(networkState?.status == Status.RUNNING)
        retry.visibility = toVisbility(networkState?.status == Status.FAILED)
        errorMsg.visibility = toVisbility(networkState?.msg != null)
        errorMsg.text = networkState?.msg
    }

    companion object {
        fun create(parent: ViewGroup, retryCallback: () -> Unit): NetworkStateItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.cell_network_state, parent, false)
            return NetworkStateItemViewHolder(view, retryCallback)
        }

        fun toVisbility(constraint : Boolean): Int {
            return if (constraint) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}