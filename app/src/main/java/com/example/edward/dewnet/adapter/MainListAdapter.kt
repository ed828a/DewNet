package com.example.edward.dewnet.adapter

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.edward.dewnet.R
import com.example.edward.dewnet.model.NetworkState
import com.example.edward.dewnet.model.VideoModel
import kotlinx.android.synthetic.main.cell_video.view.*

/**
 * Created by Edward on 7/31/2018.
 */

/**
 * @param listener: video play click listener
 * @param retryCallback: retry button click listener
 */
class MainListAdapter(
        val listener: (VideoModel) -> Unit,
        val retryCallback: () -> Unit
) : PagedListAdapter<VideoModel, RecyclerView.ViewHolder>(COMPARATOR) {

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                R.layout.cell_video -> {
                    val view = LayoutInflater.from(parent.context)
                            .inflate(R.layout.cell_video, parent, false)
                    MainListViewHolder(view)
                }
                R.layout.cell_network_state -> NetworkStateItemViewHolder.create(parent, retryCallback)
                else -> throw IllegalArgumentException("unknown view type $viewType")
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.cell_video -> {
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
                R.layout.cell_video
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
        private val textTitle = itemView.textViewTitle
        private val textDate = itemView.textViewDate
        private val imageThumbnail = itemView.imageViewThumb

        fun bind(videoModel: VideoModel) {
            textTitle.text = videoModel.title
            textDate.text = videoModel.date
            Glide.with(itemView.context).load(videoModel.thumbnail).into(imageThumbnail)
            itemView.setOnClickListener {
                listener(videoModel)
            }
        }

    }

    companion object {
        val COMPARATOR = object : DiffUtil.ItemCallback<VideoModel>() {
            override fun areItemsTheSame(oldItem: VideoModel, newItem: VideoModel): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: VideoModel, newItem: VideoModel): Boolean {
                return oldItem.videoId == newItem.videoId
            }

        }
    }

}