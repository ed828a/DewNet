package com.example.edward.dewnet.adapter

import android.arch.paging.PagedListAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.edward.dewnet.R
import com.example.edward.dewnet.model.NetworkState
import com.example.edward.dewnet.model.VideoModel
import kotlinx.android.synthetic.main.row_second_list.view.*

class SecondListAdapter(val listener: (VideoModel) -> Unit,
                        val retryCallback: () -> Unit
) : PagedListAdapter<VideoModel, RecyclerView.ViewHolder>(MainListAdapter.COMPARATOR) {

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                R.layout.row_second_list -> {
                    val view = LayoutInflater.from(parent.context)
                            .inflate(R.layout.row_second_list, parent, false)
                    MainListViewHolder(view)
                }
                R.layout.cell_network_state -> NetworkStateItemViewHolder.create(parent, retryCallback)
                else -> throw IllegalArgumentException("unknown view type $viewType")
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.row_second_list -> {
                val item = getItem(position)
                if (item != null) {
                    (holder as MainListViewHolder).bind(item)
                }
            }
            R.layout.cell_network_state -> (holder as NetworkStateItemViewHolder).bindTo(networkState)
        }
    }

    override fun getItemCount(): Int = super.getItemCount() + if (hasExtraCell()) 1 else 0

    override fun getItemViewType(position: Int): Int =
            if (hasExtraCell() && position == itemCount - 1) {
                R.layout.cell_network_state
            } else {
                R.layout.row_second_list
            }

    private fun hasExtraCell() = networkState != null && networkState != NetworkState.LOADED

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraCell = hasExtraCell()
        this.networkState = newNetworkState
        val hasExtraCell = hasExtraCell()
        if (hadExtraCell != hasExtraCell) {
            if (hadExtraCell) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraCell && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    inner class MainListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle = itemView.textTitle
        private val textDate = itemView.textDate
        private val imageThumbnail = itemView.imageThumbnail

        fun bind(videoModel: VideoModel) {
            textTitle.text = videoModel.title
            textDate.text = videoModel.date
            Glide.with(itemView.context).load(videoModel.thumbnail).into(imageThumbnail)
            itemView.setOnClickListener {
                listener(videoModel)
            }
        }
    }
}