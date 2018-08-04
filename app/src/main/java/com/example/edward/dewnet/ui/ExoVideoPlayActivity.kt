package com.example.edward.dewnet.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.commit451.youtubeextractor.YouTubeExtraction
import com.commit451.youtubeextractor.YouTubeExtractor
import com.example.edward.dewnet.R
import com.example.edward.dewnet.adapter.SecondListAdapter
import com.example.edward.dewnet.adapter.ThirdListAdapter
import com.example.edward.dewnet.model.*
import com.example.edward.dewnet.util.*
import com.example.edward.dewnet.viewmodel.MainViewModelFactory
import com.example.edward.dewnet.viewmodel.VideoPlayViewModel
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_exo_video_play.*
import kotlinx.android.synthetic.main.custom_playback_control.*
import kotlinx.android.synthetic.main.second_list.*
import javax.inject.Inject


class ExoVideoPlayActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: MainViewModelFactory
    private val queryViewModel: VideoPlayViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(VideoPlayViewModel::class.java)
    }

    private lateinit var videoModel: VideoModel
    private var isRelatedVideo: Boolean = false
    private lateinit var adapter: SecondListAdapter

    private lateinit var extractor: YouTubeExtractor

    // bandwidth meter to measure and estimate bandwidth
    private var player: SimpleExoPlayer? = null
    private var playbackPosition: Long = 0
    private var currentWindow: Int = 0
    private var playWhenReady = true
    private var videoUrl: String = ""
    private var currentVideotitle = ""

    private val preferences = DewNetApp.sharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exo_video_play)

        DewNetApp.appComponent.inject(this)

        if (ContextCompat.checkSelfPermission(this@ExoVideoPlayActivity,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@ExoVideoPlayActivity,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        videoModel = intent.getParcelableExtra(VIDEO_MODEL)

        extractor = YouTubeExtractor.Builder().okHttpClientBuilder(null).build()

        if (savedInstanceState != null) { // when Rotation, no need to search on the net.
            playbackPosition = savedInstanceState.getLong(PLAYBACK_POSITION)
            videoUrl = savedInstanceState.getString(VIDEO_URL)

        } else {
            extractUrl(videoModel.videoId)
        }

//        slidingUpPanel.isEnabled = resources.configuration.orientation != android.content.res.Configuration.ORIENTATION_PORTRAIT
//        initThirdList()

        if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            textVideoPlayTitle?.text = videoModel.title
            currentVideotitle = videoModel.title

            initRelatedList()
            initSearch()
            initDownload()
//            queryViewModel.showRelatedToVideoId(videoModel.videoId)
//            queryViewModel.backListStack.push(QueryData(videoModel.videoId, type = Type.RELATED_VIDEO_ID))
        } else {
            initThirdList()
        }

        queryViewModel.showRelatedToVideoId(videoModel.videoId)
        queryViewModel.backListStack.push(QueryData(videoModel.videoId, type = Type.RELATED_VIDEO_ID))
    }

    private fun extractUrl(videoId: String) {
        extractor.extract(videoId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { extraction ->
                            bindVideoToPlayer(extraction)
                        },
                        { error ->
                            errorHandler(error)
                        }
                )
    }

    private fun initDownload() {
        queryViewModel.downloadingState.observe(this, Observer { isSuccessful ->
            if (isSuccessful != null && isSuccessful) {
                Toast.makeText(this@ExoVideoPlayActivity,
                        "Downloading completed successfully.",
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ExoVideoPlayActivity,
                        "Downloading failed.", Toast.LENGTH_SHORT).show()
            }
        })
        buttonDownload.setOnClickListener {
            queryViewModel.download(videoUrl, currentVideotitle)
            Toast.makeText(this@ExoVideoPlayActivity, "Downloading started...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initRelatedList() {
        recyclerRelatedListView.layoutManager = LinearLayoutManager(this)
        adapter = SecondListAdapter(
                {
                    extractUrl(it.videoId)
                    queryViewModel.backListStack.push(QueryData(it.videoId, type = Type.RELATED_VIDEO_ID))
                    textVideoPlayTitle?.text = it.title
                    currentVideotitle = it.title

                    isRelatedVideo = true
                    intent.putExtra(VIDEO_MODEL, it)

                    if (queryViewModel.showRelatedToVideoId(it.videoId)) {
                        recyclerRelatedListView.scrollToPosition(0)
                        (recyclerRelatedListView.adapter as? SecondListAdapter)?.submitList(null)
                    }
                },
                { queryViewModel.retry() })

        recyclerRelatedListView.adapter = adapter

        queryViewModel.videoList.observe(this, videoListObserver)
        queryViewModel.networkState.observe(this, networkStateObserver)
    }

    private fun initThirdList() {
        thirdList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val thirdAdapter = ThirdListAdapter(
                {
                    extractUrl(it.videoId)
                    queryViewModel.backListStack.push(QueryData(it.videoId, type = Type.RELATED_VIDEO_ID))
                    currentVideotitle = it.title
                    isRelatedVideo = true
                    intent.putExtra(VIDEO_MODEL, it)

                    if (queryViewModel.showRelatedToVideoId(it.videoId)) {
                        thirdList.scrollToPosition(0)
                        (thirdList.adapter as? ThirdListAdapter)?.submitList(null)
                    }
                },
                { queryViewModel.retry() })

        thirdList.adapter = thirdAdapter

        queryViewModel.videoList.observe(this,
                Observer<PagedList<VideoModel>> { videoList ->
                    thirdAdapter.submitList(videoList) })

        queryViewModel.networkState.observe(this,
                Observer<NetworkState?> { networkState ->
                    thirdAdapter.setNetworkState(networkState) })
    }


    private fun initSearch() {
        buttonSearch.setOnSearchClickListener {
            buttonDownload.visibility = View.GONE
            textVideoPlayTitle.visibility = View.GONE

            buttonSearch.onActionViewExpanded()
        }

        buttonSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.trim()?.let {
                    if (it.isNotEmpty()) {
                        if (queryViewModel.showSearchQuery(it)) {
                            recyclerRelatedListView.scrollToPosition(0)
                            (recyclerRelatedListView.adapter as? SecondListAdapter)?.submitList(null)
                            queryViewModel.backListStack.push(QueryData(it, type = Type.QUERY_STRING))
                            preferences.edit().putString(KEY_QUERY, it).apply()
                        }
                    }
                }

                buttonSearch.onActionViewCollapsed()
                buttonDownload.visibility = View.VISIBLE
                textVideoPlayTitle.visibility = View.VISIBLE

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        val closeButton = buttonSearch.findViewById<ImageView>(R.id.search_close_btn)
        closeButton.setOnClickListener {
            buttonSearch.onActionViewCollapsed()
            buttonDownload.visibility = View.VISIBLE
            textVideoPlayTitle.visibility = View.VISIBLE
        }

        val searchEditText = buttonSearch.findViewById<EditText>(android.support.v7.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(resources.getColor(R.color.colorAccent))
    }

    private fun bindVideoToPlayer(result: YouTubeExtraction) {
        if (result.videoStreams.isEmpty()) {
            Toast.makeText(this@ExoVideoPlayActivity,
                    "This video isn't playable. Please try others.", Toast.LENGTH_LONG).show()
            return
        }

        if (videoUrl.isNotEmpty()) {
            queryViewModel.playListStack.push(VideoPlayedModel(
                    videoUrl,
                    playbackPosition,
                    currentVideotitle))
        }

        videoUrl = result.videoStreams.first().url
        playbackPosition = 0  // new video start
        Log.d("ExoMediaActivity", "videoUrl: $videoUrl")
        if (player != null) {
            releasePlayer()
        }
        initializePlayer(this, videoUrl)
    }

    private fun errorHandler(t: Throwable) {
        t.printStackTrace()
        Toast.makeText(this, "It failed to extract URL from YouTube.", Toast.LENGTH_SHORT).show()
    }

    private fun initializePlayer(context: Context, videoUrl: String) {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(
                    DefaultRenderersFactory(context),
                    DefaultTrackSelector(),
                    DefaultLoadControl())

            videoView.player = player
            player!!.playWhenReady = playWhenReady
            player!!.seekTo(currentWindow, playbackPosition)

        }
        val uri = Uri.parse(videoUrl)
        val mediaSource =
                ExtractorMediaSource.Factory(
                        DefaultHttpDataSourceFactory("exoPlayer"))
                        .createMediaSource(uri)
        player!!.prepare(mediaSource, false, false)
    }

    private fun releasePlayer() {
        if (player != null) {
            currentWindow = player!!.currentWindowIndex
            playWhenReady = player!!.playWhenReady
            player!!.release()
            player = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if (Util.SDK_INT > 23) {
            playbackPosition = player?.currentPosition ?: 0
        }
        outState?.putLong(PLAYBACK_POSITION, playbackPosition)
        outState?.putString(VIDEO_URL, videoUrl)

    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer(this, videoUrl)
        }
    }

    public override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer(this, videoUrl)
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            playbackPosition = player?.currentPosition ?: 0
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private val videoListObserver =
            Observer<PagedList<VideoModel>> { videoList -> adapter.submitList(videoList) }

    private val networkStateObserver =
            Observer<NetworkState?> { networkState -> adapter.setNetworkState(networkState) }

    fun fullscreen(view: View) {
        requestedOrientation = if (resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onBackPressed() {

        val backList = queryViewModel.backListStack.pop()
        if (backList == null) {
            super.onBackPressed()
        } else {
            if ((backList.type == Type.RELATED_VIDEO_ID &&
                            queryViewModel.showRelatedToVideoId(backList.query)) ||
                    (backList.type == Type.QUERY_STRING &&
                            queryViewModel.showSearchQuery(backList.query))) {

                when{
                    findViewById<View>(R.id.recyclerRelatedListView) != null -> {
                        recyclerRelatedListView.scrollToPosition(0)
                        (recyclerRelatedListView.adapter as? SecondListAdapter)?.submitList(null)
                    }

                    findViewById<RecyclerView>(R.id.thirdList) != null -> {
                        thirdList.scrollToPosition(0)
                        (thirdList.adapter as? ThirdListAdapter)?.submitList(null)
                    }
                }
            }
        }
    }

    fun playPreviousVideo(view: View) {

        val playedVideo = queryViewModel.playListStack.pop()
        if (playedVideo != null) {
            videoUrl = playedVideo.videoUrl
            playbackPosition = playedVideo.position
            if (findViewById<TextView>(R.id.textVideoPlayTitle) != null) {
                textVideoPlayTitle.text = playedVideo.title
            }

            Log.d("ExoMediaActivity", "videoUrl: $videoUrl")
            if (player != null) {
                releasePlayer()
            }
            initializePlayer(this, videoUrl)
        }
    }

}
